package igentuman.nr.compat.ntm;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hbm.handler.radiation.RadiationSavedData;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.mojang.serialization.JsonOps;
import dev.golem.core.world.ChunkSidecar;
import igentuman.nr.NuclearRadiation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

/**
 * Pinned-NTM-only migration preparation. Called behind the compatibility gate, never from attachment codecs.
 * Reflection is confined to NTM's package-private capture/topology and block-conversion APIs.
 * Preparing a detached candidate does not switch ownership or attach an import marker to a live chunk.
 */
public final class NtmLegacyWorldReader {
    private NtmLegacyWorldReader() {}

    public record Prepared(NtmWorldField candidate, Path recoveryRecord) {}

    static Path dimensionDirectory(ServerLevel level) {
        return DimensionType.getStorageFolder(level.dimension(), level.getServer().getWorldPath(LevelResource.ROOT));
    }

    /** Build only native topology for a new release; never write a native scalar. */
    static NtmWorldField.Section emptyGeometry(ServerLevel level, LevelChunk chunk, int sectionY) {
        requireServerThread(level);
        try {
            Object world = NativeAccess.call(NativeAccess.GET, null, level);
            Object nativeChunk = NativeAccess.call(NativeAccess.CHUNK_AT, world, chunk.getPos().x(), chunk.getPos().z(), true);
            int index = sectionY - level.getMinSectionY();
            if (index < 0 || index >= level.getSectionsCount()) throw new IllegalArgumentException("Section outside dimension");
            NativeAccess.call(NativeAccess.BUILD, world, nativeChunk, index);
            Object section = ((Object[]) NativeAccess.SECTIONS.get(nativeChunk))[index];
            short[] mapping = (short[]) NativeAccess.GEOMETRY.get(section);
            List<Integer> geometry = new ArrayList<>(4096);
            for (short pocket : mapping) geometry.add((int) pocket);
            int count = ((double[]) NativeAccess.DENSITY.get(section)).length;
            return new NtmWorldField.Section(sectionY, geometry,
                    Collections.nCopies(count, new NtmWorldField.Pocket(List.of())));
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Cannot build NTM release geometry", failure);
        }
    }

    /** Preserve NTM's data-map conversion outcomes and all block-specific fallback behavior. */
    static void decayBlock(ServerLevel level, BlockPos pos, BlockState state) {
        try {
            NativeAccess.call(NativeAccess.DECAY, null, level, pos, state);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Cannot apply NR-backed NTM world effect", failure);
        }
    }

    /** Retain old rows before enabled compatibility bypasses native migrateSavedData/drainTo. */
    public static void beforeSavedDataDrain(ServerLevel level) {
        requireServerThread(level);
        Path directory = dimensionDirectory(level);
        NtmMigrationArchive archive = new NtmMigrationArchive(directory);
        try {
            JsonObject record = record(level, "before_saved_data_drain");
            Path original = directory.resolve("data/hbm/hbm_radiation.dat");
            try {
                record.addProperty("original_saved_data", retain(archive, Files.readAllBytes(original)));
                // Retain the raw-file provenance before asking NTM's codec to load it.
                archive.retain(json(record));
            } catch (NoSuchFileException absent) {
                // New worlds legitimately have no old SavedData file. Other read failures must propagate.
            }
            // Preserve unsaved in-memory entries too. Do not call the destructive drain to enumerate them.
            Map<Long, double[][]> chunks = SavedDataAccess.chunks(RadiationSavedData.get(level));
            JsonArray entries = new JsonArray();
            for (var entry : new TreeMap<>(chunks).entrySet()) {
                Map<Integer, double[]> sections = new TreeMap<>();
                double[][] densities = entry.getValue();
                for (int i = 0; i < densities.length; i++) {
                    if (densities[i] != null) sections.put(i, densities[i]);
                }
                JsonObject chunk = new JsonObject();
                chunk.addProperty("chunk_key", entry.getKey());
                byte[] payload = NtmLegacyPayload.encode(sections);
                if (payload != null) chunk.addProperty("payload", retain(archive, payload));
                entries.add(chunk);
            }
            record.add("chunks", entries);
            if (!entries.isEmpty() || record.has("original_saved_data")) {
                Path retained = archive.retain(json(record));
                NuclearRadiation.LOGGER.info("Retained NTM pre-drain recovery record for {} ({} chunks): {}",
                        level.dimension(), entries.size(), retained);
            }
        } catch (IOException failure) {
            throw new UncheckedIOException("Cannot retain NTM legacy radiation before its destructive drain", failure);
        }
    }

    /**
     * Exercise the real native pending-read/build path without changing radiation ownership.
     * A later handoff must attach the returned candidate and retire the matching intake atomically.
     */
    public static Prepared prepare(ServerLevel level, LevelChunk chunk) {
        if (!NtmCompatibility.enabled()) throw new IllegalStateException("NTM compatibility is inactive");
        requireServerThread(level);
        if (chunk.getLevel() != level || level.getChunkSource().getChunkNow(chunk.getPos().x(), chunk.getPos().z()) != chunk) {
            throw new IllegalStateException("Cannot prepare an unloaded NTM radiation chunk");
        }
        NtmMigrationArchive archive = new NtmMigrationArchive(dimensionDirectory(level));
        try {
            Object world = NativeAccess.call(NativeAccess.GET, null, level);
            long key = ChunkPos.pack(chunk.getPos().x(), chunk.getPos().z());
            if (!((Map<?, ?>) NativeAccess.CHUNKS.get(world)).containsKey(key)) {
                // A loaded Minecraft chunk need not have a native entry (for example,
                // after native close/reopen). Schedule its persisted read before chunkAt
                // can create an apparently empty chunk and incorrectly prepare an empty import.
                RadiationSystemNT.onChunkLoaded(level, chunk);
            }
            Map<?, ?> pending = (Map<?, ?>) NativeAccess.PENDING.get(world);
            CompletableFuture<?> read = (CompletableFuture<?>) pending.get(key);
            byte[] stored = read == null ? ((ChunkSidecar) NativeAccess.SIDECAR.get(world)).read(chunk.getPos()).join()
                    : (byte[]) read.join();
            JsonObject record = record(level, "built_chunk_candidate");
            record.addProperty("chunk_x", chunk.getPos().x());
            record.addProperty("chunk_z", chunk.getPos().z());
            // In enabled sessions migrateSavedData is retained, rather than destructively
            // drained. Native migration gives these rows precedence over sidecar data too.
            double[][] legacy = SavedDataAccess.chunks(RadiationSavedData.get(level)).get(key);
            boolean fromSavedData = legacy != null;
            if (fromSavedData) {
                if (stored != null) record.addProperty("sidecar_payload", retain(archive, stored));
                Map<Integer, double[]> sections = new TreeMap<>();
                for (int i = 0; i < legacy.length; i++) if (legacy[i] != null) sections.put(i, legacy[i]);
                stored = NtmLegacyPayload.encode(sections);
                record.addProperty("source", "retained_saved_data");
            }
            if (stored != null) record.addProperty("original_payload", retain(archive, stored));
            record.addProperty("status", "source_retained");
            archive.retain(json(record));
            // Archive bytes BEFORE decoding, and reject corruption BEFORE native settle can discard it.
            Map<Integer, double[]> decoded = NtmLegacyPayload.decode(stored);
            for (int index : decoded.keySet()) {
                if (index >= level.getSectionsCount()) throw new IllegalArgumentException("Legacy NTM section outside dimension height");
            }
            if (fromSavedData) NativeAccess.supplyPending(world, key, stored);
            Object nativeChunk = NativeAccess.call(NativeAccess.CHUNK_AT, world, chunk.getPos().x(), chunk.getPos().z(), true);
            Object[] nativeSections = (Object[]) NativeAccess.SECTIONS.get(nativeChunk);
            List<NtmLegacyPayload.Section> snapshot = new ArrayList<>();
            Map<Integer, double[]> builtDensities = new TreeMap<>();
            JsonArray builtGeometry = new JsonArray();
            for (int i = 0; i < nativeSections.length; i++) {
                if (nativeSections[i] == null) continue;
                NativeAccess.call(NativeAccess.BUILD, world, nativeChunk, i);
                Object section = nativeSections[i];
                short[] geometry = (short[]) NativeAccess.GEOMETRY.get(section);
                double[] densities = ((double[]) NativeAccess.DENSITY.get(section)).clone();
                List<Integer> mapping = new ArrayList<>(4096);
                for (short pocket : geometry) mapping.add((int) pocket);
                snapshot.add(new NtmLegacyPayload.Section(level.getMinSectionY() + i, mapping,
                        Arrays.stream(densities).boxed().toList()));
                builtDensities.put(i, densities);
                JsonObject topology = new JsonObject();
                topology.addProperty("section_y", level.getMinSectionY() + i);
                JsonArray pockets = new JsonArray();
                for (int pocket : mapping) pockets.add(pocket);
                topology.add("pocket_of", pockets);
                builtGeometry.add(topology);
            }
            byte[] builtPayload = NtmLegacyPayload.encode(builtDensities);
            if (builtPayload != null) record.addProperty("built_payload", retain(archive, builtPayload));
            record.add("geometry", builtGeometry);
            // Keep scalar data AND its built mapping even if profile resolution/overflow rejects conversion.
            record.addProperty("status", "captured");
            archive.retain(json(record));
            NtmEmissionProfiles.Calibration calibration = NtmEmissionProfiles.configured();
            record.addProperty("bq_per_emission_unit", calibration.bqPerUnit());
            JsonObject composition = new JsonObject();
            calibration.composition().forEach(composition::addProperty);
            record.add("fallback_composition", composition);
            NtmWorldField candidate = NtmLegacyPayload.candidate(snapshot, NtmLegacyPayload.fingerprint(builtPayload),
                    level.getGameTime(), calibration);
            record.add("candidate", NtmWorldField.CODEC.encodeStart(JsonOps.INSTANCE, candidate).getOrThrow());
            record.addProperty("status", "prepared");
            return new Prepared(candidate, archive.retain(json(record)));
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Unsupported NTM radiation storage layout; no import committed", failure);
        } catch (IOException failure) {
            throw new UncheckedIOException("Cannot retain NTM migration candidate; no import committed", failure);
        }
    }

    private static void requireServerThread(ServerLevel level) {
        if (!level.getServer().isSameThread()) throw new IllegalStateException("NTM migration requires the server thread");
    }

    private static JsonObject record(ServerLevel level, String kind) {
        JsonObject result = new JsonObject();
        result.addProperty("format", 1);
        result.addProperty("kind", kind);
        result.addProperty("dimension", level.dimension().identifier().toString());
        result.addProperty("min_section_y", level.getMinSectionY());
        result.addProperty("tick", level.getGameTime());
        result.addProperty("ntm_version", "1.0.5");
        result.addProperty("ntm_file", "9524061");
        return result;
    }

    private static byte[] json(JsonObject record) {
        return record.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String retain(NtmMigrationArchive archive, byte[] payload) throws IOException {
        archive.retain(payload);
        return NtmMigrationArchive.digest(payload);
    }

    private static Field field(Class<?> owner, String name) throws ReflectiveOperationException {
        Field result = owner.getDeclaredField(name);
        result.setAccessible(true);
        return result;
    }

    private static Method method(Class<?> owner, String name, Class<?>... parameters) throws ReflectiveOperationException {
        Method result = owner.getDeclaredMethod(name, parameters);
        result.setAccessible(true);
        return result;
    }

    private static final class SavedDataAccess {
        private static final Field DATA;
        static {
            try {
                DATA = field(RadiationSavedData.class, "data");
            } catch (ReflectiveOperationException failure) {
                throw new ExceptionInInitializerError(failure);
            }
        }

        @SuppressWarnings("unchecked")
        private static Map<Long, double[][]> chunks(RadiationSavedData data) {
            try {
                return (Map<Long, double[][]>) DATA.get(data);
            } catch (IllegalAccessException failure) {
                throw new IllegalStateException("Cannot archive native NTM SavedData", failure);
            }
        }
    }

    private static final class NativeAccess {
        private static final Method GET, CHUNK_AT, BUILD, DECAY;
        private static final Field CHUNKS, PENDING, SIDECAR, SECTIONS, GEOMETRY, DENSITY;
        static {
            try {
                ClassLoader loader = RadiationSystemNT.class.getClassLoader();
                Class<?> world = Class.forName(RadiationSystemNT.class.getName() + "$WorldRadiationData", false, loader);
                Class<?> chunk = Class.forName(RadiationSystemNT.class.getName() + "$ChunkRad", false, loader);
                Class<?> section = Class.forName(RadiationSystemNT.class.getName() + "$SectionRad", false, loader);
                GET = method(RadiationSystemNT.class, "get", ServerLevel.class);
                CHUNK_AT = method(world, "chunkAt", int.class, int.class, boolean.class);
                BUILD = method(world, "ensureBuilt", chunk, int.class);
                DECAY = method(Class.forName("com.hbm.handler.radiation.RadiationWorldHandler", false, loader),
                        "decayBlock", ServerLevel.class, BlockPos.class, BlockState.class);
                CHUNKS = field(world, "chunks");
                PENDING = field(world, "pendingReads");
                SIDECAR = field(world, "sidecar");
                SECTIONS = field(chunk, "sections");
                GEOMETRY = field(section, "pocketOf");
                DENSITY = field(section, "density");
            } catch (ReflectiveOperationException failure) {
                throw new ExceptionInInitializerError(failure);
            }
        }

        private static Object call(Method method, Object owner, Object... arguments) throws ReflectiveOperationException {
            try {
                return method.invoke(owner, arguments);
            } catch (InvocationTargetException failure) {
                if (failure.getCause() instanceof RuntimeException runtime) throw runtime;
                if (failure.getCause() instanceof Error error) throw error;
                throw failure;
            }
        }

        @SuppressWarnings("unchecked")
        private static void supplyPending(Object world, long key, byte[] payload) throws IllegalAccessException {
            ((Map<Long, CompletableFuture<byte[]>>) PENDING.get(world)).put(key, CompletableFuture.completedFuture(payload));
        }
    }
}
