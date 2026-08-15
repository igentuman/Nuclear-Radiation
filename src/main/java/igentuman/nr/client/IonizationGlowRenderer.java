package igentuman.nr.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.config.NRClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Item-glow post-processing pipeline: re-renders highly radioactive dropped items into a private
 * render target, runs a separable blur post chain over them, then additively composites the soft
 * aura back onto the main framebuffer. Mirrors vanilla's entity-outline machinery with a custom chain.
 */
@EventBusSubscriber(modid = NuclearRadiation.MODID, value = Dist.CLIENT)
public final class IonizationGlowRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation CHAIN_ID =
            ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "shaders/post/ionization_glow.json");

    private static PostChain chain;
    private static RenderTarget glowTarget;
    private static GlowSilhouette.Buffers glowBuffers;
    private static int lastWidth = -1;
    private static int lastHeight = -1;
    private static boolean failed;

    private IonizationGlowRenderer() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        if (!NRClientConfig.GLOW_ENABLED.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        double threshold = NRClientConfig.GLOW_THRESHOLD_BQ.get();
        Frustum frustum = event.getFrustum();

        List<ItemEntity> glowing = new ArrayList<>();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof ItemEntity item)) continue;
            if (frustum != null && !frustum.isVisible(item.getBoundingBoxForCulling())) continue;
            double bq = RadiationBindings.of(item.getItem()).totalActivityBq();
            if (bq > 0 && bq * item.getItem().getCount() >= threshold) {
                glowing.add(item);
            }
        }
        if (glowing.isEmpty()) return;
        if (!ensureChain(mc)) return;

        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        double intensity = NRClientConfig.GLOW_INTENSITY.get();

        glowTarget.setClearColor(0f, 0f, 0f, 0f);
        glowTarget.clear(Minecraft.ON_OSX);
        // Pull in the world depth so the silhouette render (LEQUAL) discards items behind blocks.
        glowTarget.copyDepthFrom(mc.getMainRenderTarget());
        glowTarget.bindWrite(true);

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        PoseStack pose = event.getPoseStack();
        // Suppress the entity shadow: it renders as a ground quad whose UVs index the shadow texture,
        // but our buffer source binds the block atlas, so it would sample garbage alpha into a speckled patch.
        dispatcher.setRenderShadow(false);
        for (ItemEntity item : glowing) {
            int color = glowColor(RadiationBindings.of(item.getItem()).totalActivityBq(), threshold, intensity);
            glowBuffers.setColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, 255);
            double ex = Mth.lerp(partial, item.xOld, item.getX());
            double ey = Mth.lerp(partial, item.yOld, item.getY());
            double ez = Mth.lerp(partial, item.zOld, item.getZ());
            int light = dispatcher.getPackedLightCoords(item, partial);
            dispatcher.render(item, ex - cam.x, ey - cam.y, ez - cam.z, 0.0F, partial, pose, glowBuffers, light);
        }
        dispatcher.setRenderShadow(true);
        glowBuffers.endBatch();

        chain.process(partial);

        RenderTarget main = mc.getMainRenderTarget();
        main.bindWrite(true);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        glowTarget.blitToScreen(main.width, main.height, false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    private static boolean ensureChain(Minecraft mc) {
        if (failed) return false;
        int w = mc.getMainRenderTarget().width;
        int h = mc.getMainRenderTarget().height;
        if (chain == null) {
            try {
                chain = new PostChain(mc.getTextureManager(), mc.getResourceManager(),
                        mc.getMainRenderTarget(), CHAIN_ID);
                chain.resize(w, h);
                glowTarget = chain.getTempTarget("final");
                glowBuffers = new GlowSilhouette.Buffers();
                lastWidth = w;
                lastHeight = h;
            } catch (Exception ex) {
                LOGGER.error("Failed to load ionization_glow post chain", ex);
                failed = true;
                chain = null;
                glowTarget = null;
                return false;
            }
        }
        if (w != lastWidth || h != lastHeight) {
            chain.resize(w, h);
            glowTarget = chain.getTempTarget("final");
            lastWidth = w;
            lastHeight = h;
        }
        return true;
    }

    public static void onResourceReload() {
        if (chain != null) {
            try {
                chain.close();
            } catch (Exception ignored) {
            }
        }
        chain = null;
        glowTarget = null;
        glowBuffers = null;
        lastWidth = -1;
        lastHeight = -1;
        failed = false;
    }

    // green at threshold -> blue ~100x -> white ~1e4x, scaled by intensity
    private static int glowColor(double bq, double threshold, double intensity) {
        double r = Mth.clamp(Math.log10(bq / threshold) / 4.0, 0.0, 1.0);
        float[] green = {0.40f, 1.00f, 0.20f};
        float[] blue = {0.20f, 0.80f, 1.00f};
        float[] white = {1.00f, 1.00f, 1.00f};
        float[] c = r < 0.5
                ? lerp(green, blue, (float) (r / 0.5))
                : lerp(blue, white, (float) ((r - 0.5) / 0.5));
        int ri = (int) (c[0] * 255 * intensity);
        int gi = (int) (c[1] * 255 * intensity);
        int bi = (int) (c[2] * 255 * intensity);
        return (ri << 16) | (gi << 8) | bi;
    }

    private static float[] lerp(float[] a, float[] b, float t) {
        return new float[]{
                a[0] + (b[0] - a[0]) * t,
                a[1] + (b[1] - a[1]) * t,
                a[2] + (b[2] - a[2]) * t
        };
    }
}
