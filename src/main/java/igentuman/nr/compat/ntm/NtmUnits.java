package igentuman.nr.compat.ntm;

import igentuman.nr.radiation.simulation.entity.EntityDoseProcessor;

/** NTM units are gameplay quantities; these are configurable mappings, not physical rad. */
public final class NtmUnits {
    public static final double NR_RATE_GAIN = EntityDoseProcessor.RATE_GAIN;

    private NtmUnits() {}

    public static double doseToSv(double dose, double svPerUnit) {
        return positiveFinite(dose) * validScale(svPerUnit);
    }

    public static double doseFromSv(double sv, double svPerUnit) {
        return positiveFinite(sv) / validScale(svPerUnit);
    }

    public static double incrementToSv(double amount, double doseScale, double exposureScale) {
        if (!Double.isFinite(amount)) return 0.0;
        return amount * validScale(amount > 0.0 ? exposureScale : doseScale);
    }

    /** Reverse NR's displayed, smoothed Sv/h into an unamplified NTM exposure/second. */
    public static double exposureFromDisplayedRate(double svPerHour, double svPerUnit) {
        return positiveFinite(svPerHour) / (3600.0 * NR_RATE_GAIN * validScale(svPerUnit));
    }

    public static double resistanceProtection(double resistance) {
        return 1.0 - Math.pow(10.0, -positiveFinite(resistance));
    }

    private static double positiveFinite(double value) {
        return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }

    private static double validScale(double scale) {
        if (!Double.isFinite(scale) || scale <= 0.0) {
            throw new IllegalArgumentException("NTM conversion scale must be finite and positive");
        }
        return scale;
    }
}
