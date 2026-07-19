package igentuman.nr.api.isotope;

import igentuman.nr.config.GeneralConfig;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.util.Units;

public class IsotopeStack {
    private final Isotope isotope;
    private double atoms;
    private long timestamp;

    public IsotopeStack(Isotope isotope, double atoms, long timestamp) {
        this.isotope = isotope;
        this.atoms = atoms;
        this.timestamp = timestamp;
    }

    public Isotope isotope() {
        return isotope;
    }

    public double atoms() {
        return atoms;
    }

    public long timestamp() {
        return timestamp;
    }

    public void setAtoms(double atoms) {
        this.atoms = atoms;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double currentActivityBq() {
        return Units.atomsToBq(atoms, isotope.halfLifeTicks());
    }

    public double advanceDecay(long currentTick) {
        long delta = currentTick - timestamp;
        if (delta <= 0) return 0.0;
        double multiplier = GeneralConfig.ISOTOPE_DECAY_MULTIPLIER.get()*4.5D;
        if (isStatic(isotope, multiplier)) {
            timestamp = currentTick;
            return 0.0;
        }
        long effectiveDelta = (long) Math.ceil(delta * multiplier);
        double before = atoms;
        atoms = Units.decayAtoms(atoms, isotope.halfLifeTicks(), effectiveDelta);
        timestamp = currentTick;
        double decayed = before - atoms;
        return decayed > 0 ? decayed : 0.0;
    }

    public long expiryTick(double floorBq) {
        if (isStatic(isotope, GeneralConfig.ISOTOPE_DECAY_MULTIPLIER.get()*4.5D)) return Long.MAX_VALUE;
        long dt = Units.ticksUntilActivityFloor(currentActivityBq(), isotope.halfLifeTicks(), floorBq);
        if (dt == Long.MAX_VALUE) return Long.MAX_VALUE;
        long e = timestamp + dt;
        return e < timestamp ? Long.MAX_VALUE : e;
    }

    public static boolean isStatic(Isotope iso, double decayMultiplier) {
        if (iso.halfLifeTicks() <= 0) return true;
        if (decayMultiplier <= 0.0) return true;
        double thresholdTicks = RadiationConfig.STATIC_HALF_LIFE_YEARS.get() * Units.TICKS_PER_YEAR;
        return (iso.halfLifeTicks() / decayMultiplier) > thresholdTicks;
    }
}
