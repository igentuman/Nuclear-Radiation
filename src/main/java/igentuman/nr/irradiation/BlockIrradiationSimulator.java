package igentuman.nr.irradiation;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.recipe.BlockIrradiationRecipe;
import igentuman.nr.recipe.BlockIrradiationRecipe.BlockOutput;
import igentuman.nr.util.tracking.WorldRadSource;
import igentuman.nr.util.tracking.WorldSourceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Transforms blocks near strong world radiation sources over time. The main thread only snapshots
 * plain source data plus read-only {@link ChunkAccess} references in range; the daemon thread
 * traces rays and matches recipes entirely off the main thread, returning a {@code Map<BlockPos,
 * Transform>} that the main thread applies (with a TOCTOU re-check) via {@code setBlock}.
 *
 * <p><b>Concurrency:</b> the worker reads block states from the snapshotted chunk sections off the
 * main thread. Vanilla {@code PalettedContainer.get} takes no lock, so a concurrent main-thread
 * write to the same section is a data race; the worker is fully wrapped in try/catch and the sample
 * cadence is low, but this trades absolute safety for keeping the ray trace off-thread.
 */
public final class BlockIrradiationSimulator {

    private static final BlockIrradiationSimulator INSTANCE = new BlockIrradiationSimulator();
    public static BlockIrradiationSimulator get() { return INSTANCE; }

    private BlockIrradiationSimulator() {}

    private final LinkedBlockingQueue<IrradiationJob> workQueue = new LinkedBlockingQueue<>();
    private final Queue<Runnable> mainThreadTasks = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread worker;
    private volatile MinecraftServer server;

    private record SourceData(double x, double y, double z, double activity) {}

    private record IrradiationJob(ResourceKey<Level> dim, List<SourceData> sources,
                                  ChunkSnapshot chunks, int radius, int rays, long seed) {}

    private record Transform(BlockState state, Block expected) {}

    private record CandidateBlock(BlockPos pos, Block block, double localBq) {}

    public void startWorker() {
        if (running.getAndSet(true)) return;
        worker = new Thread(this::workerLoop, "nuclear-radiation-block-irradiation");
        worker.setDaemon(true);
        worker.start();
        NuclearRadiation.LOGGER.info("Block irradiation worker thread started");
    }

    public void stopWorker() {
        if (!running.getAndSet(false)) return;
        workQueue.clear();
        if (worker != null) worker.interrupt();
        worker = null;
        mainThreadTasks.clear();
        server = null;
        NuclearRadiation.LOGGER.info("Block irradiation worker thread stopped");
    }

    public void tick(ServerLevel level) {
        if (!RadiationConfig.BLOCK_IRRADIATION_ENABLED.get()) return;
        server = level.getServer();
        long t = level.getGameTime();
        if (t % RadiationConfig.BLOCK_IRRADIATION_INTERVAL_TICKS.get() == 0) {
            snapshot(level);
        }
        drainMainThreadTasks();
    }

    /** Main thread: gather qualifying sources + read-only chunk refs in range, enqueue one job. */
    private void snapshot(ServerLevel level) {
        BlockIrradiationCache.ensureBuilt(level.getRecipeManager());
        Collection<WorldRadSource> sources = WorldSourceRegistry.get(level).all();
        if (sources.isEmpty()) return;

        double minSourceBq = RadiationConfig.BLOCK_IRRADIATION_MIN_SOURCE_BQ.get();
        int radius = RadiationConfig.BLOCK_IRRADIATION_RADIUS.get();
        int rays = RadiationConfig.BLOCK_IRRADIATION_RAYS.get();
        int maxSources = RadiationConfig.BLOCK_IRRADIATION_MAX_SOURCES.get();

        List<SourceData> snapshot = new ArrayList<>();
        int used = 0;
        for (WorldRadSource s : sources) {
            if (used >= maxSources) break;
            if (!s.isActive()) continue;
            double activity = s.activityBq();
            if (activity < minSourceBq) continue;
            used++;
            BlockPos p = s.getPosition();
            snapshot.add(new SourceData(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, activity));
        }
        if (snapshot.isEmpty()) return;

        ChunkSnapshot chunks = collectChunks(level, snapshot, radius);
        if (chunks.isEmpty()) return;

        workQueue.offer(new IrradiationJob(level.dimension(), snapshot, chunks,
                radius, rays, level.random.nextLong()));
    }

    /** Main thread: grab references to already-loaded chunks whose columns fall within radius. */
    private ChunkSnapshot collectChunks(ServerLevel level, List<SourceData> sources, int radius) {
        Map<Long, ChunkAccess> map = new HashMap<>();
        for (SourceData s : sources) {
            int minCx = ((int) Math.floor(s.x()) - radius) >> 4;
            int maxCx = ((int) Math.floor(s.x()) + radius) >> 4;
            int minCz = ((int) Math.floor(s.z()) - radius) >> 4;
            int maxCz = ((int) Math.floor(s.z()) + radius) >> 4;
            for (int cx = minCx; cx <= maxCx; cx++) {
                for (int cz = minCz; cz <= maxCz; cz++) {
                    long key = ChunkPos.asLong(cx, cz);
                    if (map.containsKey(key)) continue;
                    ChunkAccess chunk = level.getChunkSource().getChunkNow(cx, cz);
                    if (chunk != null) map.put(key, chunk);
                }
            }
        }
        return new ChunkSnapshot(map);
    }

    private void workerLoop() {
        while (running.get()) {
            try {
                IrradiationJob job = workQueue.take();
                Map<BlockPos, Transform> transforms = compute(job);
                if (!transforms.isEmpty()) {
                    mainThreadTasks.offer(() -> apply(job.dim(), transforms));
                }
            } catch (InterruptedException e) {
                if (!running.get()) return;
            } catch (Throwable t) {
                NuclearRadiation.LOGGER.error("Block irradiation worker error", t);
            }
        }
    }

    /** Off-thread: trace rays against the chunk snapshot, match recipes, build the transform map. */
    private Map<BlockPos, Transform> compute(IrradiationJob job) {
        Map<BlockPos, Transform> out = new HashMap<>();
        RandomSource rng = RandomSource.create(job.seed());
        for (SourceData s : job.sources()) {
            for (int i = 0; i < job.rays(); i++) {
                // uniform random direction on the unit sphere
                double cosPhi = rng.nextDouble() * 2.0 - 1.0;
                double sinPhi = Math.sqrt(Math.max(0.0, 1.0 - cosPhi * cosPhi));
                double theta = rng.nextDouble() * (Math.PI * 2.0);
                double rdx = sinPhi * Math.cos(theta);
                double rdy = cosPhi;
                double rdz = sinPhi * Math.sin(theta);

                CandidateBlock c = firstHit(job.chunks(), s.x(), s.y(), s.z(), rdx, rdy, rdz, job.radius(), s.activity());
                if (c == null || out.containsKey(c.pos())) continue;

                for (BlockIrradiationRecipe r : BlockIrradiationCache.forBlock(c.block())) {
                    if (r.minBq() > c.localBq()) break;        // sorted ascending: no further match
                    if (r.outputs().isEmpty()) continue;
                    if (rng.nextFloat() < r.chance()) {
                        BlockOutput picked = weightedPick(r.outputs(), rng);
                        out.put(c.pos(), new Transform(picked.block().defaultBlockState(), c.block()));
                        break;                                  // one transform per block per interval
                    }
                }
            }
        }
        return out;
    }

    /**
     * Marches a ray via voxel-DDA (Amanatides–Woo) and returns the first solid block hit as a
     * transform candidate. Returns null if the ray exits the radius through air, leaves the
     * snapshot, or the first solid block is not transformable — that surface absorbs the ray either
     * way (occlusion). Reads only the pre-snapshotted chunks, so it is safe to run off-thread.
     */
    private CandidateBlock firstHit(ChunkSnapshot chunks, double ox, double oy, double oz,
                                    double rdx, double rdy, double rdz, int radius, double activity) {
        int x = (int) Math.floor(ox);
        int y = (int) Math.floor(oy);
        int z = (int) Math.floor(oz);

        int stepX = (int) Math.signum(rdx);
        int stepY = (int) Math.signum(rdy);
        int stepZ = (int) Math.signum(rdz);

        double tDeltaX = rdx == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / rdx);
        double tDeltaY = rdy == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / rdy);
        double tDeltaZ = rdz == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / rdz);

        double tMaxX = rdx == 0 ? Double.POSITIVE_INFINITY : (stepX > 0 ? (x + 1 - ox) : (ox - x)) / Math.abs(rdx);
        double tMaxY = rdy == 0 ? Double.POSITIVE_INFINITY : (stepY > 0 ? (y + 1 - oy) : (oy - y)) / Math.abs(rdy);
        double tMaxZ = rdz == 0 ? Double.POSITIVE_INFINITY : (stepZ > 0 ? (z + 1 - oz) : (oz - z)) / Math.abs(rdz);

        double r2 = (double) radius * radius;

        for (int i = 0; i < radius * 3 + 3; i++) {
            // step one voxel first, so the source's own voxel is never tested as a hit
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) { x += stepX; tMaxX += tDeltaX; } else { z += stepZ; tMaxZ += tDeltaZ; }
            } else {
                if (tMaxY < tMaxZ) { y += stepY; tMaxY += tDeltaY; } else { z += stepZ; tMaxZ += tDeltaZ; }
            }

            double dx = x + 0.5 - ox, dy = y + 0.5 - oy, dz = z + 0.5 - oz;
            double d2 = dx * dx + dy * dy + dz * dz;
            if (d2 > r2) return null;                          // exited radius through air

            BlockState state = chunks.getBlockState(x, y, z);
            if (state.isAir()) continue;                       // air (or outside snapshot): keep marching

            // first solid hit — absorbs the ray whether or not it transforms
            Block block = state.getBlock();
            if (BlockIrradiationCache.forBlock(block).isEmpty()) return null;
            return new CandidateBlock(new BlockPos(x, y, z), block, activity);
        }
        return null;
    }

    private static BlockOutput weightedPick(List<BlockOutput> outputs, RandomSource rng) {
        if (outputs.size() == 1) return outputs.get(0);
        int total = 0;
        for (BlockOutput o : outputs) total += Math.max(0, o.weight());
        if (total <= 0) return outputs.get(0);
        int roll = rng.nextInt(total);
        for (BlockOutput o : outputs) {
            roll -= Math.max(0, o.weight());
            if (roll < 0) return o;
        }
        return outputs.get(outputs.size() - 1);
    }

    /** Main thread: apply the transform map, re-verifying each block is unchanged (TOCTOU guard). */
    private void apply(ResourceKey<Level> dim, Map<BlockPos, Transform> transforms) {
        MinecraftServer srv = server;
        if (srv == null) return;
        ServerLevel level = srv.getLevel(dim);
        if (level == null) return;
        int cap = RadiationConfig.BLOCK_IRRADIATION_MAX_TRANSFORMS.get();
        int applied = 0;
        for (Map.Entry<BlockPos, Transform> e : transforms.entrySet()) {
            if (applied >= cap) break;
            Transform t = e.getValue();
            if (!level.getBlockState(e.getKey()).is(t.expected())) continue;   // TOCTOU guard
            level.setBlock(e.getKey(), t.state(), Block.UPDATE_ALL);
            applied++;
        }
    }

    private void drainMainThreadTasks() {
        Runnable r;
        while ((r = mainThreadTasks.poll()) != null) {
            try { r.run(); } catch (Throwable t) {
                NuclearRadiation.LOGGER.error("Block irradiation apply error", t);
            }
        }
    }

    /**
     * Immutable view over a set of {@link ChunkAccess} references captured on the main thread.
     * Block-state reads walk the section palette directly — no {@code Level} / {@code ChunkSource}
     * access — so the worker never touches chunk loading off-thread.
     */
    private static final class ChunkSnapshot {
        private static final BlockState AIR = Blocks.AIR.defaultBlockState();

        private final Map<Long, ChunkAccess> chunks;

        ChunkSnapshot(Map<Long, ChunkAccess> chunks) { this.chunks = chunks; }

        boolean isEmpty() { return chunks.isEmpty(); }

        BlockState getBlockState(int x, int y, int z) {
            ChunkAccess chunk = chunks.get(ChunkPos.asLong(x >> 4, z >> 4));
            if (chunk == null) return AIR;
            int sectionIndex = chunk.getSectionIndex(y);
            LevelChunkSection[] sections = chunk.getSections();
            if (sectionIndex < 0 || sectionIndex >= sections.length) return AIR;
            LevelChunkSection section = sections[sectionIndex];
            if (section == null || section.hasOnlyAir()) return AIR;
            return section.getBlockState(x & 15, y & 15, z & 15);
        }
    }
}
