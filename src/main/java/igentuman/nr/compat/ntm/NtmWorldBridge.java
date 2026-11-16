package igentuman.nr.compat.ntm;

import com.hbm.handler.radiation.RadiationSystemNT;
import dev.golem.core.world.Protection;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.config.NtmCompatConfig;
import igentuman.nr.radiation.simulation.RadiationFieldSampler;
import igentuman.nr.radiation.storage.NRAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Pinned-NTM adapter. Access only on the server thread and behind NtmCompatibility.enabled(). */
public final class NtmWorldBridge {
    private NtmWorldBridge() {}

    // Weak holders do not keep an unloaded dimension/chunk alive across server sessions.
    private static final Map<ServerLevel, RuntimeState> LEVELS = new WeakHashMap<>();
    private static final class RuntimeState {
        final Map<Long, WeakReference<LevelChunk>> chunks = new HashMap<>();
        final ArrayDeque<Effect> effects = new ArrayDeque<>();
        boolean clearRecovered;
    }
    private record Effect(int chunkX, int chunkZ, NtmWorldField.Section section, int pocket) {}

    private static RuntimeState runtime(ServerLevel level) {
        if (!level.getServer().isSameThread()) throw new IllegalStateException("NTM world adapter requires the server thread");
        return LEVELS.computeIfAbsent(level, ignored -> new RuntimeState());
    }

    public static void loaded(ServerLevel level, LevelChunk chunk) {
        runtime(level).chunks.put(ChunkPos.pack(chunk.getPos().x(), chunk.getPos().z()), new WeakReference<>(chunk));
    }

    public static void unloaded(ServerLevel level, LevelChunk chunk) {
        runtime(level).chunks.remove(ChunkPos.pack(chunk.getPos().x(), chunk.getPos().z()));
    }

    private static NtmWorldState worldState(ServerLevel level) {
        RuntimeState runtime = runtime(level);
        NtmWorldState state = level.getData(NRAttachments.NTM_WORLD_STATE.get());
        if (!runtime.clearRecovered) {
            try {
                state.recoverClear(new NtmClearJournal(NtmLegacyWorldReader.dimensionDirectory(level)).latest());
                runtime.clearRecovered = true;
            } catch (IOException failure) {
                throw new UncheckedIOException("Cannot recover NTM world-clear watermark", failure);
            }
        }
        return state;
    }

    /** Attach a fresh, archived snapshot before any reader, writer or exposure can observe the chunk. */
    public static NtmWorldField field(ServerLevel level, LevelChunk chunk) {
        loaded(level, chunk);
        NtmWorldState state = worldState(level);
        NtmWorldField field = chunk.getData(NRAttachments.NTM_WORLD_FIELD.get());
        if (!field.migrated()) {
            // Native workers/persistence are stopped for the whole enabled session. The candidate
            // cannot evolve between capture and attachment, and the marker travels with the atoms.
            field = NtmLegacyWorldReader.prepare(level, chunk).candidate();
            state.synchronize(field);
            chunk.setData(NRAttachments.NTM_WORLD_FIELD.get(), field);
            chunk.markUnsaved();
        }
        if (state.synchronize(field)) chunk.markUnsaved();
        return field;
    }

    public static RadiationProfile profile(ServerLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) return RadiationProfile.empty();
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) return RadiationProfile.empty();
        RadiationProfile profile = field(level, chunk).profileAt(pos.getX(), pos.getY(), pos.getZ(), true);
        // Sample a detached profile between NR simulation steps. Instrument polling
        // must not change decay cadence or dirty every sampled chunk every tick.
        profile.advanceDecay(level.getGameTime());
        return profile;
    }

    public static void tick(ServerLevel level) {
        for (LevelChunk chunk : loadedChunks(level)) {
            if (!chunk.hasData(NRAttachments.NTM_WORLD_FIELD.get())) continue;
            var stored = chunk.getData(NRAttachments.NTM_WORLD_FIELD.get());
            if (stored.migrated() && field(level, chunk).advanceDecay(level.getGameTime())) chunk.markUnsaved();
        }
    }

    public static boolean mayExpose(LivingEntity entity) {
        if (entity instanceof net.minecraft.world.entity.player.Player player
                && (player.isCreative() || player.isSpectator())) return false;
        return Protection.mayHarm(entity.level(), entity, Protection.Source.NONE, Protection.Hazard.RADIATION);
    }

    public static void observeExposure(LivingEntity entity, double rawSv, double absorbedSv) {
        if (com.hbm.config.RadiationConfig.enableContamination && com.hbm.api.event.HbmEvents.irradiationObserved()) {
            double units = NtmCompatConfig.SV_PER_EXPOSURE_UNIT.get();
            com.hbm.api.event.HbmEvents.entityIrradiated(entity, "RADIATION", "CREATIVE", rawSv / units, absorbedSv / units);
        }
    }

    public static double reading(ServerLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) return 0;
        return NtmEmissionProfiles.configured().nativeWorldReading(
                RadiationFieldSampler.sample(level, pos.getCenter()).total());
    }

    private static NtmWorldField writable(ServerLevel level, LevelChunk chunk, BlockPos pos) {
        NtmWorldField field = field(level, chunk);
        int sectionY = pos.getY() >> 4;
        if (field.sections().stream().noneMatch(section -> section.sectionY() == sectionY)) {
            var geometry = NtmLegacyWorldReader.emptyGeometry(level, chunk, sectionY);
            field.addEmptySection(sectionY, geometry.pocketOf(), geometry.pockets().size());
        }
        return field;
    }

    public static void set(ServerLevel level, BlockPos pos, double value) {
        if (level.isOutsideBuildHeight(pos)) return;
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) return;
        writable(level, chunk, pos).set(pos.getX(), pos.getY(), pos.getZ(), value, true,
                NtmEmissionProfiles.configured(), level.getGameTime());
        chunk.markUnsaved();
    }

    public static void increment(ServerLevel level, BlockPos pos, double amount, double cap) {
        if (level.isOutsideBuildHeight(pos)) return;
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) return;
        writable(level, chunk, pos).increment(pos.getX(), pos.getY(), pos.getZ(), amount, cap, true,
                NtmEmissionProfiles.configured(), level.getGameTime());
        chunk.markUnsaved();
    }

    public static void clear(ServerLevel level) {
        NtmWorldState state = worldState(level);
        long generation = Math.incrementExact(state.clearGeneration());
        try {
            new NtmClearJournal(NtmLegacyWorldReader.dimensionDirectory(level)).commit(generation);
        } catch (IOException failure) {
            throw new UncheckedIOException("Cannot persist NTM world clear; no live clear performed", failure);
        }
        state.recoverClear(generation);
        runtime(level).effects.clear();
        for (LevelChunk chunk : loadedChunks(level)) {
            var field = chunk.getData(NRAttachments.NTM_WORLD_FIELD.get());
            if (field.migrated() && state.synchronize(field)) chunk.markUnsaved();
        }
    }

    private static List<LevelChunk> loadedChunks(ServerLevel level) {
        List<LevelChunk> result = new ArrayList<>();
        var iterator = runtime(level).chunks.entrySet().iterator();
        while (iterator.hasNext()) {
            LevelChunk chunk = iterator.next().getValue().get();
            if (chunk == null || level.getChunkSource().getChunkNow(chunk.getPos().x(), chunk.getPos().z()) != chunk) {
                iterator.remove();
            } else result.add(chunk);
        }
        return result;
    }

    /** Fog's chunk prefilter uses owned pocket maxima plus live total-field samples. No chunk is loaded here. */
    public static void forEachChunkMax(ServerLevel level, RadiationSystemNT.ChunkMaxConsumer consumer) {
        for (LevelChunk chunk : loadedChunks(level)) {
            double max = 0;
            for (var section : field(level, chunk).sections()) {
                for (int index : representatives(section)) {
                    if (index >= 0) max = Math.max(max, reading(level, position(chunk, section, index)));
                }
            }
            // Other NR sources must still make an otherwise empty NTM chunk eligible for fog.
            for (var source : igentuman.nr.radiation.simulation.RadiationSimulator.get().indexFor(level).all()) {
                BlockPos pos = source.getPosition();
                if ((pos.getX() >> 4) == chunk.getPos().x() && (pos.getZ() >> 4) == chunk.getPos().z()) {
                    max = Math.max(max, reading(level, pos));
                }
            }
            var surface = new BlockPos(chunk.getPos().getMinBlockX() + 8,
                    chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, 8, 8), chunk.getPos().getMinBlockZ() + 8);
            max = Math.max(max, reading(level, surface));
            if (max > 0) consumer.accept(chunk.getPos().x(), chunk.getPos().z(), max);
        }
    }

    /** Called at the original simulation cadence, instead of prepare/diffusion/retention workers. */
    public static void queueWorldEffects(MinecraftServer server) {
        if (!RadiationConfig.masterEnabled() || !com.hbm.config.RadiationConfig.worldRadEffects) return;
        for (ServerLevel level : server.getAllLevels()) {
            var queue = runtime(level).effects;
            for (LevelChunk chunk : loadedChunks(level)) {
                for (var section : field(level, chunk).sections()) {
                    int[] representatives = representatives(section);
                    for (int pocket = 0; pocket < representatives.length; pocket++) {
                        int index = representatives[pocket];
                        if (index < 0 || queue.size() >= 512) continue;
                        if (reading(level, position(chunk, section, index)) >= 5.0 && level.getRandom().nextInt(100) == 0) {
                            queue.add(new Effect(chunk.getPos().x(), chunk.getPos().z(), section, pocket));
                        }
                    }
                }
            }
        }
    }

    /** Same native surface selection, 1/3 block rolls, protection gate and decayBlock implementation. */
    public static void drainWorldEffects(ServerLevel level) {
        var queue = runtime(level).effects;
        if (!RadiationConfig.masterEnabled()) { queue.clear(); return; }
        for (int budget = Math.max(1, com.hbm.config.RadiationConfig.worldRad); budget > 0 && !queue.isEmpty(); budget--) {
            Effect effect = queue.remove();
            LevelChunk chunk = level.getChunkSource().getChunkNow(effect.chunkX, effect.chunkZ);
            if (chunk == null) continue;
            var gate = Protection.gate(level, Protection.Source.NONE, Protection.Cause.RADIATION_BLOCK_CONVERSION);
            if (gate.coverage(effect.chunkX, effect.chunkZ) == Protection.Coverage.CLOSED) continue;
            for (int index = 0; index < 4096; index++) {
                if (level.getRandom().nextInt(3) != 0 || effect.section.pocketOf().get(index) != effect.pocket) continue;
                int height = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, index & 15, (index >> 4) & 15);
                BlockPos pos = position(chunk, effect.section, index);
                if (pos.getY() < height - 1 || pos.getY() > height) continue;
                var block = level.getBlockState(pos);
                if (!block.isAir() && gate.mayAlter(pos)) NtmLegacyWorldReader.decayBlock(level, pos, block);
            }
        }
    }

    private static int[] representatives(NtmWorldField.Section section) {
        int[] result = new int[section.pockets().size()];
        java.util.Arrays.fill(result, -1);
        for (int i = 0; i < 4096; i++) {
            int pocket = section.pocketOf().get(i);
            if (pocket >= 0 && result[pocket] < 0) result[pocket] = i;
        }
        return result;
    }

    private static BlockPos position(LevelChunk chunk, NtmWorldField.Section section, int index) {
        return new BlockPos(chunk.getPos().getMinBlockX() + (index & 15), (section.sectionY() << 4) + (index >> 8),
                chunk.getPos().getMinBlockZ() + ((index >> 4) & 15));
    }
}
