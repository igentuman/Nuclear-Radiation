package igentuman.nr.shielding;

import igentuman.nr.util.WorldUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;

public final class ShieldingRaycast {

    private static final int MAX_STEPS = 1024;

    private ShieldingRaycast() {}

    public static AttenuationResult cast(Level level, Vec3 from, Vec3 to) {
        if (!(level instanceof ServerLevel server)) return AttenuationResult.UNATTENUATED;
        return cast(server, from, to);
    }

    public static AttenuationResult cast(ServerLevel level, Vec3 from, Vec3 to) {
        double sumX = 0.0;
        double sumN = 0.0;

        double dxv = to.x - from.x;
        double dyv = to.y - from.y;
        double dzv = to.z - from.z;
        double dist = Math.sqrt(dxv * dxv + dyv * dyv + dzv * dzv);
        if (dist <= 1.0e-6) return AttenuationResult.UNATTENUATED;

        double rdx = dxv / dist;
        double rdy = dyv / dist;
        double rdz = dzv / dist;

        int x = (int) Math.floor(from.x);
        int y = (int) Math.floor(from.y);
        int z = (int) Math.floor(from.z);
        int endX = (int) Math.floor(to.x);
        int endY = (int) Math.floor(to.y);
        int endZ = (int) Math.floor(to.z);

        int stepX = Integer.signum((int) Math.signum(rdx));
        int stepY = Integer.signum((int) Math.signum(rdy));
        int stepZ = Integer.signum((int) Math.signum(rdz));

        double tDeltaX = rdx == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / rdx);
        double tDeltaY = rdy == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / rdy);
        double tDeltaZ = rdz == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / rdz);

        double tMaxX = rdx == 0 ? Double.POSITIVE_INFINITY
                : (stepX > 0 ? (x + 1 - from.x) : (from.x - x)) / Math.abs(rdx);
        double tMaxY = rdy == 0 ? Double.POSITIVE_INFINITY
                : (stepY > 0 ? (y + 1 - from.y) : (from.y - y)) / Math.abs(rdy);
        double tMaxZ = rdz == 0 ? Double.POSITIVE_INFINITY
                : (stepZ > 0 ? (z + 1 - from.z) : (from.z - z)) / Math.abs(rdz);

        int cachedCx = Integer.MIN_VALUE;
        int cachedCz = Integer.MIN_VALUE;
        ChunkAccess cachedChunk = null;

        for (int i = 0; i < MAX_STEPS; i++) {
            int cx = x >> 4;
            int cz = z >> 4;
            if (cx != cachedCx || cz != cachedCz) {
                cachedChunk = WorldUtil.getChunk(cx, cz, level, false);
                cachedCx = cx;
                cachedCz = cz;
            }
            if (cachedChunk != null) {
                int sectionIndex = level.getSectionIndex(y);
                LevelChunkSection[] sections = cachedChunk.getSections();
                if (sectionIndex >= 0 && sectionIndex < sections.length) {
                    LevelChunkSection section = sections[sectionIndex];
                    if (section != null && !section.hasOnlyAir()) {
                        BlockState state = section.getBlockState(x & 15, y & 15, z & 15);
                        ShieldingRegistry.Coeffs c = ShieldingRegistry.get(state);
                        if (c != null) {
                            sumX += c.xray();
                            sumN += c.neutron();
                        }
                    }
                }
            }
            if (x == endX && y == endY && z == endZ) break;
            double tNext = Math.min(Math.min(tMaxX, tMaxY), tMaxZ);
            if (tNext >= dist) break;
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) { x += stepX; tMaxX += tDeltaX; }
                else               { z += stepZ; tMaxZ += tDeltaZ; }
            } else {
                if (tMaxY < tMaxZ) { y += stepY; tMaxY += tDeltaY; }
                else               { z += stepZ; tMaxZ += tDeltaZ; }
            }
        }

        return new AttenuationResult(Math.exp(-sumX), Math.exp(-sumN));
    }
}
