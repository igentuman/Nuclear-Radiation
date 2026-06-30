package igentuman.nr.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import igentuman.nr.NuclearRadiation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

/**
 * Depth-testing flat-color render path for the ionization glow. The stock {@code RenderType.outline}
 * is hard-wired to {@code NO_DEPTH_TEST}, so glowing items would bleed through walls. This mirrors it
 * but adds {@code LEQUAL_DEPTH_TEST}, relying on the caller to copy the world depth into the glow
 * target first. Geometry is routed through {@link Buffers} (a color-forcing buffer source modeled on
 * vanilla {@code OutlineBufferSource.EntityOutlineGenerator}) so every item draws as a flat silhouette
 * sampled from the block atlas.
 */
public final class GlowSilhouette {
    private static ShaderInstance shader;

    // No-op output state: keep whatever framebuffer the caller bound instead of rebinding the main target.
    private static final RenderStateShard.OutputStateShard KEEP_TARGET =
            new RenderStateShard.OutputStateShard("nr_keep_target", () -> {}, () -> {});

    public static final RenderType RENDER_TYPE = RenderType.create(
            "nr_glow_silhouette",
            DefaultVertexFormat.POSITION_TEX_COLOR,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> shader))
                    .setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false))
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setOutputState(KEEP_TARGET)
                    .createCompositeState(false));

    private GlowSilhouette() {}

    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        ShaderInstance instance = new ShaderInstance(
                event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "glow_silhouette"),
                DefaultVertexFormat.POSITION_TEX_COLOR);
        event.registerShader(instance, si -> shader = si);
    }

    /** Routes all incoming item geometry into a single flat-colored silhouette buffer. */
    public static final class Buffers implements MultiBufferSource {
        private final MultiBufferSource.BufferSource delegate =
                MultiBufferSource.immediate(new ByteBufferBuilder(1536));
        private int color = 0xFFFFFFFF;

        public void setColor(int red, int green, int blue, int alpha) {
            this.color = FastColor.ARGB32.color(alpha, red, green, blue);
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return new ColorForcer(delegate.getBuffer(RENDER_TYPE), color);
        }

        public void endBatch() {
            delegate.endBatch();
        }
    }

    // Forces a flat color and drops the entity-format attributes the silhouette format lacks.
    private record ColorForcer(VertexConsumer delegate, int color) implements VertexConsumer {
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.delegate.addVertex(x, y, z).setColor(this.color);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            return this;
        }
    }
}
