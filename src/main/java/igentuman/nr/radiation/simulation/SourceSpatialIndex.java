package igentuman.nr.radiation.simulation;

import igentuman.nr.api.shielding.IRadiationSource;
import net.minecraft.core.BlockPos;
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
    private final Map<UUID, Vec3> globalPosById = new HashMap<>();

    public synchronized void add(IRadiationSource src) {
        if (src == null) return;
        byId.put(src.getId(), src);
        long key = chunkKey(src.getPosition());
        byChunk.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(src.getId());
    }

    public synchronized void add(IRadiationSource src, Vec3 globalPos) {
        if (src == null) return;
        byId.put(src.getId(), src);
        globalPosById.put(src.getId(), globalPos);
        long key = chunkKey(globalPos);
        byChunk.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(src.getId());
    }

    public synchronized void updateGlobalPosition(UUID id, Vec3 newGlobalPos) {
        if (!byId.containsKey(id)) return;
        Vec3 old = globalPosById.get(id);
        long newKey = chunkKey(newGlobalPos);
        if (old != null && chunkKey(old) == newKey) {
            globalPosById.put(id, newGlobalPos);
            return;
        }
        if (old != null) {
            Set<UUID> oldSet = byChunk.get(chunkKey(old));
            if (oldSet != null) {
                oldSet.remove(id);
                if (oldSet.isEmpty()) byChunk.remove(chunkKey(old));
            }
        }
        byChunk.computeIfAbsent(newKey, k -> new LinkedHashSet<>()).add(id);
        globalPosById.put(id, newGlobalPos);
    }

    public synchronized void remove(UUID id) {
        IRadiationSource src = byId.remove(id);
        if (src == null) return;
        Vec3 globalPos = globalPosById.remove(id);
        long key = globalPos != null ? chunkKey(globalPos) : chunkKey(src.getPosition());
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
        int cy = (int) Math.floor(center.y) >> 4;
        int cz = (int) Math.floor(center.z) >> 4;
        double r2 = radius * radius;
        for (int dx = -chunkR; dx <= chunkR; dx++) {
            for (int dy = -chunkR; dy <= chunkR; dy++) {
                for (int dz = -chunkR; dz <= chunkR; dz++) {
                    long key = chunkKey(cx + dx, cy + dy, cz + dz);
                    Set<UUID> set = byChunk.get(key);
                    if (set == null) continue;
                    for (UUID id : set) {
                        IRadiationSource s = byId.get(id);
                        if (s == null) continue;
                        Vec3 gp = globalPosById.get(id);
                        double px, py, pz;
                        if (gp != null) {
                            px = gp.x; py = gp.y; pz = gp.z;
                        } else {
                            BlockPos p = s.getPosition();
                            px = p.getX() + 0.5; py = p.getY() + 0.5; pz = p.getZ() + 0.5;
                        }
                        double dxv = px - center.x;
                        double dyv = py - center.y;
                        double dzv = pz - center.z;
                        if (dxv * dxv + dyv * dyv + dzv * dzv <= r2) {
                            out.add(s);
                        }
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
        globalPosById.clear();
    }

    private static long chunkKey(BlockPos pos) {
        return chunkKey(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
    }

    private static long chunkKey(Vec3 pos) {
        return chunkKey((int) Math.floor(pos.x) >> 4, (int) Math.floor(pos.y) >> 4, (int) Math.floor(pos.z) >> 4);
    }

    // Pack section key: cx (22 bits) | cz (22 bits) | cy (20 bits).
    // Section height = 16. cy range ~ -524288..524287, more than enough for MC world bounds.
    public static long chunkKey(int cx, int cy, int cz) {
        return ((long)(cx & 0x3FFFFF))
             | (((long)(cz & 0x3FFFFF)) << 22)
             | (((long)(cy & 0xFFFFF)) << 44);
    }

    public static int unpackCx(long key) {
        int v = (int)(key & 0x3FFFFF);
        return (v << 10) >> 10;
    }

    public static int unpackCz(long key) {
        int v = (int)((key >>> 22) & 0x3FFFFF);
        return (v << 10) >> 10;
    }

    public static int unpackCy(long key) {
        int v = (int)((key >>> 44) & 0xFFFFF);
        return (v << 12) >> 12;
    }
}
