package igentuman.nr.client;

import igentuman.nr.network.ChunkVectorDebugPayload;
import igentuman.nr.network.ClientChunkVectorCache;
import igentuman.nr.network.ClientShieldingRaysCache;
import igentuman.nr.network.ShieldingRaysDebugPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Draws server-synced field vectors and shielding rays using the 26.1 debug gizmo API. */
public final class RadiationDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private static final int X_RAY = 0xFF33FF33;
    private static final int NEUTRON = 0xFFFF5555;
    private static final int SECTION = 0xFFFFFF44;

    @Override
    public void emitGizmos(double camX, double camY, double camZ, DebugValueAccess debugValues,
                           Frustum frustum, float partialTicks) {
        if (!RadiationDebugControls.isEnabled() || Minecraft.getInstance().level == null) return;

        ChunkVectorDebugPayload vector = ClientChunkVectorCache.get();
        if (vector != null) {
            Vec3 apex = new Vec3(vector.apexX(), vector.apexY(), vector.apexZ());
            for (int direction = 0; direction < 6; direction++) {
                Vec3 tip = new Vec3(vector.tipX()[direction], vector.tipY()[direction], vector.tipZ()[direction]);
                if (vector.xRayBq()[direction] > 0) Gizmos.arrow(apex, tip, X_RAY, 2.0f);
                if (vector.neutronBq()[direction] > 0) {
                    Gizmos.arrow(apex.add(0, 0.05, 0), tip.add(0, 0.05, 0), NEUTRON, 2.0f);
                }
            }
            double x = vector.chunkX() * 16.0;
            double y = vector.chunkY() * 16.0;
            double z = vector.chunkZ() * 16.0;
            Gizmos.cuboid(new AABB(x, y, z, x + 16, y + 16, z + 16),
                    GizmoStyle.stroke(SECTION, 1.0f));
        }

        ShieldingRaysDebugPayload rays = ClientShieldingRaysCache.get();
        if (rays == null) return;
        Vec3 origin = new Vec3(rays.originX(), rays.originY(), rays.originZ());
        for (int i = 0; i < rays.endX().length; i++) {
            Vec3 end = new Vec3(rays.endX()[i], rays.endY()[i], rays.endZ()[i]);
            float pass = Math.clamp(rays.passValue()[i], 0.0f, 1.0f);
            int red = Math.round(255.0f * (1.0f - pass));
            int blueOrGreen = Math.round(255.0f * pass);
            int color = rays.channel()[i] == 0
                    ? 0xFF000000 | (red << 16) | (blueOrGreen << 8)
                    : 0xFF000000 | (red << 16) | blueOrGreen;
            Gizmos.line(origin, end, color, 1.5f);
            Gizmos.point(end, color, 3.0f);
        }
    }
}
