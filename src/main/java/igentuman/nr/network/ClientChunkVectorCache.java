package igentuman.nr.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;

public final class ClientChunkVectorCache {
    private static volatile ChunkVectorDebugPayload latest;
    public static long lastUpdateTime;

    private ClientChunkVectorCache() {}

    public static void update(ChunkVectorDebugPayload p) {
        latest = p;
        assert Minecraft.getInstance().level != null;
        lastUpdateTime = Minecraft.getInstance().level.getGameTime();
    }

    public static ChunkVectorDebugPayload get() {
        assert Minecraft.getInstance().level != null;
        if (lastUpdateTime < Minecraft.getInstance().level.getGameTime() - 20) {
            latest = null;
        }
        return latest;
    }

    public static boolean matches(ChunkPos pos, int cy) {
        ChunkVectorDebugPayload p = latest;
        return p != null && p.chunkX() == pos.x && p.chunkY() == cy && p.chunkZ() == pos.z;
    }

    public static void clear() { latest = null; }
}
