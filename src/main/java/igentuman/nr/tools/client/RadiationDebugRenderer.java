package igentuman.nr.tools.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.network.ChunkVectorDebugPayload;
import igentuman.nr.network.ClientChunkVectorCache;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.glfw.GLFW;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = NuclearRadiation.MODID, value = Dist.CLIENT)
public final class RadiationDebugRenderer {

    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.nuclear_radiation.toggle_debug_vectors",
            GLFW.GLFW_KEY_F8,
            "key.categories.nuclear_radiation");

    private static boolean enabled = false;
    private static boolean keyWasDown = false;

    private RadiationDebugRenderer() {}

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_KEY);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        boolean down = TOGGLE_KEY.isDown();
        if (down && !keyWasDown) {
            enabled = !enabled;
            LocalPlayer p = Minecraft.getInstance().player;
            if (p != null) {
                p.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "Radiation debug vectors: " + (enabled ? "ON" : "OFF")), true);
            }
            if (!enabled) ClientChunkVectorCache.clear();
        }
        keyWasDown = down;

        if (!enabled) return;
        ChunkVectorDebugPayload pl = ClientChunkVectorCache.get();
        if (pl == null) return;

        Minecraft mc = Minecraft.getInstance();
        Camera cam = event.getCamera();
        Vec3 camPos = cam.getPosition();

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-camPos.x, -camPos.y, -camPos.z);

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffers.getBuffer(RenderType.lines());

        double cx = pl.chunkX() * 16.0 + 8.0;
        double cz = pl.chunkZ() * 16.0 + 8.0;
        double surfaceY = mc.level != null
                ? mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) cx, (int) cz) + 1.0
                : 80.0;
        double sourceY = pl.centerY();

        double xrayLen = clamp(Math.log10(Math.max(1.0, pl.scalarXRay() + 1.0)), 0.5, 16.0);
        double neutronLen = clamp(Math.log10(Math.max(1.0, pl.scalarNeutron() + 1.0)), 0.5, 16.0);

        // X-ray gradient (green) — tilts from surface toward source position (X,Y,Z)
        drawArrow3D(vc, pose, cx, surfaceY, cz,
                cx + pl.gradXRayX() * xrayLen, sourceY, cz + pl.gradXRayZ() * xrayLen,
                0.2f, 1.0f, 0.2f);

        // Neutron gradient (red)
        drawArrow3D(vc, pose, cx, surfaceY + 0.05, cz,
                cx + pl.gradNeutronX() * neutronLen, sourceY + 0.05, cz + pl.gradNeutronZ() * neutronLen,
                1.0f, 0.3f, 0.3f);

        // Vertical Y marker (cyan): surface → source Y at chunk center, shows vertical offset
        line(vc, pose, cx, surfaceY, cz, cx, sourceY, cz, 0.2f, 0.8f, 1.0f);

        // Chunk bounds (yellow box) at source Y
        drawChunkOutline(vc, pose, pl.chunkX(), pl.chunkZ(), sourceY - 0.5);

        buffers.endBatch(RenderType.lines());
        pose.popPose();
    }

    private static void drawArrow(VertexConsumer vc, PoseStack pose,
                                  double x1, double y1, double z1,
                                  double x2, double y2, double z2,
                                  float r, float g, float b) {
        line(vc, pose, x1, y1, z1, x2, y2, z2, r, g, b);
        double dx = x2 - x1;
        double dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 1.0e-4) return;
        double nx = dx / len;
        double nz = dz / len;
        double headLen = Math.min(0.8, len * 0.25);
        double perpX = -nz;
        double perpZ = nx;
        double hx1 = x2 - nx * headLen + perpX * headLen * 0.5;
        double hz1 = z2 - nz * headLen + perpZ * headLen * 0.5;
        double hx2 = x2 - nx * headLen - perpX * headLen * 0.5;
        double hz2 = z2 - nz * headLen - perpZ * headLen * 0.5;
        line(vc, pose, x2, y2, z2, hx1, y2, hz1, r, g, b);
        line(vc, pose, x2, y2, z2, hx2, y2, hz2, r, g, b);
    }

    private static void drawArrow3D(VertexConsumer vc, PoseStack pose,
                                    double x1, double y1, double z1,
                                    double x2, double y2, double z2,
                                    float r, float g, float b) {
        line(vc, pose, x1, y1, z1, x2, y2, z2, r, g, b);
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0e-4) return;
        double nx = dx / len, ny = dy / len, nz = dz / len;
        double headLen = Math.min(1.0, len * 0.25);

        // pick perpendicular: cross(direction, worldUp); if parallel to up, fall back to X axis
        double upX = 0, upY = 1, upZ = 0;
        double pX = ny * upZ - nz * upY;
        double pY = nz * upX - nx * upZ;
        double pZ = nx * upY - ny * upX;
        double pLen = Math.sqrt(pX * pX + pY * pY + pZ * pZ);
        if (pLen < 1.0e-4) { pX = 1; pY = 0; pZ = 0; pLen = 1; }
        pX /= pLen; pY /= pLen; pZ /= pLen;

        // second perpendicular via cross(direction, p1)
        double qX = ny * pZ - nz * pY;
        double qY = nz * pX - nx * pZ;
        double qZ = nx * pY - ny * pX;

        double bx = x2 - nx * headLen;
        double by = y2 - ny * headLen;
        double bz = z2 - nz * headLen;
        double s = headLen * 0.5;

        line(vc, pose, x2, y2, z2, bx + pX * s, by + pY * s, bz + pZ * s, r, g, b);
        line(vc, pose, x2, y2, z2, bx - pX * s, by - pY * s, bz - pZ * s, r, g, b);
        line(vc, pose, x2, y2, z2, bx + qX * s, by + qY * s, bz + qZ * s, r, g, b);
        line(vc, pose, x2, y2, z2, bx - qX * s, by - qY * s, bz - qZ * s, r, g, b);
    }

    private static void drawChunkOutline(VertexConsumer vc, PoseStack pose, int cx, int cz, double y) {
        double x0 = cx * 16.0;
        double z0 = cz * 16.0;
        double x1 = x0 + 16.0;
        double z1 = z0 + 16.0;
        float r = 1.0f, g = 1.0f, b = 0.2f;
        line(vc, pose, x0, y, z0, x1, y, z0, r, g, b);
        line(vc, pose, x1, y, z0, x1, y, z1, r, g, b);
        line(vc, pose, x1, y, z1, x0, y, z1, r, g, b);
        line(vc, pose, x0, y, z1, x0, y, z0, r, g, b);
    }

    private static void line(VertexConsumer vc, PoseStack pose,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2,
                             float r, float g, float b) {
        Matrix4f m = pose.last().pose();
        float nx = (float) (x2 - x1);
        float ny = (float) (y2 - y1);
        float nz = (float) (z2 - z1);
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1.0e-6f) return;
        nx /= len; ny /= len; nz /= len;
        vc.addVertex(m, (float) x1, (float) y1, (float) z1)
                .setColor(r, g, b, 1.0f)
                .setNormal(nx, ny, nz);
        vc.addVertex(m, (float) x2, (float) y2, (float) z2)
                .setColor(r, g, b, 1.0f)
                .setNormal(nx, ny, nz);
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
