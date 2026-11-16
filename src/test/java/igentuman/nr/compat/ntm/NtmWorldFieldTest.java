package igentuman.nr.compat.ntm;

import com.google.gson.JsonObject;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.serialization.JsonOps;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.config.GeneralConfig;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.registry.DefaultIsotopes;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class NtmWorldFieldTest {
    private NtmEmissionProfiles.Calibration calibration;

    @BeforeAll
    static void loadDecayConfiguration() throws ReflectiveOperationException {
        loadDefaults(GeneralConfig.SPEC);
        loadDefaults(RadiationConfig.SPEC);
    }

    private static void loadDefaults(ModConfigSpec spec) throws ReflectiveOperationException {
        CommentedConfig config = CommentedConfig.inMemory();
        spec.correct(config);
        // FML's loaded-config implementation is package-private and its interface sealed.
        // Supply corrected in-memory defaults through its ordinary constructor; no game boot or disk writes.
        var constructor = Class.forName("net.neoforged.fml.config.LoadedConfig")
                .getDeclaredConstructor(CommentedConfig.class, Path.class, ModConfig.class);
        constructor.setAccessible(true);
        spec.acceptConfig((IConfigSpec.ILoadedConfig) constructor.newInstance(config, null, null));
    }

    @AfterAll
    static void unloadDecayConfiguration() {
        GeneralConfig.SPEC.acceptConfig(null);
        RadiationConfig.SPEC.acceptConfig(null);
    }

    @BeforeEach
    void registerProfiles() {
        for (var definition : NtmEmissionProfiles.defaults()) DefaultIsotopes.apply(definition);
        calibration = new NtmEmissionProfiles.Calibration(8.0e12, Map.of(NtmEmissionProfiles.FALLOUT, 1.0));
    }

    private NtmWorldField field(double first, double second) {
        NtmWorldField field = new NtmWorldField();
        List<Integer> geometry = new ArrayList<>(Collections.nCopies(4096, 0));
        geometry.set(1, 1);
        geometry.set(2, -1);
        assertTrue(field.importOnce(List.of(new NtmWorldField.Section(-1, geometry, List.of(
                NtmWorldField.Pocket.of(calibration.profile(first, 0)),
                NtmWorldField.Pocket.of(calibration.profile(second, 0))))), 1234L, 0L));
        return field;
    }

    private double owned(NtmWorldField field, int x) {
        return field.ownedAt(x, -16, 0, true, calibration);
    }

    @Test
    void newSectionGeometryDoesNotReplaceImportedReleasesAndRejectsInvalidTopology() {
        var field = field(10, 20);
        var original = field.sections();
        field.addEmptySection(-1, Collections.nCopies(4096, 0), 1);
        assertEquals(original, field.sections());
        field.addEmptySection(18, Collections.nCopies(4096, 0), 1);
        field.set(0, 300, 0, 7, true, calibration, 0);
        assertEquals(7, field.ownedAt(0, 300, 0, true, calibration), 1e-10);
        assertEquals(10, owned(field, 0), 1e-10);
        assertThrows(IllegalArgumentException.class, () -> field.addEmptySection(19, List.of(0), 1));
        assertEquals(2, field.sections().size());
    }

    @Test
    void physicalVolumeSamplingUsesQualityAndProtectionOnceWithoutDisplayGain() {
        var field = field(10, 20);
        var rate = igentuman.nr.radiation.simulation.RadiationFieldSampler.profile(field.profileAt(0, -16, 0, true));
        assertEquals(0.008, rate.total(), 1e-12);
        var armor = new igentuman.nr.api.shielding.ArmorProtectionRegistry.Protection(0.75, 0, 0, 0, false);
        assertEquals(0.002, rate.protectedRate(armor), 1e-12);
        assertTrue(field.advanceDecay(12000));
        assertFalse(field.advanceDecay(12000));
        assertTrue(igentuman.nr.radiation.simulation.RadiationFieldSampler.profile(
                field.profileAt(0, -16, 0, true)).total() < rate.total());
    }

    @Test
    void sourceIdentityIsStableAndIncludesDimensionChunkSectionAndPocket() {
        var id = NtmWorldField.sourceId("minecraft:overworld", -2, 3, -1, 0);
        assertEquals(id, NtmWorldField.sourceId("minecraft:overworld", -2, 3, -1, 0));
        assertNotEquals(id, NtmWorldField.sourceId("minecraft:the_nether", -2, 3, -1, 0));
        assertNotEquals(id, NtmWorldField.sourceId("minecraft:overworld", -1, 3, -1, 0));
        assertNotEquals(id, NtmWorldField.sourceId("minecraft:overworld", -2, 4, -1, 0));
        assertNotEquals(id, NtmWorldField.sourceId("minecraft:overworld", -2, 3, 0, 0));
        assertNotEquals(id, NtmWorldField.sourceId("minecraft:overworld", -2, 3, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> NtmWorldField.sourceId("invalid", 0, 0, 0, 0));
    }

    @Test
    void preservesPocketGeometryAndNegativeSectionCoordinates() {
        NtmWorldField field = field(10, 20);
        assertEquals(10, owned(field, 0), 1.0e-10);
        assertEquals(20, owned(field, 1), 1.0e-10);
        assertEquals(0, owned(field, 2)); // Wall.
        assertEquals(10, field.ownedAt(-1, -1, -1, true, calibration), 1.0e-10);
        assertEquals(0, field.ownedAt(0, 0, 0, true, calibration)); // Different section.
        field.set(2, -16, 0, 50, true, calibration, 0);
        assertEquals(0, owned(field, 2));
    }

    @Test
    void writesPreserveNativeCapAndNegativeIncrementSemantics() {
        NtmWorldField field = field(10, 20);
        field.increment(0, -16, 0, 8, 15, true, calibration, 0);
        assertEquals(15, owned(field, 0), 1.0e-10);
        field.increment(0, -16, 0, 8, 5, true, calibration, 0);
        assertEquals(15, owned(field, 0), 1.0e-10); // A positive increment must not lower an existing value above cap.
        field.increment(0, -16, 0, -1, 5, true, calibration, 0);
        assertEquals(5, owned(field, 0), 1.0e-10); // Native applies cap to negative changes too.
        field.decrement(0, -16, 0, 2, true, calibration, 0);
        assertEquals(3, owned(field, 0), 1.0e-10);
        field.decrement(0, -16, 0, 100, true, calibration, 0);
        assertEquals(0, owned(field, 0));
        field.set(0, -16, 0, -20, true, calibration, 0);
        assertEquals(0, owned(field, 0));
        assertEquals(20, owned(field, 1), 1.0e-10);
        field.set(0, -16, 0, 10, true, calibration, 0);
        field.increment(0, -16, 0, 1, 10.000001, true, calibration, 0);
        assertEquals(10.000001, owned(field, 0), 1.0e-10);
    }

    @Test
    void unloadedChunksCannotBeReadOrMutatedAndMissingGeometryIsNotInvented() {
        NtmWorldField field = field(10, 20);
        field.set(0, -16, 0, 99, false, calibration, 0);
        field.increment(0, -16, 0, 99, 100, false, calibration, 0);
        field.decrement(0, -16, 0, 99, false, calibration, 0);
        assertEquals(10, owned(field, 0), 1.0e-10);
        assertEquals(0, field.ownedAt(0, -16, 0, false, calibration));
        field.set(0, 16, 0, 99, true, calibration, 0);
        assertEquals(0, field.ownedAt(0, 16, 0, true, calibration));
        assertEquals(1, field.sections().size());
    }

    @Test
    void ownedMutationsCannotEraseOtherNrProfilesOrAnotherChunksField() {
        NtmWorldField field = field(10, 20);
        NtmWorldField otherChunk = field(7, 8);
        RadiationProfile unrelated = calibration.profile(40, 0);
        double unrelatedBq = unrelated.totalActivityBq();
        double total = NtmEmissionProfiles.unshieldedSvPerSecond(unrelated)
                + NtmEmissionProfiles.unshieldedSvPerSecond(field.profileAt(0, -16, 0, true));
        assertEquals(50, calibration.nativeWorldReading(total), 1.0e-10);
        field.decrement(0, -16, 0, 5, true, calibration, 0);
        assertEquals(5, owned(field, 0), 1.0e-10); // Not total(50) - 5.
        field.clear();
        assertEquals(0, owned(field, 0));
        assertEquals(7, owned(otherChunk, 0), 1.0e-10);
        assertEquals(unrelatedBq, unrelated.totalActivityBq());
        assertTrue(field.migrated());
        assertFalse(field.importOnce(List.of(), 9999, 10));
    }

    @Test
    void codecPersistsMarkerProvenanceGeometryAndAtomsAsOneState() {
        NtmWorldField field = field(10, 20);
        var encoded = NtmWorldField.CODEC.encodeStart(JsonOps.INSTANCE, field).getOrThrow();
        NtmWorldField loaded = NtmWorldField.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertTrue(loaded.migrated());
        assertEquals(1234L, loaded.legacyFingerprint());
        assertEquals(0L, loaded.importTick());
        assertEquals(field.sections(), loaded.sections());
        assertFalse(loaded.importOnce(List.of(), 9999, 5));
        assertEquals(20, owned(loaded, 1), 1.0e-10);
        loaded.clear();
        loaded = NtmWorldField.CODEC.parse(JsonOps.INSTANCE,
                NtmWorldField.CODEC.encodeStart(JsonOps.INSTANCE, loaded).getOrThrow()).getOrThrow();
        assertTrue(loaded.migrated());
        assertFalse(loaded.importOnce(field.sections(), 1234, 10));
        assertEquals(0, owned(loaded, 0));
    }

    @Test
    void worldClearPersistsAndReachesLaterLoadedChunksWithoutErasingNewEmissions() {
        NtmWorldState dimension = new NtmWorldState();
        NtmWorldState otherDimension = new NtmWorldState();
        NtmWorldField loadedChunk = field(10, 20);
        NtmWorldField unloadedChunk = field(30, 40);
        assertFalse(dimension.synchronize(loadedChunk));
        dimension.clearWorld();
        assertTrue(dimension.synchronize(loadedChunk));
        assertEquals(0, owned(loadedChunk, 0));
        dimension = NtmWorldState.CODEC.parse(JsonOps.INSTANCE,
                NtmWorldState.CODEC.encodeStart(JsonOps.INSTANCE, dimension).getOrThrow()).getOrThrow();
        assertEquals(1, dimension.clearGeneration());
        assertTrue(dimension.synchronize(unloadedChunk));
        assertEquals(0, owned(unloadedChunk, 0));
        assertEquals(1234, unloadedChunk.legacyFingerprint());
        assertTrue(unloadedChunk.migrated());
        unloadedChunk.set(0, -16, 0, 6, true, calibration, 0);
        unloadedChunk = NtmWorldField.CODEC.parse(JsonOps.INSTANCE,
                NtmWorldField.CODEC.encodeStart(JsonOps.INSTANCE, unloadedChunk).getOrThrow()).getOrThrow();
        assertFalse(dimension.synchronize(unloadedChunk));
        assertEquals(6, owned(unloadedChunk, 0), 1.0e-10);
        assertFalse(otherDimension.synchronize(unloadedChunk));
        assertEquals(6, owned(unloadedChunk, 0), 1.0e-10);
        assertThrows(IllegalStateException.class, () -> new NtmWorldState().synchronize(new NtmWorldField()));
    }

    @Test
    void rejectsIncompleteOrMalformedSnapshotsWithoutMarkingImportComplete() {
        NtmWorldField field = new NtmWorldField();
        assertThrows(IllegalArgumentException.class, () -> field.importOnce(List.of(
                new NtmWorldField.Section(0, List.of(0), List.of())), 1, 0));
        assertFalse(field.migrated());
        assertTrue(field.sections().isEmpty());
        List<Integer> geometry = Collections.nCopies(4096, 0);
        NtmWorldField.Pocket overflowing = new NtmWorldField.Pocket(List.of(
                new NtmWorldField.Atom(NtmEmissionProfiles.FALLOUT, Double.MAX_VALUE, 0),
                new NtmWorldField.Atom(NtmEmissionProfiles.BALEFIRE, Double.MAX_VALUE, 0)));
        // Each atom count is finite, but sum activity need not be. A deliberately short-lived
        // registry definition exercises the migration guard without accepting partial state.
        igentuman.nr.api.isotope.IsotopeBuilder.create(NtmEmissionProfiles.FALLOUT).xray(1).halfLife(1).register();
        try {
            assertThrows(IllegalArgumentException.class, () -> field.importOnce(List.of(
                    new NtmWorldField.Section(0, geometry, List.of(overflowing))), 1, 0));
            assertFalse(field.migrated());
        } finally {
            registerProfiles();
        }
        var encoded = NtmWorldField.CODEC.encodeStart(JsonOps.INSTANCE, field(10, 20)).getOrThrow().getAsJsonObject();
        encoded.getAsJsonArray("sections").get(0).getAsJsonObject().getAsJsonArray("pocket_of").set(0,
                new com.google.gson.JsonPrimitive(200));
        assertTrue(NtmWorldField.CODEC.parse(JsonOps.INSTANCE, encoded).error().isPresent());
        assertFalse(NtmWorldField.CODEC.parse(JsonOps.INSTANCE, new JsonObject()).getOrThrow().migrated());
    }

    @Test
    void temporarilyMissingIsotopeSurvivesSerializationButCannotBeImportedOrSimulated() {
        NtmWorldField original = field(10, 20);
        var encoded = NtmWorldField.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        IsotopeRegistry.clear();
        try {
            NtmWorldField loaded = NtmWorldField.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
            assertEquals(original.sections(), loaded.sections());
            assertThrows(IllegalArgumentException.class, () -> owned(loaded, 0));
            NtmWorldField candidate = new NtmWorldField();
            assertThrows(IllegalArgumentException.class, () -> candidate.importOnce(loaded.sections(), 1234, 0));
            assertFalse(candidate.migrated());
        } finally {
            registerProfiles();
        }
    }

    @Test
    void profileActivityWeightsAndPhysicalRateHaveIndependentReversibleCalibration() {
        var mixed = new NtmEmissionProfiles.Calibration(1000, Map.of(
                NtmEmissionProfiles.FALLOUT, 3.0, NtmEmissionProfiles.SOLINIUM, 1.0));
        RadiationProfile profile = mixed.profile(2, 500);
        assertEquals(2000, profile.totalActivityBq(), 1.0e-10);
        assertEquals(1500, profile.get(NtmEmissionProfiles.FALLOUT).currentActivityBq(), 1.0e-10);
        assertEquals(500, profile.get(NtmEmissionProfiles.SOLINIUM).currentActivityBq(), 1.0e-10);
        assertEquals(2, mixed.nativeUnits(profile), 1.0e-10);
        assertEquals(2, mixed.nativeWorldReading(NtmEmissionProfiles.unshieldedSvPerSecond(profile)), 1.0e-10);
        assertEquals(0.008, NtmEmissionProfiles.unshieldedSvPerSecond(calibration.profile(1, 0)), 1.0e-12);
        assertThrows(IllegalArgumentException.class, () -> new NtmEmissionProfiles.Calibration(0, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new NtmEmissionProfiles.Calibration(1, Map.of("unknown:id", Double.NaN)));
        assertThrows(IllegalArgumentException.class, () -> new NtmEmissionProfiles.Calibration(1, Map.of("unknown:id", 1.0)).profile(1, 0));
        assertEquals(0, calibration.nativeWorldReading(Double.NaN));
    }

    @Test
    void incrementalWritesRetainExistingCompositionAndDecrementScalesEveryIsotope() {
        var material = new NtmEmissionProfiles.Calibration(8.0e12, Map.of(NtmEmissionProfiles.SCHRABIDIUM, 1.0));
        NtmWorldField field = new NtmWorldField();
        RadiationProfile original = material.profile(10, 0);
        assertTrue(field.importOnce(List.of(new NtmWorldField.Section(0,
                Collections.nCopies(4096, 0), List.of(NtmWorldField.Pocket.of(original)))), 10, 0));
        field.increment(0, 0, 0, 10, 100, true, calibration, 0);
        RadiationProfile added = field.profileAt(0, 0, 0, true);
        assertEquals(original.get(NtmEmissionProfiles.SCHRABIDIUM).atoms(),
                added.get(NtmEmissionProfiles.SCHRABIDIUM).atoms());
        assertEquals(8.0e13, added.get(NtmEmissionProfiles.FALLOUT).currentActivityBq(), 1.0e-2);
        field.decrement(0, 0, 0, 10, true, calibration, 0);
        RadiationProfile reduced = field.profileAt(0, 0, 0, true);
        for (var stack : added.stacks()) {
            assertEquals(stack.atoms() / 2, reduced.get(stack.isotope().id()).atoms());
        }
    }

    @Test
    void releaseDecaysOnceAndReadsOrSmallRepeatedEmissionsDoNotRefreshOldAtoms() {
        NtmWorldField field = field(10, 20);
        field.advanceDecay(12000);
        // NR's current policy applies a 4.5 multiplier to its configured isotope decay speed.
        double remaining = 10 * Math.pow(0.5, 4.5);
        assertEquals(remaining, owned(field, 0), 1.0e-10);
        field.advanceDecay(12000);
        assertEquals(remaining, owned(field, 0), 1.0e-10);
        field.increment(0, -16, 0, 1, 100, true, calibration, 12000);
        field.advanceDecay(24000);
        assertEquals((remaining + 1) * Math.pow(0.5, 4.5), owned(field, 0), 1.0e-10);
        var reloaded = NtmWorldField.CODEC.parse(JsonOps.INSTANCE,
                NtmWorldField.CODEC.encodeStart(JsonOps.INSTANCE, field).getOrThrow()).getOrThrow();
        reloaded.advanceDecay(24000);
        assertEquals(owned(field, 0), owned(reloaded, 0));
    }

    @Test
    void epsilonAndNonFiniteInputsMatchNativeSanitizationWithoutPoisoningStorage() {
        NtmWorldField field = field(10, 20);
        field.increment(0, -16, 0, Double.NaN, 100, true, calibration, 0);
        field.increment(0, -16, 0, 1, Double.NaN, true, calibration, 0);
        field.increment(0, -16, 0, 1.0e-6, 100, true, calibration, 0);
        assertEquals(10, owned(field, 0), 1.0e-10);
        assertEquals(NtmWorldField.MAX_RADIATION, NtmWorldField.sanitize(Double.POSITIVE_INFINITY));
        field.set(0, -16, 0, Double.NaN, true, calibration, 0);
        assertEquals(0, owned(field, 0));
        field.set(0, -16, 0, 1.0e-6, true, calibration, 0);
        assertEquals(0, owned(field, 0));
        assertThrows(IllegalArgumentException.class,
                () -> field.set(0, -16, 0, Double.POSITIVE_INFINITY, true, calibration, 0));
        assertEquals(0, owned(field, 0));
    }
}
