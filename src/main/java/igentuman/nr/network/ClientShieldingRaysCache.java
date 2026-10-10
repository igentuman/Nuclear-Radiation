package igentuman.nr.network;

import net.minecraft.client.Minecraft;

public final class ClientShieldingRaysCache {
    private static volatile ShieldingRaysDebugPayload latest;
    public static long lastUpdateTime;

    private ClientShieldingRaysCache() {}

    public static void update(ShieldingRaysDebugPayload p) {
        latest = p;
        assert Minecraft.getInstance().level != null;
        lastUpdateTime = Minecraft.getInstance().level.getGameTime();
    }

    public static ShieldingRaysDebugPayload get() {
        assert Minecraft.getInstance().level != null;
        if (lastUpdateTime < Minecraft.getInstance().level.getGameTime() - 20) {
            latest = null;
        }
        return latest;
    }

    public static void clear() { latest = null; }
}
