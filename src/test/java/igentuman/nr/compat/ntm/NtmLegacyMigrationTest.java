package igentuman.nr.compat.ntm;

import com.mojang.serialization.JsonOps;
import igentuman.nr.registry.DefaultIsotopes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NtmLegacyMigrationTest {
    @TempDir Path directory;

    @BeforeEach
    void registerProfiles() {
        for (var definition : NtmEmissionProfiles.defaults()) DefaultIsotopes.apply(definition);
    }

    @Test
    void readsPinnedBigEndianLayoutAndPreservesRelativeIndices() {
        byte[] bytes = ByteBuffer.allocate(45).put((byte) 1).putInt(2)
                .putInt(0).putInt(1).putDouble(12.5)
                .putInt(23).putInt(2).putDouble(2).putDouble(7).array();
        var decoded = NtmLegacyPayload.decode(bytes);
        assertArrayEquals(new double[]{12.5}, decoded.get(0));
        assertArrayEquals(new double[]{2, 7}, decoded.get(23));
        assertArrayEquals(bytes, NtmLegacyPayload.encode(decoded));
        assertEquals(0xa430d84680aabd0bL, NtmLegacyPayload.fingerprint("hello".getBytes(StandardCharsets.UTF_8)));
        assertEquals(0L, NtmLegacyPayload.fingerprint(null));
    }

    @Test
    void emptyAndMissingHaveDifferentMeaningFromCorrupt() {
        assertTrue(NtmLegacyPayload.decode(null).isEmpty());
        assertNull(NtmLegacyPayload.encode(Map.of()));
        for (byte[] invalid : List.of(new byte[0], new byte[]{1}, new byte[]{1, 0, 0, 0, 0},
                new byte[]{2, 0, 0, 0, 1})) {
            assertThrows(IllegalArgumentException.class, () -> NtmLegacyPayload.decode(invalid));
        }
        assertTrue(NtmLegacyPayload.decode(ByteBuffer.allocate(13).put((byte) 1).putInt(1)
                .putInt(2).putInt(0).array()).containsKey(2));
    }

    @Test
    void rejectsEveryTruncatedPrefixAndTrailingGarbage() {
        byte[] valid = NtmLegacyPayload.encode(Map.of(5, new double[]{2.5, 9}));
        for (int length = 0; length < valid.length; length++) {
            byte[] prefix = Arrays.copyOf(valid, length);
            assertThrows(IllegalArgumentException.class, () -> NtmLegacyPayload.decode(prefix));
        }
        assertThrows(IllegalArgumentException.class, () -> NtmLegacyPayload.decode(Arrays.copyOf(valid, valid.length + 1)));
    }

    @Test
    void rejectsDuplicateRowsAndOutOfBoundsBeforeAllocation() {
        byte[] duplicate = ByteBuffer.allocate(21).put((byte) 1).putInt(2)
                .putInt(0).putInt(0).putInt(0).putInt(0).array();
        assertThrows(IllegalArgumentException.class, () -> NtmLegacyPayload.decode(duplicate));
        for (int[] limits : List.of(new int[]{-1, 1}, new int[]{1024, 1}, new int[]{0, -1}, new int[]{0, 4097})) {
            byte[] invalid = ByteBuffer.allocate(13).put((byte) 1).putInt(1).putInt(limits[0]).putInt(limits[1]).array();
            assertThrows(IllegalArgumentException.class, () -> NtmLegacyPayload.decode(invalid));
        }
        for (int count : new int[]{-1, 0, 1025, Integer.MAX_VALUE}) {
            byte[] invalid = ByteBuffer.allocate(5).put((byte) 1).putInt(count).array();
            assertThrows(IllegalArgumentException.class, () -> NtmLegacyPayload.decode(invalid));
        }
    }

    @Test
    void archiveKeepsOriginalScalarsWhileConversionUsesNativeSanitization() {
        double[] raw = {Double.NaN, -2, 0.000001, Double.POSITIVE_INFINITY, 7};
        byte[] payload = NtmLegacyPayload.encode(Map.of(0, raw));
        assertArrayEquals(new double[]{0, 0, 0, NtmWorldField.MAX_RADIATION, 7}, NtmLegacyPayload.decode(payload).get(0));
        ByteBuffer bytes = ByteBuffer.wrap(payload);
        assertTrue(Double.isNaN(bytes.getDouble(13)));
        assertEquals(-2, bytes.getDouble(21));
        assertEquals(Double.POSITIVE_INFINITY, bytes.getDouble(37));
    }

    @Test
    void capturesTopologyWithoutAliasingAndBuildsPersistableDetachedCandidate() {
        List<Integer> geometry = new ArrayList<>(Collections.nCopies(4096, 0));
        geometry.set(1, 1);
        geometry.set(2, -1);
        List<Double> densities = new ArrayList<>(List.of(3.0, 7.0));
        var section = new NtmLegacyPayload.Section(-4, geometry, densities);
        geometry.set(0, -1);
        densities.set(0, 999.0);
        var calibration = new NtmEmissionProfiles.Calibration(8e12, Map.of(NtmEmissionProfiles.FALLOUT, 1.0));
        var field = NtmLegacyPayload.candidate(List.of(section), 321, 100, calibration);
        assertEquals(3.0, field.ownedAt(0, -64, 0, true, calibration), 1e-10);
        assertEquals(7.0, field.ownedAt(1, -64, 0, true, calibration), 1e-10);
        assertEquals(0.0, field.ownedAt(2, -64, 0, true, calibration));
        assertEquals(321, field.legacyFingerprint());
        assertEquals(100, field.importTick());
        var json = NtmWorldField.CODEC.encodeStart(JsonOps.INSTANCE, field).getOrThrow();
        var restored = NtmWorldField.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertFalse(restored.importOnce(List.of(), 999, 101));
        assertEquals(7.0, restored.ownedAt(1, -64, 0, true, calibration), 1e-10);
    }

    @Test
    void invalidGeometryOrProfilesCannotProduceAnImportCandidate() {
        assertThrows(IllegalArgumentException.class, () -> new NtmLegacyPayload.Section(0, List.of(0), List.of(1.0)));
        assertThrows(IllegalArgumentException.class, () -> new NtmLegacyPayload.Section(0,
                Collections.nCopies(4096, 1), List.of(1.0)));
        var section = new NtmLegacyPayload.Section(0, Collections.nCopies(4096, 0), List.of(1.0));
        var calibration = new NtmEmissionProfiles.Calibration(8e12, Map.of(NtmEmissionProfiles.FALLOUT, 1.0));
        assertThrows(IllegalArgumentException.class, () -> NtmLegacyPayload.candidate(List.of(section, section), 1, 0, calibration));
        var missing = new NtmEmissionProfiles.Calibration(8e12, Map.of("missing:ntm", 1.0));
        assertThrows(IllegalArgumentException.class, () -> NtmLegacyPayload.candidate(List.of(section), 1, 0, missing));
        var overflow = new NtmLegacyPayload.Section(0, section.pocketOf(), List.of(Double.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> NtmLegacyPayload.candidate(List.of(overflow), 1, 0, calibration));
    }

    @Test
    void recoveryObjectsSurviveRetryAndNewArchiveInstance() throws IOException {
        var archive = new NtmMigrationArchive(directory);
        byte[] original = {1, 2, 3, 4};
        Path retained = archive.retain(original);
        assertEquals(retained, archive.retain(original));
        assertArrayEquals(original, new NtmMigrationArchive(directory).read(NtmMigrationArchive.digest(original)));
        assertNotEquals(retained, archive.retain(new byte[]{1, 2, 3, 5}));
        assertArrayEquals(original, Files.readAllBytes(retained));
        try (var files = Files.list(retained.getParent())) {
            assertEquals(2, files.count());
        }
    }

    @Test
    void corruptedRecoveryObjectIsNeverAcceptedOrOverwritten() throws IOException {
        var archive = new NtmMigrationArchive(directory);
        byte[] original = {4, 5, 6};
        Path retained = archive.retain(original);
        Files.write(retained, new byte[]{9});
        assertThrows(IOException.class, () -> archive.retain(original));
        assertThrows(IOException.class, () -> archive.read(NtmMigrationArchive.digest(original)));
        assertArrayEquals(new byte[]{9}, Files.readAllBytes(retained));
        assertThrows(IllegalArgumentException.class, () -> archive.read("../anything"));
    }

    @Test
    void archiveFailureLeavesTheOriginalFileAvailableForRetry() throws IOException {
        Path nativeFile = directory.resolve("hbm_radiation.dat");
        byte[] original = {1, 2, 3};
        Files.write(nativeFile, original);
        Files.write(directory.resolve("nuclear_radiation"), new byte[]{0});
        var archive = new NtmMigrationArchive(directory);
        assertThrows(IOException.class, () -> archive.retain(Files.readAllBytes(nativeFile)));
        assertArrayEquals(original, Files.readAllBytes(nativeFile));
    }
}
