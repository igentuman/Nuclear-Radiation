package igentuman.nr.radiation.source;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.DecayGraph;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.block.CreativeRadSourceBlockEntity;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.radiation.simulation.containers.ContainerRadiationTicker;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** Coalesces block mutations, then snapshots final states within a per-tick server-thread budget. */
public final class BlockSourceChangeProcessor {
    private static final BlockSourceChangeProcessor INSTANCE = new BlockSourceChangeProcessor();
    private static final int MAX_READS_PER_TICK = 4096;
    private static final int MAX_RELEVANT_PER_BATCH = 128;
    private static final long SERVER_TIME_BUDGET_NS = 1_000_000L;
    private static final long[] NO_RETRY = new long[0];

    public static BlockSourceChangeProcessor get() { return INSTANCE; }

    private final Map<ServerLevel, LevelQueue> levels = new IdentityHashMap<>();
    private final LinkedBlockingQueue<Batch> jobs = new LinkedBlockingQueue<>();
    private volatile boolean running;
    private Thread worker;

    private enum Kind { NONE, BLOCK, FLUID }

    private record Binding(Kind kind, RadiationProfile profile) {}
    private record Change(long pos, BlockState state, Binding binding) {}
    private record Prepared(long pos, BlockState state, Kind kind, RadiationProfile profile) {}
    private record Batch(LevelQueue queue, List<Change> changes, long tick) {}
    private record Result(List<Prepared> prepared, long[] retry) {}

    private static final class LevelQueue {
        final LongLinkedOpenHashSet changed = new LongLinkedOpenHashSet();
        final Queue<Result> completed = new ConcurrentLinkedQueue<>();
        volatile boolean active = true;
        boolean inFlight;
        List<Prepared> applying;
        int applyIndex;
    }

    private BlockSourceChangeProcessor() {}

    public void startWorker() {
        if (running) return;
        running = true;
        worker = new Thread(this::workerLoop, "nuclear-radiation-block-sources");
        worker.setDaemon(true);
        worker.start();
    }

    public void stopWorker() {
        running = false;
        if (worker != null) {
            worker.interrupt();
            try {
                worker.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        worker = null;
        jobs.clear();
        for (LevelQueue queue : levels.values()) queue.active = false;
        levels.clear();
    }

    public void unloadLevel(ServerLevel level) {
        LevelQueue queue = levels.remove(level);
        if (queue != null) {
            queue.active = false;
            queue.changed.clear();
            queue.completed.clear();
        }
    }

    public void markChanged(ServerLevel level, BlockPos pos) {
        markChanged(level, pos.asLong());
    }

    private void markChanged(ServerLevel level, long packedPos) {
        if (!level.getServer().isSameThread()) {
            level.getServer().execute(() -> markChanged(level, packedPos));
            return;
        }
        levels.computeIfAbsent(level, unused -> new LevelQueue()).changed.add(packedPos);
    }

    /** Reads only final block states, within a fixed server-thread time budget. */
    public void tick(ServerLevel level) {
        LevelQueue queue = levels.get(level);
        if (queue == null) return;
        long deadline = System.nanoTime() + SERVER_TIME_BUDGET_NS;
        if (queue.applying == null) {
            Result completed = queue.completed.poll();
            if (completed != null) {
                for (long pos : completed.retry()) queue.changed.add(pos);
                queue.applying = completed.prepared();
                queue.applyIndex = 0;
            }
        }
        if (queue.applying != null) {
            WorldSourceRegistry registry = WorldSourceRegistry.get(level);
            int applied = 0;
            while (queue.applyIndex < queue.applying.size()
                    && (applied == 0 || System.nanoTime() < deadline)) {
                apply(level, queue, registry, queue.applying.get(queue.applyIndex++));
                applied++;
            }
            if (queue.applyIndex == queue.applying.size()) {
                queue.applying = null;
                queue.inFlight = false;
            }
        }
        if (queue.inFlight || queue.changed.isEmpty() || !running || System.nanoTime() >= deadline) return;

        WorldSourceRegistry registry = WorldSourceRegistry.get(level);
        Map<BlockState, Binding> bindings = new IdentityHashMap<>();
        List<Change> changes = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int count = 0; count < MAX_READS_PER_TICK && !queue.changed.isEmpty()
                && changes.size() < MAX_RELEVANT_PER_BATCH && System.nanoTime() < deadline; count++) {
            long packedPos = queue.changed.removeFirstLong();
            int x = BlockPos.getX(packedPos);
            int y = BlockPos.getY(packedPos);
            int z = BlockPos.getZ(packedPos);
            LevelChunk chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
            if (chunk == null) continue;
            cursor.set(x, y, z);
            BlockState state = chunk.getBlockState(cursor);
            Binding binding = bindings.computeIfAbsent(state, this::resolveBinding);
            DecayGraph.WorldRadSource existing = registry.atBlock(cursor);
            if (binding.kind() != Kind.NONE || state.hasBlockEntity()
                    || existing instanceof BlockRadSource || existing instanceof FluidRadSource
                    || existing instanceof ContainerRadSource || existing instanceof CreativeRadSource) {
                changes.add(new Change(packedPos, state, binding));
            }
        }
        if (!changes.isEmpty()) {
            queue.inFlight = true;
            jobs.offer(new Batch(queue, changes, level.getGameTime()));
        }
    }

    private Binding resolveBinding(BlockState state) {
        if (RadiationConfig.blockRadiationEnabled()) {
            RadiationProfile profile = RadiationBindings.of(state);
            if (!profile.isEmpty()) return new Binding(Kind.BLOCK, profile);
        }
        if (RadiationConfig.fluidRadiationEnabled()) {
            FluidState fluid = state.getFluidState();
            if (!fluid.isEmpty()) {
                RadiationProfile profile = RadiationBindings.of(fluid);
                if (!profile.isEmpty()) return new Binding(Kind.FLUID, profile);
            }
        }
        return new Binding(Kind.NONE, RadiationProfile.empty());
    }

    private void workerLoop() {
        while (running) {
            Batch batch = null;
            try {
                batch = jobs.take();
                if (!batch.queue().active) continue;
                List<Prepared> prepared = new ArrayList<>(batch.changes().size());
                for (Change change : batch.changes()) {
                    Binding binding = change.binding();
                    RadiationProfile profile = binding.kind() == Kind.NONE
                            ? null : binding.profile().copy(batch.tick());
                    prepared.add(new Prepared(change.pos(), change.state(), binding.kind(), profile));
                }
                if (batch.queue().active) batch.queue().completed.offer(new Result(prepared, NO_RETRY));
            } catch (InterruptedException e) {
                if (!running) return;
            } catch (Throwable t) {
                NuclearRadiation.LOGGER.error("Block source worker error", t);
                if (batch != null && batch.queue().active) {
                    long[] retry = new long[batch.changes().size()];
                    for (int i = 0; i < retry.length; i++) retry[i] = batch.changes().get(i).pos();
                    batch.queue().completed.offer(new Result(List.of(), retry));
                }
            }
        }
    }

    private void apply(ServerLevel level, LevelQueue queue, WorldSourceRegistry registry, Prepared change) {
        long packedPos = change.pos();
        if (queue.changed.contains(packedPos)) return;
        int x = BlockPos.getX(packedPos);
        int y = BlockPos.getY(packedPos);
        int z = BlockPos.getZ(packedPos);
        LevelChunk chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
        if (chunk == null) return;
        BlockPos pos = new BlockPos(x, y, z);
        if (chunk.getBlockState(pos) != change.state()) {
            queue.changed.add(packedPos);
            return;
        }

        BlockEntity be = chunk.getBlockEntity(pos);
        boolean container = be instanceof RadiationProfile.IRadiatingContainer || be instanceof Container;
        boolean activeContainer = container && RadiationConfig.containerRadiationEnabled();
        if (activeContainer) ContainerRadiationTicker.track(level, pos);
        else ContainerRadiationTicker.untrack(level, pos);

        DecayGraph.WorldRadSource existing = registry.atBlock(pos);
        if (existing instanceof BlockRadSource || existing instanceof FluidRadSource
                || existing instanceof ContainerRadSource || existing instanceof CreativeRadSource) {
            registry.remove(existing.getId());
        }

        if (be instanceof CreativeRadSourceBlockEntity creative) {
            creative.registerSource(level);
        } else if (!activeContainer && registry.atBlock(pos) == null && change.profile() != null) {
            if (change.kind() == Kind.BLOCK) {
                registry.register(new BlockRadSource(UUID.randomUUID(), level.dimension(), pos,
                        change.profile(), level.getGameTime()));
            } else if (change.kind() == Kind.FLUID) {
                registry.register(new FluidRadSource(UUID.randomUUID(), level.dimension(), pos,
                        change.profile(), level.getGameTime()));
            }
        }
    }
}
