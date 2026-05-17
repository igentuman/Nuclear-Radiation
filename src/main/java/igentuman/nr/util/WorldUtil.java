package igentuman.nr.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

import static net.minecraft.world.level.block.Blocks.AIR;

/**
 * Utility class for faster world-related operations.
 */
public class WorldUtil {

    public static BlockState getBlockState(BlockPos pos, ServerLevel level) {
        return getBlockState(pos, level, true);
    }

    public static BlockState getBlockState(BlockPos pos, ServerLevel level, boolean load) {
        if (level == null) {
            return AIR.defaultBlockState();
        }

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        ChunkAccess chunk = getChunk(chunkX, chunkZ, level, load);

        if (chunk == null) {
            return AIR.defaultBlockState();
        }

        int sectionIndex = level.getSectionIndex(pos.getY());
        if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) {
            return AIR.defaultBlockState();
        }

        LevelChunkSection section = chunk.getSections()[sectionIndex];
        if (section == null || section.hasOnlyAir()) {
            return AIR.defaultBlockState();
        }

        int localX = pos.getX() & 15;
        int localY = pos.getY() & 15;
        int localZ = pos.getZ() & 15;

        return section.getBlockState(localX, localY, localZ);
    }

    public static BlockEntity getBlockEntity(BlockPos pos, ServerLevel level) {
        return getBlockEntity(pos, level, true);
    }

    public static BlockEntity getBlockEntity(BlockPos pos, ServerLevel level, boolean load) {
        if (level == null) {
            return null;
        }

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        ChunkAccess chunk = getChunk(chunkX, chunkZ, level, load);

        if (chunk == null) {
            return null;
        }

        return chunk.getBlockEntity(pos);
    }

    public static ChunkAccess getChunk(int chunkX, int chunkZ, ServerLevel level) {
        return getChunk(chunkX, chunkZ, level, true);
    }

    public static ChunkAccess getChunk(int chunkX, int chunkZ, ServerLevel level, boolean load) {
        if (load) {
            return level.getChunkSource().getChunk(chunkX, chunkZ, true);
        }
        return level.getChunkSource().getChunkNow(chunkX, chunkZ);
    }
}
