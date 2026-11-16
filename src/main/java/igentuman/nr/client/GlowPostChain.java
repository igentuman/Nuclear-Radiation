package igentuman.nr.client;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Native frame-graph bloom pyramid; only the external silhouette target needs depth. */
final class GlowPostChain implements AutoCloseable {
    private final List<PostPass> passes = new ArrayList<>();
    private final Map<Identifier, PostChainConfig.InternalTarget> internalTargets;
    private final Projection projection = new Projection();
    private final ProjectionMatrixBuffer projectionBuffer = new ProjectionMatrixBuffer("ionization glow");
    final float intensity;

    GlowPostChain(float intensity) throws IOException {
        this.intensity = intensity;
        try {
            PostChainConfig config;
            try (var reader = Minecraft.getInstance().getResourceManager().openAsReader(
                    id("post_effect/ionization_glow.json"))) {
                config = PostChainConfig.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
            }
            internalTargets = config.internalTargets();
            projection.setupOrtho(0.1f, 1000, 1, 1, false);
            for (var pass : config.passes()) {
                var pipeline = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
                        .withLocation(id("ionization_glow/" + passes.size()))
                        .withVertexShader(pass.vertexShaderId())
                        .withFragmentShader(pass.fragmentShaderId())
                        .withUniform("SamplerInfo", UniformType.UNIFORM_BUFFER);
                List<PostPass.Input> inputs = new ArrayList<>();
                for (var input : pass.inputs()) {
                    if (!(input instanceof PostChainConfig.TargetInput target)) {
                        throw new IOException("Ionization glow requires render-target inputs");
                    }
                    if (!internalTargets.containsKey(target.targetId())
                            && !LevelTargetBundle.OUTLINE_TARGETS.contains(target.targetId())) {
                        throw new IOException("Unknown ionization target: " + target.targetId());
                    }
                    pipeline.withSampler(target.samplerName() + "Sampler");
                    inputs.add(new PostPass.TargetInput(target.samplerName(), target.targetId(),
                            target.useDepthBuffer(), target.bilinear()));
                }
                Map<String, List<UniformValue>> uniforms = new HashMap<>(pass.uniforms());
                if (pass.fragmentShaderId().equals(id("post/ionization_glow_composite"))) {
                    uniforms.put("GlowConfig", List.of(new UniformValue.FloatUniform(2 * intensity)));
                }
                uniforms.keySet().forEach(name -> pipeline.withUniform(name, UniformType.UNIFORM_BUFFER));
                passes.add(new PostPass(pipeline.build(), pass.outputTarget(), uniforms, inputs));
            }
        } catch (IOException | RuntimeException ex) {
            close();
            throw ex;
        }
    }

    void addToFrame(FrameGraphBuilder frame, int width, int height, PostChain.TargetBundle provided) {
        Map<Identifier, ResourceHandle<RenderTarget>> targets = new HashMap<>();
        for (Identifier external : LevelTargetBundle.OUTLINE_TARGETS) {
            targets.put(external, provided.getOrThrow(external));
        }
        for (var entry : internalTargets.entrySet()) {
            String name = entry.getKey().getPath();
            // JSON keeps the topology editable; pyramid levels follow the actual framebuffer size.
            int level = name.startsWith("glow_down_") || name.startsWith("glow_up_")
                    ? Integer.parseInt(name.substring(name.lastIndexOf('_') + 1)) : 0;
            int divisor = 1 << level;
            int w = entry.getValue().width().orElse(Math.max(1, (width + divisor - 1) / divisor));
            int h = entry.getValue().height().orElse(Math.max(1, (height + divisor - 1) / divisor));
            targets.put(entry.getKey(), frame.createInternal(entry.getKey().toString(),
                    new RenderTargetDescriptor(w, h, false, entry.getValue().clearColor())));
        }
        // screenquad uses gl_VertexID and normalized UVs, independent of output dimensions.
        projection.setSize(width, height);
        var matrix = projectionBuffer.getBuffer(projection);
        for (PostPass pass : passes) pass.addToFrame(frame, targets, matrix);
        for (Identifier external : LevelTargetBundle.OUTLINE_TARGETS) {
            provided.replace(external, targets.get(external));
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("nuclear_radiation", path);
    }

    @Override
    public void close() {
        passes.forEach(PostPass::close);
        passes.clear();
        projectionBuffer.close();
    }
}
