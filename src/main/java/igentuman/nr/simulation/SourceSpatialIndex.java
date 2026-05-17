package igentuman.nr.simulation;

import igentuman.nr.api.IRadiationSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SourceSpatialIndex {

    private final Map<UUID, IRadiationSource> byId = new HashMap<>();
    private final Map<Long, Set<UUID>> byChunk = new HashMap<>();

    public synchronized void add(IRadiationSource src) {
        if (src == null) return;
        byId.put(src.getId(), src);
        long key = chunkKey(src.getPosition());
        byChunk.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(src.getId());
    }

    public synchronized void remove(UUID id) {
        IRadiationSource src = byId.remove(id);
        if (src == null) return;
        long key = chunkKey(src.getPosition());
        Set<UUID> set = byChunk.get(key);
        if (set != null) {
            set.remove(id);
            if (set.isEmpty()) byChunk.remove(key);
        }
    }

    public synchronized IRadiationSource get(UUID id) {
        return byId.get(id);
    }

    public synchronized Collection<IRadiationSource> all() {
        return new ArrayList<>(byId.values());
    }

    public synchronized List<IRadiationSource> queryRadius(Vec3 center, double radius) {
        if (byId.isEmpty()) return Collections.emptyList();
        List<IRadiationSource> out = new ArrayList<>();
        int chunkR = (int) Math.ceil(radius / 16.0) + 1;
        int cx = (int) Math.floor(center.x) >> 4;
        int cz = (int) Math.floor(center.z) >> 4;
        double r2 = radius * radius;
        for (int dx = -chunkR; dx <= chunkR; dx++) {
            for (int dz = -chunkR; dz <= chunkR; dz++) {
                long key = chunkKey(cx + dx, cz + dz);
                Set<UUID> set = byChunk.get(key);
                if (set == null) continue;
                for (UUID id : set) {
                    IRadiationSource s = byId.get(id);
                    if (s == null) continue;
                    BlockPos p = s.getPosition();
                    double dxv = p.getX() + 0.5 - center.x;
                    double dyv = p.getY() + 0.5 - center.y;
                    double dzv = p.getZ() + 0.5 - center.z;
                    if (dxv * dxv + dyv * dyv + dzv * dzv <= r2) {
                        out.add(s);
                    }
                }
            }
        }
        return out;
    }

    public synchronized int size() { return byId.size(); }

    public synchronized void clear() {
        byId.clear();
        byChunk.clear();
    }

    private static long chunkKey(BlockPos pos) {
        return chunkKey(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public static long chunkKey(int cx, int cz) {
        return ((long) cx & 0xffffffffL) | (((long) cz & 0xffffffffL) << 32);
    }

    public static long chunkKey(ChunkPos pos) {
        return chunkKey(pos.x, pos.z);
    }
}
