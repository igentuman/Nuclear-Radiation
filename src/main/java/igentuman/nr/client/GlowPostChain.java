package igentuman.nr.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** A bloom pyramid. Only the silhouette needs depth; all filtering happens on smaller color targets. */
final class GlowPostChain implements AutoCloseable {
    private static final int LEVELS = 5;
    private final List<RenderTarget> targets = new ArrayList<>();
    private final List<PostPass> passes = new ArrayList<>();
    final RenderTarget mask;
    final RenderTarget output;
    private final PostPass composite;

    GlowPostChain(ResourceProvider resources, int width, int height) throws IOException {
        try {
            mask = target(width, height, true, false);
            RenderTarget[] down = new RenderTarget[LEVELS];
            RenderTarget input = mask;
            for (int i = 0; i < LEVELS; i++) {
                width = Math.max(1, (width + 1) / 2);
                height = Math.max(1, (height + 1) / 2);
                down[i] = target(width, height, false, true);
                pass(resources, "ionization_glow_blur", input, down[i]);
                input = down[i];
            }
            for (int i = LEVELS - 2; i >= 0; i--) {
                RenderTarget up = target(down[i].width, down[i].height, false, true);
                PostPass pass = pass(resources, "ionization_glow_upsample", input, up);
                pass.addAuxAsset("DetailSampler", down[i]::getColorTextureId, down[i].width, down[i].height);
                input = up;
            }
            output = target(mask.width, mask.height, false, false);
            composite = pass(resources, "ionization_glow_composite", input, output);
            composite.addAuxAsset("MaskSampler", mask::getColorTextureId, mask.width, mask.height);
        } catch (IOException | RuntimeException ex) {
            close();
            throw ex;
        }
    }

    private RenderTarget target(int width, int height, boolean depth, boolean floatingPoint) {
        RenderTarget target = new TextureTarget(width, height, depth, Minecraft.ON_OSX);
        targets.add(target);
        target.setClearColor(0, 0, 0, 0);
        target.setFilterMode(GL11.GL_LINEAR);
        if (floatingPoint) {
            // Preserve faint/distant sources through the pyramid instead of quantizing them to zero.
            GlStateManager._bindTexture(target.getColorTextureId());
            GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, width, height,
                    0, GL11.GL_RGBA, GL11.GL_FLOAT, null);
            GlStateManager._bindTexture(0);
        }
        return target;
    }

    private PostPass pass(ResourceProvider resources, String name, RenderTarget input, RenderTarget output) throws IOException {
        PostPass pass = new PostPass(resources, "nuclear_radiation:" + name, input, output, true);
        passes.add(pass);
        // Vanilla PostChain shares a full-screen projection, which is wrong for reduced-size outputs.
        pass.setOrthoMatrix(new Matrix4f().setOrtho(0, output.width, 0, output.height, 0.1f, 1000));
        return pass;
    }

    void process(float partialTick, float intensity) {
        composite.getEffect().safeGetUniform("Boost").set(2.0f * intensity);
        for (PostPass pass : passes) pass.process(partialTick);
    }

    @Override
    public void close() {
        for (PostPass pass : passes) pass.close();
        passes.clear();
        for (RenderTarget target : targets) target.destroyBuffers();
        targets.clear();
    }
}
