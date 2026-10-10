package igentuman.nr.client;

import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.config.NRClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

/** Selects radioactive dropped items for the ionization post effect. */
public final class IonizationGlowRenderer {
    // This color marks only our items in Minecraft's entity silhouette target.
    public static final int MASK_COLOR = 0xFF01FE;
    private static final Identifier GLOW_CHAIN = Identifier.fromNamespaceAndPath("nuclear_radiation", "ionization_glow");

    private IonizationGlowRenderer() {}

    public static boolean shouldGlow(ItemEntity item) {
        if (!NRClientConfig.GLOW_ENABLED.get() || NRClientConfig.GLOW_INTENSITY.get() <= 0.0) return false;
        double activity = RadiationBindings.of(item.getItem()).totalActivityBq();
        return activity > 0.0 && activity * item.getItem().getCount() >= NRClientConfig.GLOW_THRESHOLD_BQ.get();
    }

    public static boolean hasGlowingItems() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return false;
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof ItemEntity item && shouldGlow(item)) return true;
        }
        return false;
    }

    public static PostChain chainForWorldOutlines(PostChain vanilla) {
        if (!hasGlowingItems()) return vanilla;
        PostChain glow = Minecraft.getInstance().getShaderManager().getPostChain(GLOW_CHAIN, LevelTargetBundle.OUTLINE_TARGETS);
        return glow != null ? glow : vanilla;
    }
}
