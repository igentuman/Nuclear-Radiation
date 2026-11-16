package igentuman.nr.compat.ntm;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class NtmCompatibilityTest {
    @Test
    void doseAndRateUseDifferentConversions() {
        assertEquals(8.0, NtmUnits.doseToSv(1000.0, 0.008), 1.0e-12);
        assertEquals(200.0, NtmUnits.doseFromSv(1.6, 0.008), 1.0e-12);
        double displayedRate = 0.002 * 3600.0 * NtmUnits.NR_RATE_GAIN;
        assertEquals(1.0, NtmUnits.exposureFromDisplayedRate(displayedRate, 0.002), 1.0e-12);
        assertEquals(0.002, NtmUnits.incrementToSv(1, 0.008, 0.002), 1.0e-12);
        assertEquals(-0.008, NtmUnits.incrementToSv(-1, 0.008, 0.002), 1.0e-12);
    }

    @Test
    void logarithmicResistanceComposesMultiplicatively() {
        double a = NtmUnits.resistanceProtection(0.5);
        double b = NtmUnits.resistanceProtection(0.7);
        assertEquals(NtmUnits.resistanceProtection(1.2), 1 - (1 - a) * (1 - b), 1.0e-12);
        assertEquals(0.0, NtmUnits.resistanceProtection(-10));
        assertEquals(0.0, NtmUnits.resistanceProtection(Double.NaN));
    }

    @Test
    void nonFiniteInputsCannotPoisonDose() {
        assertEquals(0, NtmUnits.doseToSv(Double.NaN, 0.008));
        assertEquals(0, NtmUnits.incrementToSv(Double.POSITIVE_INFINITY, 0.008, 0.008));
        assertEquals(0, NtmUnits.doseFromSv(-1, 0.008));
        assertThrows(IllegalArgumentException.class, () -> NtmUnits.doseFromSv(1, 0));
        assertThrows(IllegalArgumentException.class, () -> NtmUnits.doseToSv(1, Double.NaN));
    }

    @Test
    void migrationSurvivesSaveReloadAndCannotReimportMirroredDose() {
        NtmEntityState state = NtmEntityState.CODEC.parse(JsonOps.INSTANCE, new JsonObject()).getOrThrow();
        assertEquals(450, state.migrate(450));
        state.recordExposure(0.5);
        var encoded = NtmEntityState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow();
        NtmEntityState loaded = NtmEntityState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertTrue(loaded.migrated());
        assertEquals(450, loaded.importedDose());
        assertEquals(0, loaded.migrate(1500));
        assertEquals(0, loaded.drainExposure()); // A reload must never count exposure twice.
        assertEquals(0, loaded.drainAmbientExposure());
        state.recordAmbientCorrection(0.25);
        assertEquals(0.75, state.drainAmbientExposure());
        assertEquals(0.5, state.drainExposure());
        assertEquals(0, state.drainExposure());
    }

    @Test
    void missingOptionalTargetsAreSkippedWithoutLoadingThem() {
        ClassLoader absent = new ClassLoader(null) {
            @Override public URL getResource(String name) { return null; }
            @Override public Class<?> loadClass(String name) {
                throw new AssertionError("Optional-target detection tried to load " + name);
            }
        };
        assertFalse(NtmMixinPlugin.targetAvailable(absent, "com.hbm.capability.HbmLivingProps"));
        assertFalse(NtmMixinPlugin.targetAvailable(absent, "com.ntmspace.fork.neutron.NeutronActivation"));
    }
}
