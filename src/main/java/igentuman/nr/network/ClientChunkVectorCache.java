package igentuman.nr.network;

import net.minecraft.world.level.ChunkPos;

public final class ClientChunkVectorCache {
    private static volatile ChunkVectorDebugPayload latest;

    private ClientChunkVectorCache() {}

    public static void update(ChunkVectorDebugPayload p) {
        latest = p;
    }

    public static ChunkVectorDebugPayload get() { return latest; }

    public static boolean matches(ChunkPos pos) {
        ChunkVectorDebugPayload p = latest;
        return p != null && p.chunkX() == pos.x && p.chunkZ() == pos.z;
    }

    public static void clear() { latest = null; }
}
