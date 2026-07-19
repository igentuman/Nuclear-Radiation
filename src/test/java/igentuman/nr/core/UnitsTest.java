package igentuman.nr.core;

import igentuman.nr.util.Units;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnitsTest {

    private static final double EPS = 1e-9;

    @Test
    void decayConstantFromHalfLife() {
        double lambda = Units.decayConstantFromHalfLifeTicks(1000);
        assertEquals(Units.LN2 / 1000.0, lambda, EPS);
    }

    @Test
    void decayConstantZeroHalfLifeReturnsZero() {
        assertEquals(0.0, Units.decayConstantFromHalfLifeTicks(0));
        assertEquals(0.0, Units.decayConstantFromHalfLifeTicks(-5));
    }

    @Test
    void atomsToBqProducesPositiveActivity() {
        double bq = Units.atomsToBq(1.0e20, 20L * 3600L * 20L);
        assertTrue(bq > 0.0);
    }

    @Test
    void atomsToBqZeroHalfLife() {
        assertEquals(0.0, Units.atomsToBq(1.0e10, 0));
    }

    @Test
    void bqToGy() {
        double gy = Units.bqToGyPerSecond(1.0e6, 1.0e-12);
        assertEquals(1.0e-6, gy, 1e-18);
    }

    @Test
    void gyToSvAppliesQuality() {
        assertEquals(20.0, Units.gyToSv(1.0, 20.0f), EPS);
        assertEquals(1.0, Units.gyToSv(1.0, 1.0f), EPS);
    }

    @Test
    void svPerSecondToSvPerHour() {
        assertEquals(3600.0, Units.svPerSecondToSvPerHour(1.0), EPS);
    }

    @Test
    void decayAtomsHalvesAtHalfLife() {
        double remaining = Units.decayAtoms(1000.0, 1000L, 1000L);
        assertEquals(500.0, remaining, 1e-6);
    }

    @Test
    void decayAtomsNoTimeNoChange() {
        assertEquals(1000.0, Units.decayAtoms(1000.0, 1000L, 0L), EPS);
    }

    @Test
    void fullPipelineBqGySv() {
        double bq = 1.0e6;
        double gyPerSec = Units.bqToGyPerSecond(bq, 1.0e-12);
        double svPerSec = Units.gyToSv(gyPerSec, 1.0f);
        double svPerHour = Units.svPerSecondToSvPerHour(svPerSec);
        assertEquals(svPerSec * 3600.0, svPerHour, 1e-18);
    }
}
