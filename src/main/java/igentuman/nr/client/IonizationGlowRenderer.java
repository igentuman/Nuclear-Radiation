package igentuman.nr.client;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.logging.LogUtils;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.config.NRClientConfig;
import igentuman.nr.integration.iris.IrisCompat;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.OptionalInt;

/** Selects visible radioactive silhouettes and runs a reduced-resolution bloom pyramid. */
public final class IonizationGlowRenderer {
    // Green/blue tag the source; red carries log-scaled activity for the green -> blue -> white hue.
    private static final int MASK_TAG = 0x0001FE;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RenderPipeline BLOOM_BLIT = RenderPipelines.ENTITY_OUTLINE_BLIT.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath("nuclear_radiation", "pipeline/ionization_blit"))
            .withColorTargetState(new ColorTargetState(Optional.of(new BlendFunction(
                    SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ZERO, DestFactor.ONE)), 7))
            .build();
    private static GlowPostChain chain;
    private static boolean bloomRendered;
    private static boolean failed;

    private IonizationGlowRenderer() {}

    public static boolean shouldGlow(ItemEntity item) {
        if (failed || !NRClientConfig.GLOW_ENABLED.get() || NRClientConfig.GLOW_INTENSITY.get() <= 0.0
                || item.isInvisible() || item.isRemoved() || IrisCompat.renderingShadowPass()) return false;
        double activity = RadiationBindings.of(item.getItem()).totalActivityBq() * item.getItem().getCount();
        return activity > 0.0 && activity >= NRClientConfig.GLOW_THRESHOLD_BQ.get();
    }

    public static int maskColor(ItemEntity item) {
        double activity = RadiationBindings.of(item.getItem()).totalActivityBq() * item.getItem().getCount();
        double threshold = NRClientConfig.GLOW_THRESHOLD_BQ.get();
        double ratio = threshold > 0 ? Mth.clamp(Math.log10(activity / threshold) / 4, 0, 1) : 1;
        return ((int) Math.round(ratio * 255) << 16) | MASK_TAG;
    }

    public static void renderOutlines(PostChain vanilla, FrameGraphBuilder frame, int width, int height,
                                      PostChain.TargetBundle targets, LevelRenderState state) {
        if (IrisCompat.renderingShadowPass()) {
            vanilla.addToFrame(frame, width, height, targets);
            return;
        }
        bloomRendered = false;
        // These states have already passed vanilla's distance, frustum and section visibility checks.
        boolean visibleSource = state.entityRenderStates.stream().anyMatch(entity ->
                entity instanceof ItemEntityRenderState && !entity.isInvisible
                        && entity.appearsGlowing() && (entity.outlineColor & 0xFFFF) == MASK_TAG);
        if (!visibleSource || failed || !NRClientConfig.GLOW_ENABLED.get()
                || NRClientConfig.GLOW_INTENSITY.get() <= 0) {
            vanilla.addToFrame(frame, width, height, targets);
            return;
        }
        float intensity = NRClientConfig.GLOW_INTENSITY.get().floatValue();
        if (chain == null || chain.intensity != intensity) {
            if (chain != null) chain.close();
            chain = null;
            try {
                chain = new GlowPostChain(intensity);
            } catch (Exception ex) {
                failed = true;
                LOGGER.error("Failed to load ionization glow; disabled until resource reload", ex);
                vanilla.addToFrame(frame, width, height, targets);
                return;
            }
        }
        chain.addToFrame(frame, width, height, targets);
        bloomRendered = true;
    }

    /** Premultiplied outlines plus additive bloom, preserving the destination alpha. */
    public static void blitOutlines(RenderTarget source, GpuTextureView output) {
        if (!bloomRendered || IrisCompat.renderingShadowPass()) {
            source.blitAndBlendToTexture(output);
            return;
        }
        try (var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Ionization glow and entity outlines", output, OptionalInt.empty())) {
            pass.setPipeline(BLOOM_BLIT);
            RenderSystem.bindDefaultUniforms(pass);
            pass.bindTexture("InSampler", source.getColorTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.draw(0, 3);
        }
    }

    public static void onResourceReload() {
        if (chain != null) chain.close();
        chain = null;
        bloomRendered = false;
        failed = false;
    }
}
