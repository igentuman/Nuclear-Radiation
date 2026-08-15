package igentuman.nr.network;

public final class ClientRadiationCache {
    private static volatile double svTotal;
    private static volatile double svPerHour;
    private static volatile double svPerHourAmbient;

    private ClientRadiationCache() {}

    public static void update(RadiationSyncPayload p) {
        svTotal = Math.max(0.0, p.svTotal());
        svPerHour = Math.max(0.0, p.svPerHour());
        svPerHourAmbient = Math.max(0.0, p.svPerHourAmbient());
    }

    public static double svTotal() { return Math.max(0.0, svTotal); }
    public static double svPerHour() { return Math.max(0.0, svPerHour); }
    public static double svPerHourAmbient() { return Math.max(0.0, svPerHourAmbient); }
}
