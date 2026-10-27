package igentuman.nr.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.config.NRClientConfig;
import igentuman.nr.integration.iris.IrisCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/** Depth-tested item silhouettes followed by a small bloom pyramid, composited after world shaders. */
@EventBusSubscriber(modid = NuclearRadiation.MODID, value = Dist.CLIENT)
public final class IonizationGlowRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<GlowingItem> glowing = new ArrayList<>();
    private static GlowPostChain chain;
    private static GlowSilhouette.Buffers glowBuffers;
    private static boolean depthCaptured;
    private static boolean failed;

    private IonizationGlowRenderer() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        var stage = event.getStage();
        if (stage != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES
                && stage != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        // Shadow rendering can emit level-stage events too; never replace the main view's work.
        if (IrisCompat.renderingShadowPass()) return;

        Minecraft mc = Minecraft.getInstance();
        if (!NRClientConfig.GLOW_ENABLED.get() || NRClientConfig.GLOW_INTENSITY.get() <= 0
                || mc.level == null || mc.player == null || failed) {
            glowing.clear();
            depthCaptured = false;
            return;
        }

        if (stage == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            glowing.clear();
            depthCaptured = false;
            collectItems(mc, event);
            // Iris can clear/replace world depth during its final pass. Capture before that happens,
            // but defer all custom geometry and post processing until Iris has finished rendering.
            if (!glowing.isEmpty() && IrisCompat.shaderPackInUse()) captureDepth(mc);
        } else if (!glowing.isEmpty()) {
            try {
                if (!depthCaptured) captureDepth(mc);
                if (depthCaptured) renderGlow(mc, event);
            } finally {
                glowing.clear();
                depthCaptured = false;
            }
        }
    }

    private static void collectItems(Minecraft mc, RenderLevelStageEvent event) {
        double threshold = NRClientConfig.GLOW_THRESHOLD_BQ.get();
        Vec3 camera = event.getCamera().getPosition();
        Frustum frustum = event.getFrustum();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity item) || item.isInvisible()) continue;
            // Include vanilla's entity-distance limit, not just the camera frustum.
            if (!item.shouldRender(camera.x, camera.y, camera.z)) continue;
            if (frustum != null && !dispatcher.shouldRender(item, frustum, camera.x, camera.y, camera.z)) continue;
            double bq = RadiationBindings.of(item.getItem()).totalActivityBq() * item.getItem().getCount();
            if (bq > 0 && bq >= threshold) glowing.add(new GlowingItem(item, glowColor(bq, threshold)));
        }
    }

    private static void captureDepth(Minecraft mc) {
        // Allocation and depth copies bind framebuffers. Restore the exact caller bindings, including
        // Iris' G-buffer, so later world stages keep rendering to their intended destination.
        int read = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int draw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        try {
            RenderTarget main = mc.getMainRenderTarget();
            if (!ensureChain(mc, main)) return;
            matchDepthFormat(chain.mask, main);
            chain.mask.clear(Minecraft.ON_OSX);
            chain.mask.copyDepthFrom(main);
            depthCaptured = true;
        } finally {
            GlStateManager._bindTexture(texture);
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, read);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, draw);
            RenderSystem.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        }
    }

    private static void renderGlow(Minecraft mc, RenderLevelStageEvent event) {
        RenderTarget main = mc.getMainRenderTarget();
        if (chain.mask.width != main.width || chain.mask.height != main.height) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 cam = event.getCamera().getPosition();
        Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        var sorting = RenderSystem.getVertexSorting();
        var modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        try {
            // AFTER_LEVEL has unwound LevelRenderer's model-view stack.
            modelView.mul(event.getModelViewMatrix());
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(event.getProjectionMatrix(), sorting);
            chain.mask.bindWrite(true);
            EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
            PoseStack pose = new PoseStack();
            for (GlowingItem source : glowing) {
                ItemEntity item = source.item();
                if (item.isRemoved() || item.level() != mc.level) continue;
                int color = source.color();
                glowBuffers.setColor((color >> 16) & 255, (color >> 8) & 255, color & 255, 255);
                EntityRenderer<? super ItemEntity> renderer = dispatcher.getRenderer(item);
                Vec3 offset = renderer.getRenderOffset(item, partial);
                pose.pushPose();
                try {
                    pose.translate(Mth.lerp(partial, item.xOld, item.getX()) - cam.x + offset.x,
                            Mth.lerp(partial, item.yOld, item.getY()) - cam.y + offset.y,
                            Mth.lerp(partial, item.zOld, item.getZ()) - cam.z + offset.z);
                    // Render only the item, excluding dispatcher shadows, fire, and debug hitboxes.
                    renderer.render(item, 0, partial, pose, glowBuffers, LightTexture.FULL_BRIGHT);
                } finally {
                    pose.popPose();
                }
            }
            glowBuffers.endBatch();
            chain.process(partial, NRClientConfig.GLOW_INTENSITY.get().floatValue());
            main.bindWrite(true);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            chain.output.blitToScreen(main.width, main.height, false);
        } finally {
            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(projection, sorting);
            main.bindWrite(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.colorMask(true, true, true, true);
        }
    }

    private static boolean ensureChain(Minecraft mc, RenderTarget main) {
        if (failed) return false;
        if (chain != null && chain.mask.width == main.width && chain.mask.height == main.height) return true;
        if (chain != null) chain.close();
        chain = null;
        try {
            chain = new GlowPostChain(mc.getResourceManager(), main.width, main.height);
            if (glowBuffers == null) glowBuffers = new GlowSilhouette.Buffers();
            return true;
        } catch (Exception ex) {
            LOGGER.error("Failed to load ionization glow; disabled until resource reload", ex);
            failed = true;
            return false;
        }
    }

    /** GL depth blits require matching internal formats, including shader packs' depth/stencil formats. */
    private static void matchDepthFormat(RenderTarget target, RenderTarget main) {
        GlStateManager._bindTexture(main.getDepthTextureId());
        int format = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_INTERNAL_FORMAT);
        GlStateManager._bindTexture(target.getDepthTextureId());
        if (format == GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_INTERNAL_FORMAT)) return;
        boolean stencil = format == GL30.GL_DEPTH24_STENCIL8 || format == GL30.GL_DEPTH32F_STENCIL8;
        int type = format == GL30.GL_DEPTH24_STENCIL8 ? GL30.GL_UNSIGNED_INT_24_8
                : format == GL30.GL_DEPTH32F_STENCIL8 ? GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV
                : format == GL30.GL_DEPTH_COMPONENT32F ? GL11.GL_FLOAT : GL11.GL_UNSIGNED_INT;
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, format, target.width, target.height, 0,
                stencil ? GL30.GL_DEPTH_STENCIL : GL11.GL_DEPTH_COMPONENT, type, null);
        target.bindWrite(false);
        GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT,
                GL11.GL_TEXTURE_2D, 0, 0);
        GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,
                stencil ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : GL30.GL_DEPTH_ATTACHMENT,
                GL11.GL_TEXTURE_2D, target.getDepthTextureId(), 0);
        target.checkStatus();
    }

    public static void onResourceReload() {
        if (chain != null) chain.close();
        chain = null;
        if (glowBuffers != null) glowBuffers.close();
        glowBuffers = null;
        glowing.clear();
        depthCaptured = false;
        failed = false;
    }

    // Green at threshold -> blue ~100x -> white ~1e4x. Stack activity drives both eligibility and hue.
    private static int glowColor(double bq, double threshold) {
        double ratio = threshold > 0 ? Mth.clamp(Math.log10(bq / threshold) / 4.0, 0, 1) : 1;
        double t = ratio < 0.5 ? ratio * 2 : ratio * 2 - 1;
        double red = ratio < 0.5 ? Mth.lerp(t, 0.4, 0.2) : Mth.lerp(t, 0.2, 1.0);
        double green = ratio < 0.5 ? Mth.lerp(t, 1.0, 0.8) : Mth.lerp(t, 0.8, 1.0);
        double blue = ratio < 0.5 ? Mth.lerp(t, 0.2, 1.0) : 1.0;
        return ((int) (red * 255) << 16) | ((int) (green * 255) << 8) | (int) (blue * 255);
    }

    private record GlowingItem(ItemEntity item, int color) {}
}
