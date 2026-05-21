package igentuman.nr.tracking;

import igentuman.nr.config.RadiationConfig;
import igentuman.nr.persistence.ChunkRadiationData;
import igentuman.nr.persistence.NRAttachments;
import igentuman.nr.simulation.RadiationSimulator;
import igentuman.nr.simulation.SourceSpatialIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldSourceRegistry {

    private static final Map<ServerLevel, WorldSourceRegistry> BY_LEVEL = new ConcurrentHashMap<>();

    public static WorldSourceRegistry get(ServerLevel level) {
        return BY_LEVEL.computeIfAbsent(level, WorldSourceRegistry::new);
    }

    public static void unload(ServerLevel level) {
        BY_LEVEL.remove(level);
    }

    private final ServerLevel level;
    private final Map<UUID, WorldRadSource> byId = new HashMap<>();
    private final Map<Long, WorldRadSource> byBlock = new HashMap<>();
    private final Map<UUID, ItemEntityRadSource> byItemEntity = new HashMap<>();

    private WorldSourceRegistry(ServerLevel level) {
        this.level = level;
    }

    public synchronized void register(WorldRadSource s) {
        if (s == null) return;
        byId.put(s.getId(), s);
        long pkey = packPos(s.getPosition());
        if (s instanceof ItemEntityRadSource ie) {
            byItemEntity.put(ie.entityUuid(), ie);
        } else {
            byBlock.put(pkey, s);
        }
        RadiationSimulator.get().addSource(level, s);
    }

    public synchronized void remove(UUID id) {
        WorldRadSource s = byId.remove(id);
        if (s == null) return;
        if (s instanceof ItemEntityRadSource ie) {
            byItemEntity.remove(ie.entityUuid());
        } else {
            byBlock.remove(packPos(s.getPosition()));
        }
        RadiationSimulator.get().removeSource(level, id);
    }

    public synchronized WorldRadSource atBlock(BlockPos pos) {
        return byBlock.get(packPos(pos));
    }

    public synchronized ItemEntityRadSource forItemEntity(UUID entityUuid) {
        return byItemEntity.get(entityUuid);
    }

    public synchronized Collection<WorldRadSource> all() {
        return new ArrayList<>(byId.values());
    }

    public synchronized int size() { return byId.size(); }

    public synchronized List<WorldRadSource> queryRadius(Vec3 center, double radius) {
        if (byId.isEmpty()) return List.of();
        List<WorldRadSource> out = new ArrayList<>();
        double r2 = radius * radius;
        for (WorldRadSource s : byId.values()) {
            BlockPos p = s.getPosition();
            double dx = p.getX() + 0.5 - center.x;
            double dy = p.getY() + 0.5 - center.y;
            double dz = p.getZ() + 0.5 - center.z;
            if (dx * dx + dy * dy + dz * dz <= r2) out.add(s);
        }
        return out;
    }

    public void tickDecay(long now) {
        List<UUID> dead = new ArrayList<>();
        double floor = RadiationConfig.ACTIVITY_FLOOR_BQ.get();
        for (WorldRadSource s : all()) {
            if (now >= s.expiryGameTime()) {
                dead.add(s.getId());
                continue;
            }
            if (s instanceof AbstractWorldRadSource a) a.advanceDecay(now);
            if (!s.isActive() || s.activityBq() < floor) {
                dead.add(s.getId());
            }
        }
        if (dead.isEmpty()) return;
        for (UUID id : dead) remove(id);
    }

    public void spreadContamination(long now, long intervalTicks) {
        double base = RadiationConfig.CONTAMINATION_SPREAD_FACTOR.get();
        if (base <= 0.0) return;
        for (WorldRadSource s : all()) {
            if (!s.contaminatesArea() || !s.isActive()) continue;
            ChunkPos cp = new ChunkPos(s.getPosition());
            LevelChunk chunk = level.getChunkSource().getChunkNow(cp.x, cp.z);
            if (chunk == null) continue;
            ChunkRadiationData data = chunk.getData(NRAttachments.CHUNK_RADIATION.get());
            double scale = base * intervalTicks;
            double soilShare;
            double airShare;
            double waterShare;
            if (s instanceof FluidRadSource) {
                soilShare = 0.3; waterShare = 0.6; airShare = 0.1;
            } else if (s instanceof ItemEntityRadSource) {
                soilShare = 0.4; waterShare = 0.0; airShare = 0.6;
            } else {
                soilShare = 0.7; waterShare = 0.0; airShare = 0.3;
            }
            if (soilShare > 0)  data.soil().mergeAtoms(s.getProfile(), scale * soilShare, now);
            if (waterShare > 0) data.water().mergeAtoms(s.getProfile(), scale * waterShare, now);
            if (airShare > 0)   data.air().mergeAtoms(s.getProfile(), scale * airShare, now);
            data.setLastDecayTick(now);
            data.markExpiryDirty();
            chunk.setUnsaved(true);
        }
    }

    private static long packPos(BlockPos p) {
        return ((long) p.getX() & 0x3FFFFFFL)
                | (((long) p.getZ() & 0x3FFFFFFL) << 26)
                | (((long) p.getY() & 0xFFFL) << 52);
    }
}
