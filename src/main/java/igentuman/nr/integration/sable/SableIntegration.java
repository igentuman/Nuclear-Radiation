package igentuman.nr.integration.sable;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

/**
 * Wraps {@link SableCompanion#INSTANCE} for translating sub-level (plot-grid) positions
 * to global world-space. Sable is JiJ'd with a {@code DefaultSableCompanion} that returns
 * identity values when the real Sable mod is absent, so these calls are always safe.
 *
 * Must only be called from the main thread — sub-level pose data is not thread-safe.
 */
public final class SableIntegration {

    private SableIntegration() {}

    public static Vec3 projectPosition(ServerLevel level, Vec3 pos) {
        return SableCompanion.INSTANCE.projectOutOfSubLevel(level, (net.minecraft.core.Position) pos);
    }

    public static BlockPos projectBlockPos(ServerLevel level, BlockPos pos) {
        return BlockPos.containing(projectPosition(level, Vec3.atCenterOf(pos)));
    }

    public static ChunkPos projectChunkPos(ServerLevel level, BlockPos pos) {
        return new ChunkPos(projectBlockPos(level, pos));
    }

    public static boolean isInSubLevel(ServerLevel level, BlockPos pos) {
        return SableCompanion.INSTANCE.getContaining(level, pos) != null;
    }

    public static double distanceSquared(ServerLevel level, Vec3 a, Vec3 b) {
        return SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level, a.x, a.y, a.z, b.x, b.y, b.z);
    }
}
