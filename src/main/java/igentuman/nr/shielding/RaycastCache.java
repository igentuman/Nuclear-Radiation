package igentuman.nr.shielding;

import net.minecraft.world.level.ChunkPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RaycastCache {

    private record Key(long srcChunk, long tgtChunk) {}

    private final Map<Key, AttenuationResult> cache = new ConcurrentHashMap<>();

    public AttenuationResult get(ChunkPos src, ChunkPos tgt) {
        return cache.get(new Key(src.toLong(), tgt.toLong()));
    }

    public void put(ChunkPos src, ChunkPos tgt, AttenuationResult res) {
        cache.put(new Key(src.toLong(), tgt.toLong()), res);
    }

    public void invalidateChunk(ChunkPos pos) {
        long key = pos.toLong();
        cache.keySet().removeIf(k -> k.srcChunk == key || k.tgtChunk == key);
    }

    public void clear() { cache.clear(); }

    public int size() { return cache.size(); }
}
