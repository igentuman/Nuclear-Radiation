package igentuman.nr.gas;

import igentuman.nr.config.RadiationConfig;
import igentuman.nr.util.tracking.WorldRadSource;

public final class GasClouds {

    private GasClouds() {}

    public static boolean isGasEmitting(WorldRadSource source) {
        return source.isActive() && source.activityBq() >= RadiationConfig.GAS_SOURCE_THRESHOLD_BQ.get();
    }

    // Radius in blocks: base at threshold, +1 block per GAS_RADIUS_STEP_BQ above it, capped.
    public static double radius(double activityBq) {
        double threshold = RadiationConfig.GAS_SOURCE_THRESHOLD_BQ.get();
        if (activityBq < threshold) return 0.0;
        double step = RadiationConfig.GAS_RADIUS_STEP_BQ.get();
        double extra = Math.floor((activityBq - threshold) / step);
        double r = RadiationConfig.GAS_BASE_RADIUS.get() + extra;
        return Math.min(RadiationConfig.GAS_MAX_RADIUS.get(), r);
    }

    public static double radius(WorldRadSource source) {
        return radius(source.activityBq());
    }
}
