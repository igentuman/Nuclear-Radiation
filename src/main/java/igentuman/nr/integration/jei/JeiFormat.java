package igentuman.nr.integration.jei;

import igentuman.nr.util.Units;

public final class JeiFormat {
    private JeiFormat() {}

    public static String halfLife(long halfLifeTicks) {
        if (halfLifeTicks <= 0) return "stable";
        if (halfLifeTicks == Long.MAX_VALUE) return "~stable";
        double seconds = halfLifeTicks * Units.SECONDS_PER_TICK * 72D;
        if (seconds < 60) return fmt(seconds) + " s";
        double minutes = seconds / 60.0;
        if (minutes < 60) return fmt(minutes) + " min";
        double hours = minutes / 60.0;
        if (hours < 24) return fmt(hours) + " h";
        double days = hours / 24.0;
        if (days < 365.25) return fmt(days) + " d";
        double years = days / 365.25;
        if (years < 1.0e6) return fmt(years) + " y";
        if (years < 1.0e9) return fmt(years / 1.0e6) + " My";
        return fmt(years / 1.0e9) + " Gy";
    }

    public static String activity(double bq) {
        if (bq <= 0) return "0 Bq";
        if (bq < 1.0e3) return fmt(bq) + " Bq";
        if (bq < 1.0e6) return fmt(bq / 1.0e3) + " kBq";
        if (bq < 1.0e9) return fmt(bq / 1.0e6) + " MBq";
        if (bq < 1.0e12) return fmt(bq / 1.0e9) + " GBq";
        return fmt(bq / 1.0e12) + " TBq";
    }

    public static String percent(float v) {
        return String.format("%.0f%%", v * 100.0f);
    }

    public static String doseShare(float frac) {
        float pct = frac * 100.0f;
        if (pct > 0f && pct < 0.1f) return "<0.1%";
        return String.format("%.1f%%", pct);
    }

    public static String fmt(double v) {
        if (v >= 100) return String.format("%.0f", v);
        if (v >= 10) return String.format("%.1f", v);
        return String.format("%.2f", v);
    }
}
