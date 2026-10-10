package igentuman.nr.radiation.simulation;

import igentuman.nr.api.DecayGraph;
import igentuman.nr.config.RadiationConfig;

public final class GasClouds {

    private GasClouds() {}

    public static boolean isGasEmitting(DecayGraph.WorldRadSource source) {
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

    public static double radius(DecayGraph.WorldRadSource source) {
        return radius(source.activityBq());
    }
}
