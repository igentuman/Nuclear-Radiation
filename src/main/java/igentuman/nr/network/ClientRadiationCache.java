package igentuman.nr.network;

public final class ClientRadiationCache {
    private static volatile double svTotal;
    private static volatile double svPerHour;
    private static volatile double svPerHourAmbient;

    private ClientRadiationCache() {}

    public static void update(RadiationSyncPayload p) {
        svTotal = p.svTotal();
        svPerHour = p.svPerHour();
        svPerHourAmbient = p.svPerHourAmbient();
    }

    public static double svTotal() { return svTotal; }
    public static double svPerHour() { return svPerHour; }
    public static double svPerHourAmbient() { return svPerHourAmbient; }
}
