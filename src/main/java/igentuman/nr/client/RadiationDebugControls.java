package igentuman.nr.client;

import igentuman.nr.network.ClientChunkContaminationCache;
import igentuman.nr.network.ClientChunkVectorCache;
import igentuman.nr.network.ClientShieldingRaysCache;
import igentuman.nr.network.ClientRadiationCache;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

public final class RadiationDebugControls {
    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.nuclear_radiation.toggle_debug_vectors", GLFW.GLFW_KEY_F8,
            KeyMapping.Category.DEBUG);

    private static boolean enabled;

    private RadiationDebugControls() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (TOGGLE_KEY.consumeClick()) {
            enabled = !enabled;
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().gui.setOverlayMessage(Component.translatable(
                        enabled ? "message.nuclear_radiation.debug_on" : "message.nuclear_radiation.debug_off"), true);
            }
            if (!enabled) clearCaches();
        }
    }

    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        enabled = false;
        ClientRadiationCache.clear();
        clearCaches();
    }

    private static void clearCaches() {
        ClientChunkVectorCache.clear();
        ClientShieldingRaysCache.clear();
        ClientChunkContaminationCache.clear();
    }
}
