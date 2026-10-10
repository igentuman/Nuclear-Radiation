package igentuman.nr.radiation.source;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

/** World coordinates for the core simulation while sub-level integrations are unavailable. */
public final class WorldProjection {
    private WorldProjection() {}

    public static Vec3 projectPosition(ServerLevel level, Vec3 position) {
        return position;
    }

    public static BlockPos projectBlockPos(ServerLevel level, BlockPos position) {
        return position;
    }

    public static ChunkPos projectChunkPos(ServerLevel level, BlockPos position) {
        return ChunkPos.containing(position);
    }

    public static boolean isInSubLevel(ServerLevel level, BlockPos position) {
        return false;
    }
}
