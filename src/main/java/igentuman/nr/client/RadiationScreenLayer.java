package igentuman.nr.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.network.ClientRadiationCache;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

public final class RadiationScreenLayer implements LayeredDraw.Layer {

    private static ShaderInstance shaderInstance;

    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        ShaderInstance shader = new ShaderInstance(
                event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "radiation_screen"),
                DefaultVertexFormat.POSITION
        );
        event.registerShader(shader, instance -> shaderInstance = instance);
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker delta) {
        if (shaderInstance == null) return;

        float intensity = computeIntensity(ClientRadiationCache.svPerHour());
        float doseIntensity = computeDose(ClientRadiationCache.svTotal());
        if (intensity < 0.001f && doseIntensity < 0.001f) return;

        Minecraft mc = Minecraft.getInstance();
        float screenW = (float) mc.getWindow().getWidth();
        float screenH = (float) mc.getWindow().getHeight();
        float time = (float) ((System.nanoTime() / 1_000_000L) % 1_000_000L) / 1000f;
        float randSeed = mc.level != null ? (float) mc.level.random.nextFloat() : 0f;

        shaderInstance.safeGetUniform("Intensity").set(intensity);
        shaderInstance.safeGetUniform("DoseIntensity").set(doseIntensity);
        shaderInstance.safeGetUniform("RandSeed").set(randSeed);
        shaderInstance.safeGetUniform("Time").set(time);
        shaderInstance.safeGetUniform("ScreenSize").set(screenW, screenH);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(() -> shaderInstance);

        BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        buf.addVertex(-1f, -1f, 0f);
        buf.addVertex( 1f, -1f, 0f);
        buf.addVertex( 1f,  1f, 0f);
        buf.addVertex(-1f,  1f, 0f);
        BufferUploader.drawWithShader(buf.buildOrThrow());

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    static float computeIntensity(double svPerHour) {
        final double THRESHOLD_SVH = 25e-3;
        if (svPerHour < THRESHOLD_SVH) return 0f;
        double t = Math.log10(svPerHour / THRESHOLD_SVH) / 25.0;
        return (float) Math.clamp(t, 0.0, 1.0);
    }

    static float computeDose(double svTotal) {
        if (svTotal <= 0.0) return 0f;
        return (float) Math.clamp(svTotal / 1000, 0.0, 1.0);
    }
}
