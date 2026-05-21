package igentuman.nr.network;

public final class ClientShieldingRaysCache {
    private static volatile ShieldingRaysDebugPayload latest;

    private ClientShieldingRaysCache() {}

    public static void update(ShieldingRaysDebugPayload p) {
        latest = p;
    }

    public static ShieldingRaysDebugPayload get() { return latest; }

    public static void clear() { latest = null; }
}
