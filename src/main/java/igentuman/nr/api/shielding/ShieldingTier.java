package igentuman.nr.api.shielding;

import igentuman.nr.radiation.shielding.world.ShieldingRegistry;

import javax.annotation.Nullable;

public enum ShieldingTier {
    LIGHT(0.10, 0.30),
    MID(0.30, 0.25),
    HEAVY(0.60, 0.45),
    EXTRA_HEAVY(0.95, 0.95)
    ;

    public final double defaultXray;
    public final double defaultNeutron;

    ShieldingTier(double defaultXray, double defaultNeutron) {
        this.defaultXray = defaultXray;
        this.defaultNeutron = defaultNeutron;
    }

    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public ShieldingRegistry.Coeffs defaultCoeffs() {
        return new ShieldingRegistry.Coeffs(defaultXray, defaultNeutron);
    }

    @Nullable
    public static ShieldingTier byId(String id) {
        if (id == null) return null;
        for (ShieldingTier t : values()) {
            if (t.id().equals(id.toLowerCase(java.util.Locale.ROOT))) return t;
        }
        return null;
    }
}
