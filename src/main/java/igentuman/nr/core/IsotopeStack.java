package igentuman.nr.core;

import igentuman.nr.config.GeneralConfig;
import igentuman.nr.api.Isotope;

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

    public void advanceDecay(long currentTick) {
        long delta = currentTick - timestamp;
        if (delta <= 0) return;
        double multiplier = GeneralConfig.ISOTOPE_DECAY_MULTIPLIER.get();
        long effectiveDelta = (long) Math.ceil(delta * multiplier);
        atoms = Units.decayAtoms(atoms, isotope.halfLifeTicks(), effectiveDelta);
        timestamp = currentTick;
    }

    public long expiryTick(double floorBq) {
        long dt = Units.ticksUntilActivityFloor(currentActivityBq(), isotope.halfLifeTicks(), floorBq);
        if (dt == Long.MAX_VALUE) return Long.MAX_VALUE;
        long e = timestamp + dt;
        return e < timestamp ? Long.MAX_VALUE : e;
    }
}
