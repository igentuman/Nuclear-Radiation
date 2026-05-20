package igentuman.nr.api;

public final class Units {
    public static final double SECONDS_PER_TICK = 0.05;
    public static final double TICKS_PER_SECOND = 20.0;
    public static final double SECONDS_PER_HOUR = 3600.0;
    public static final double TICKS_PER_HOUR = TICKS_PER_SECOND * SECONDS_PER_HOUR;
    public static final double TICKS_PER_YEAR = TICKS_PER_SECOND * SECONDS_PER_HOUR * 24.0 * 365.25;

    public static final double LN2 = 0.6931471805599453;

    public static final double DEFAULT_GY_PER_BQ_SECOND = 1.0e-15;

    private Units() {}

    public static double decayConstantFromHalfLifeTicks(long halfLifeTicks) {
        if (halfLifeTicks <= 0) return 0.0;
        return LN2 / halfLifeTicks;
    }

    public static double atomsToBq(double atoms, long halfLifeTicks) {
        if (halfLifeTicks <= 0) return 0.0;
        double lambdaPerSec = LN2 / (halfLifeTicks * SECONDS_PER_TICK);
        return atoms * lambdaPerSec;
    }

    public static double bqToGyPerSecond(double bq, double gyPerBqSecond) {
        return bq * gyPerBqSecond;
    }

    public static double gyToSv(double gy, float qualityFactor) {
        return gy * qualityFactor;
    }

    public static double svPerSecondToSvPerHour(double svPerSecond) {
        return svPerSecond * SECONDS_PER_HOUR;
    }

    public static double decayAtoms(double atoms, long halfLifeTicks, long deltaTicks) {
        if (halfLifeTicks <= 0 || deltaTicks <= 0) return atoms;
        double lambda = LN2 / halfLifeTicks;
        return atoms * Math.exp(-lambda * deltaTicks);
    }

    public static long ticksUntilActivityFloor(double currentBq, long halfLifeTicks, double floorBq) {
        if (halfLifeTicks <= 0) return Long.MAX_VALUE;
        if (floorBq <= 0.0) return Long.MAX_VALUE;
        if (currentBq <= floorBq) return 0L;
        double lambda = LN2 / halfLifeTicks;
        double ticks = Math.log(currentBq / floorBq) / lambda;
        if (Double.isInfinite(ticks) || ticks >= Long.MAX_VALUE) return Long.MAX_VALUE;
        return (long) Math.ceil(ticks);
    }
}
