package igentuman.nr.radiation.simulation;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.IChunkRadiation;
import igentuman.nr.api.IPointRadiationSource;
import igentuman.nr.api.shielding.IRadiationSimulator;
import igentuman.nr.api.shielding.IRadiationSource;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.radiation.simulation.entity.EntityIgnoreFilter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class RadiationSimulator implements IRadiationSimulator {

    private static final RadiationSimulator INSTANCE = new RadiationSimulator();
    public static RadiationSimulator get() { return INSTANCE; }

    private final Map<ResourceKey<Level>, SourceSpatialIndex> indexByDim = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, Map<Long, SubChunkRadVector>> vectorByDim = new ConcurrentHashMap<>();

    private final LinkedBlockingQueue<Job> workQueue = new LinkedBlockingQueue<>();
    private final Queue<Runnable> mainThreadTasks = new ConcurrentLinkedQueue<>();
    private Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private record Job(ResourceKey<Level> dim,
                       RadiationSnapshot snapshot,
                       long[] subChunkKeys,
                       int radius,
                       long ttl) {}

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
        indexByDim.clear();
        vectorByDim.clear();
        mainThreadTasks.clear();
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
        if (t % RadiationConfig.WORLD_SIM_INTERVAL_TICKS.get() == 0) {
            pruneDeadSources(level);
            tickWorld(level);
        }
        if (t % RadiationConfig.ENTITY_SIM_INTERVAL_TICKS.get() == 0) tickEntities(level);
        drainMainThreadTasks();
    }

    private void pruneDeadSources(ServerLevel level) {
        SourceSpatialIndex index = indexFor(level);
        List<UUID> dead = new ArrayList<>();
        for (IRadiationSource s : index.all()) {
            if (!s.isActive()) dead.add(s.getId());
        }
        for (UUID id : dead) index.remove(id);
    }

    @Override
    public void tickWorld(ServerLevel level) {
        SourceSpatialIndex index = indexFor(level);
        if (index.size() == 0) {
            vectorByDim.remove(level.dimension());
            return;
        }

        Set<Long> occupied = collectOccupiedSubChunks(level);
        if (occupied.isEmpty()) {
            vectorByDim.remove(level.dimension());
            return;
        }

        List<RadiationSnapshot.SourceData> sources = new ArrayList<>();
        for (IRadiationSource s : index.all()) {
            if (!s.isActive()) continue;
            if (s instanceof IPointRadiationSource p) sources.add(RadiationSnapshot.of(p));
        }
        if (sources.isEmpty()) {
            vectorByDim.remove(level.dimension());
            return;
        }

        long[] keys = new long[occupied.size()];
        int i = 0;
        for (long k : occupied) keys[i++] = k;

        RadiationSnapshot snap = new RadiationSnapshot(sources, List.of(), level.getGameTime());
        int radius = RadiationConfig.MAX_SOURCE_RADIUS_M.get();
        long ttl = RadiationConfig.CHUNK_VECTOR_TTL_TICKS.get();
        workQueue.offer(new Job(level.dimension(), snap, keys, radius, ttl));
    }

    private Set<Long> collectOccupiedSubChunks(ServerLevel level) {
        Set<Long> occupied = new HashSet<>();
        for (Entity e : level.getAllEntities()) {
            if (!(e instanceof LivingEntity living)) continue;
            if (EntityIgnoreFilter.shouldSkip(living)) continue;
            int cx = e.chunkPosition().x;
            int cy = e.blockPosition().getY() >> 4;
            int cz = e.chunkPosition().z;
            occupied.add(SourceSpatialIndex.chunkKey(cx, cy, cz));
        }
        return occupied;
    }

    @Override
    public void tickEntities(ServerLevel level) {
    }

    @Override
    public void submitSources(List<IRadiationSource> sources) {
    }

    @Override
    public void submitChunks(List<IChunkRadiation> chunks) {
    }

    @Override
    public SubChunkRadVector getChunkVector(ChunkPos pos, int cy) {
        for (Map<Long, SubChunkRadVector> map : vectorByDim.values()) {
            SubChunkRadVector v = map.get(SourceSpatialIndex.chunkKey(pos.x, cy, pos.z));
            if (v != null) return v;
        }
        return null;
    }

    public SubChunkRadVector getChunkVector(ServerLevel level, ChunkPos pos, int cy) {
        Map<Long, SubChunkRadVector> map = vectorByDim.get(level.dimension());
        if (map == null) return null;
        return map.get(SourceSpatialIndex.chunkKey(pos.x, cy, pos.z));
    }

    private void workerLoop() {
        while (running.get()) {
            try {
                Job job = workQueue.take();
                RadiationResult res = compute(job);
                mainThreadTasks.offer(() -> apply(job.dim(), res));
            } catch (InterruptedException e) {
                if (!running.get()) return;
            } catch (Throwable t) {
                NuclearRadiation.LOGGER.error("Worker error", t);
            }
        }
    }

    private RadiationResult compute(Job job) {
        RadiationResult result = new RadiationResult(job.snapshot().tick);
        int radius = job.radius();
        long ttl = job.ttl();
        double r2 = (double) radius * radius;

        for (long key : job.subChunkKeys()) {
            int cx = SourceSpatialIndex.unpackCx(key);
            int cy = SourceSpatialIndex.unpackCy(key);
            int cz = SourceSpatialIndex.unpackCz(key);
            double apexX = cx * 16.0 + 8.0;
            double apexY = cy * 16.0 + 8.0;
            double apexZ = cz * 16.0 + 8.0;
            ChunkAccum accum = null;
            for (RadiationSnapshot.SourceData s : job.snapshot().sources) {
                double dx = s.x() - apexX;
                double dy = s.y() - apexY;
                double dz = s.z() - apexZ;
                if (dx * dx + dy * dy + dz * dz > r2) continue;
                if (accum == null) accum = new ChunkAccum(cx, cy, cz);
                accum.contribute(s);
            }
            if (accum != null) {
                result.vectorUpdates.put(key, accum.toVector(job.snapshot().tick, ttl));
            }
        }
        return result;
    }

    private void apply(ResourceKey<Level> dim, RadiationResult result) {
        if (result.vectorUpdates.isEmpty()) {
            vectorByDim.remove(dim);
            return;
        }
        Map<Long, SubChunkRadVector> map = vectorByDim.computeIfAbsent(
                dim, k -> new ConcurrentHashMap<>());
        long now = result.tick;
        map.entrySet().removeIf(e -> e.getValue().isExpired(now));
        map.putAll(result.vectorUpdates);
    }

    private void drainMainThreadTasks() {
        Runnable r;
        while ((r = mainThreadTasks.poll()) != null) {
            try { r.run(); } catch (Throwable t) {
                NuclearRadiation.LOGGER.error("Apply error", t);
            }
        }
    }

    private static final class ChunkAccum {
        final int cx, cy, cz;
        final double apexX, apexY, apexZ;
        final double[] sumXRay = new double[SubChunkRadVector.DIR_COUNT];
        final double[] sumNeutron = new double[SubChunkRadVector.DIR_COUNT];
        final double[] wx = new double[SubChunkRadVector.DIR_COUNT];
        final double[] wy = new double[SubChunkRadVector.DIR_COUNT];
        final double[] wz = new double[SubChunkRadVector.DIR_COUNT];
        final double[] sumW = new double[SubChunkRadVector.DIR_COUNT];
        final Set<UUID> processed = new HashSet<>();
        double maxBq;

        ChunkAccum(int cx, int cy, int cz) {
            this.cx = cx; this.cy = cy; this.cz = cz;
            this.apexX = cx * 16.0 + 8.0;
            this.apexY = cy * 16.0 + 8.0;
            this.apexZ = cz * 16.0 + 8.0;
        }

        void contribute(RadiationSnapshot.SourceData s) {
            if (!processed.add(s.id())) return;
            double dx = s.x() - apexX;
            double dy = s.y() - apexY;
            double dz = s.z() - apexZ;
            double dist2 = dx * dx + dy * dy + dz * dz + 1.0;
            double falloff = 1.0 / dist2;
            double xr = s.xRayBq() * falloff;
            double n  = s.neutronBq() * falloff;
            int dir = SubChunkRadVector.classify(dx, dy, dz);
            sumXRay[dir] += xr;
            sumNeutron[dir] += n;
            double w = xr + n;
            sumW[dir] += w;
            wx[dir] += s.x() * w;
            wy[dir] += s.y() * w;
            wz[dir] += s.z() * w;
            if (w > maxBq) maxBq = w;
        }

        SubChunkRadVector toVector(long tick, long ttl) {
            SubChunkRadVector v = new SubChunkRadVector();
            v.apexX = apexX; v.apexY = apexY; v.apexZ = apexZ;
            v.maxBq = maxBq;
            v.computedTick = tick;
            v.ttlTicks = ttl;
            for (int i = 0; i < SubChunkRadVector.DIR_COUNT; i++) {
                v.xRayBq[i] = sumXRay[i];
                v.neutronBq[i] = sumNeutron[i];
                if (sumW[i] > 0) {
                    double tx = wx[i] / sumW[i];
                    double ty = wy[i] / sumW[i];
                    double tz = wz[i] / sumW[i];
                    v.tip[i] = new Vec3(tx, ty, tz);
                    double dxa = tx - apexX, dya = ty - apexY, dza = tz - apexZ;
                    v.tipApexDist2[i] = dxa * dxa + dya * dya + dza * dza;
                } else {
                    double[] n = SubChunkRadVector.DIR_NORMALS[i];
                    v.tip[i] = new Vec3(apexX + n[0], apexY + n[1], apexZ + n[2]);
                    v.tipApexDist2[i] = 1.0;
                }
            }
            return v;
        }
    }
}
