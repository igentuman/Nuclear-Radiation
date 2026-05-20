package igentuman.nr.simulation;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.IChunkRadiation;
import igentuman.nr.api.IPointRadiationSource;
import igentuman.nr.api.IRadiationSource;
import igentuman.nr.config.RadiationConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class RadiationSimulator implements IRadiationSimulator {

    private static final RadiationSimulator INSTANCE = new RadiationSimulator();
    public static RadiationSimulator get() { return INSTANCE; }

    private final Map<ResourceKey<Level>, SourceSpatialIndex> indexByDim = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, Map<Long, ChunkRadVector>> vectorByDim = new ConcurrentHashMap<>();

    private final LinkedBlockingQueue<Job> workQueue = new LinkedBlockingQueue<>();
    private final Queue<Runnable> mainThreadTasks = new ConcurrentLinkedQueue<>();
    private Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private record Job(ResourceKey<Level> dim, RadiationSnapshot snapshot) {}

    public void startWorker() {
        if (running.getAndSet(true)) return;
        worker = new Thread(this::workerLoop, "nuclear-radiation-sim");
        worker.setDaemon(true);
        worker.start();
        NuclearRadiation.LOGGER.info("Radiation worker thread started");
    }

    public void stopWorker() {
        if (!running.getAndSet(false)) return;
        workQueue.clear();
        if (worker != null) worker.interrupt();
        worker = null;
        NuclearRadiation.LOGGER.info("Radiation worker thread stopped");
    }

    public boolean isRunning() { return running.get(); }

    public SourceSpatialIndex indexFor(ServerLevel level) {
        return indexByDim.computeIfAbsent(level.dimension(), k -> new SourceSpatialIndex());
    }

    public void addSource(ServerLevel level, IRadiationSource src) {
        indexFor(level).add(src);
    }

    public void removeSource(ServerLevel level, java.util.UUID id) {
        indexFor(level).remove(id);
    }

    @Override
    public void tick(ServerLevel level) {
        long t = level.getGameTime();
        if (t % RadiationConfig.WORLD_SIM_INTERVAL_TICKS.get() == 0) tickWorld(level);
        if (t % RadiationConfig.ENTITY_SIM_INTERVAL_TICKS.get() == 0) tickEntities(level);
        drainMainThreadTasks();
    }

    @Override
    public void tickWorld(ServerLevel level) {
        SourceSpatialIndex index = indexFor(level);
        List<RadiationSnapshot.SourceData> sources = new ArrayList<>();
        for (IRadiationSource s : index.all()) {
            if (!s.isActive()) continue;
            if (s instanceof IPointRadiationSource p) {
                sources.add(RadiationSnapshot.of(p));
            }
        }
        RadiationSnapshot snap = new RadiationSnapshot(sources, List.of(), level.getGameTime());
        workQueue.offer(new Job(level.dimension(), snap));
    }

    @Override
    public void tickEntities(ServerLevel level) {
        // Entity sampling wiring lands in Phase 7. Skeleton holds the gating point.
    }

    @Override
    public void submitSources(List<IRadiationSource> sources) {
        // Direct manual feed (tests / non-world callers). Per-dim simulator uses addSource.
    }

    @Override
    public void submitChunks(List<IChunkRadiation> chunks) {
        // Reserved for chunk-load priming. Phase 5 wires it.
    }

    @Override
    public ChunkRadVector getChunkVector(ChunkPos pos) {
        for (Map<Long, ChunkRadVector> map : vectorByDim.values()) {
            ChunkRadVector v = map.get(SourceSpatialIndex.chunkKey(pos));
            if (v != null) return v;
        }
        return null;
    }

    public ChunkRadVector getChunkVector(ServerLevel level, ChunkPos pos) {
        Map<Long, ChunkRadVector> map = vectorByDim.get(level.dimension());
        if (map == null) return null;
        return map.get(SourceSpatialIndex.chunkKey(pos));
    }

    private void workerLoop() {
        while (running.get()) {
            try {
                Job job = workQueue.take();
                RadiationResult res = compute(job);
                mainThreadTasks.offer(() -> apply(job.dim, res));
            } catch (InterruptedException e) {
                if (!running.get()) return;
            } catch (Throwable t) {
                NuclearRadiation.LOGGER.error("Worker error", t);
            }
        }
    }

    private RadiationResult compute(Job job) {
        RadiationResult result = new RadiationResult(job.snapshot.tick);
        int radius = RadiationConfig.MAX_SOURCE_RADIUS_M.get();
        long ttl = RadiationConfig.CHUNK_VECTOR_TTL_TICKS.get();

        Map<Long, ChunkAccum> accum = new HashMap<>();
        for (RadiationSnapshot.SourceData s : job.snapshot.sources) {
            int minCx = (int) Math.floor((s.x() - radius)) >> 4;
            int maxCx = (int) Math.floor((s.x() + radius)) >> 4;
            int minCz = (int) Math.floor((s.z() - radius)) >> 4;
            int maxCz = (int) Math.floor((s.z() + radius)) >> 4;
            for (int cx = minCx; cx <= maxCx; cx++) {
                for (int cz = minCz; cz <= maxCz; cz++) {
                    final int fcx = cx;
                    final int fcz = cz;
                    long key = SourceSpatialIndex.chunkKey(fcx, fcz);
                    ChunkAccum a = accum.computeIfAbsent(key, k -> new ChunkAccum(fcx, fcz));
                    a.contribute(s, radius);
                }
            }
        }

        for (ChunkAccum a : accum.values()) {
            ChunkRadVector v = a.toVector(job.snapshot.tick, ttl);
            result.vectorUpdates.put(new ChunkPos(a.cx, a.cz), v);
        }
        return result;
    }

    private void apply(ResourceKey<Level> dim, RadiationResult result) {
        Map<Long, ChunkRadVector> map = vectorByDim.computeIfAbsent(dim, k -> new ConcurrentHashMap<>());
        for (Map.Entry<ChunkPos, ChunkRadVector> e : result.vectorUpdates.entrySet()) {
            map.put(SourceSpatialIndex.chunkKey(e.getKey()), e.getValue());
        }
    }

    private void drainMainThreadTasks() {
        Runnable r;
        while ((r = mainThreadTasks.poll()) != null) {
            try { r.run(); } catch (Throwable t) {
                NuclearRadiation.LOGGER.error("Apply error", t);
            }
        }
    }

    static final double CHUNK_Y_REF = 64.0;

    private static final class ChunkAccum {
        final int cx;
        final int cz;
        double sumXRay;
        double sumNeutron;
        double sumYXRay;
        double sumYNeutron;
        double maxBq;
        Vec3 weightedXRay = Vec3.ZERO;
        Vec3 weightedNeutron = Vec3.ZERO;

        ChunkAccum(int cx, int cz) {
            this.cx = cx;
            this.cz = cz;
        }

        void contribute(RadiationSnapshot.SourceData s, double maxR) {
            double cxCenter = cx * 16.0 + 8.0;
            double czCenter = cz * 16.0 + 8.0;
            double dx = s.x() - cxCenter;
            double dy = s.y() - CHUNK_Y_REF;
            double dz = s.z() - czCenter;
            double dist2 = dx * dx + dy * dy + dz * dz + 1.0;
            double falloff = 1.0 / dist2;
            double xray = s.xRayBq() * falloff;
            double neutron = s.neutronBq() * falloff;
            sumXRay += xray;
            sumNeutron += neutron;
            sumYXRay += s.y() * xray;
            sumYNeutron += s.y() * neutron;
            double total = xray + neutron;
            if (total > maxBq) maxBq = total;
            weightedXRay = weightedXRay.add(dx * xray, 0, dz * xray);
            weightedNeutron = weightedNeutron.add(dx * neutron, 0, dz * neutron);
        }

        ChunkRadVector toVector(long tick, long ttl) {
            ChunkRadVector v = new ChunkRadVector();
            v.centerScalarXRay = sumXRay;
            v.centerScalarNeutron = sumNeutron;
            v.maxBq = maxBq;
            v.computedTick = tick;
            v.ttlTicks = ttl;
            double totalBq = sumXRay + sumNeutron;
            v.centerY = totalBq > 0 ? (sumYXRay + sumYNeutron) / totalBq : CHUNK_Y_REF;
            if (sumXRay > 0) v.gradientXRay = weightedXRay.scale(1.0 / sumXRay);
            if (sumNeutron > 0) v.gradientNeutron = weightedNeutron.scale(1.0 / sumNeutron);
            return v;
        }
    }
}
