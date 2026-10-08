package igentuman.nr.integration.projectexplosive;

import igentuman.nr.api.RadiationProfile;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.integration.createnucleartech.CreateNTHelper;
import igentuman.nr.radiation.source.WorldSourceRegistry;
import igentuman.nr.radiation.storage.ChunkRadiationData;
import igentuman.nr.radiation.storage.NRAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Remembers PE's short-lived zone so chunks loaded after a blast receive fallout once. */
public final class ProjectExplosiveFalloutZones extends SavedData {
    private static final String DATA_NAME = "nuclear_radiation_projectexplosive_zones";
    private static final long ZONE_LIFETIME_TICKS = 48_000L;
    private final List<Zone> zones = new ArrayList<>();

    private ProjectExplosiveFalloutZones() {}

    private ProjectExplosiveFalloutZones(CompoundTag tag) {
        ListTag list = tag.getList("zones", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            Zone zone = new Zone(entry.getDouble("x"), entry.getDouble("z"),
                    entry.getDouble("radius"), entry.getLong("start"));
            for (long applied : entry.getLongArray("applied")) zone.applied.add(applied);
            if (Double.isFinite(zone.x) && Double.isFinite(zone.z)
                    && Double.isFinite(zone.radius) && zone.radius > 0) zones.add(zone);
        }
    }

    private static ProjectExplosiveFalloutZones get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ProjectExplosiveFalloutZones::new,
                        (tag, provider) -> new ProjectExplosiveFalloutZones(tag)), DATA_NAME);
    }

    public static void register(ServerLevel level, double x, double z, double radius) {
        ProjectExplosiveFalloutZones data = get(level);
        long now = level.getGameTime();
        data.prune(now);
        Zone zone = new Zone(x, z, radius, now);
        data.zones.add(zone);
        data.setDirty();
        int chunkRadius = (int) Math.ceil(radius / 16.0);
        ChunkPos origin = new ChunkPos((int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4);
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(origin.x + dx, origin.z + dz);
                if (chunk != null) data.apply(level, chunk, zone);
            }
        }
    }

    public static void onChunkLoad(ServerLevel level, LevelChunk chunk) {
        if (!RadiationConfig.chunkContaminationEnabled()) return;
        ProjectExplosiveFalloutZones data = get(level);
        data.prune(level.getGameTime());
        for (Zone zone : data.zones) data.apply(level, chunk, zone);
    }

    private void prune(long now) {
        if (zones.removeIf(zone -> now - zone.start >= ZONE_LIFETIME_TICKS)) setDirty();
    }

    private void apply(ServerLevel level, LevelChunk chunk, Zone zone) {
        if (!RadiationConfig.chunkContaminationEnabled()) return;
        ChunkPos pos = chunk.getPos();
        long key = pos.toLong();
        if (zone.applied.contains(key)) return;
        double dx = pos.getMinBlockX() + 8.0 - zone.x;
        double dz = pos.getMinBlockZ() + 8.0 - zone.z;
        double normalized = Math.sqrt(dx * dx + dz * dz) / zone.radius;
        if (normalized >= 1.0) return;

        long now = level.getGameTime();
        RadiationProfile fallout = CreateNTHelper.buildFalloutProfile(450.0, 4, zone.start);
        fallout.advanceDecay(now);
        double strength = 0.005 * (1.0 - normalized) * (1.0 - normalized);
        ChunkRadiationData radiation = chunk.getData(NRAttachments.CHUNK_RADIATION.get());
        radiation.air().mergeAtoms(fallout, strength, now);
        radiation.soil().mergeAtoms(fallout, strength * 0.5, now);
        radiation.setLastDecayTick(now);
        radiation.markExpiryDirty();
        WorldSourceRegistry.get(level).markChunkContaminated(pos);
        chunk.setUnsaved(true);
        zone.applied.add(key);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Zone zone : zones) {
            CompoundTag entry = new CompoundTag();
            entry.putDouble("x", zone.x);
            entry.putDouble("z", zone.z);
            entry.putDouble("radius", zone.radius);
            entry.putLong("start", zone.start);
            entry.putLongArray("applied", zone.applied.stream().mapToLong(Long::longValue).toArray());
            list.add(entry);
        }
        tag.put("zones", list);
        return tag;
    }

    private static final class Zone {
        final double x;
        final double z;
        final double radius;
        final long start;
        final Set<Long> applied = new HashSet<>();

        Zone(double x, double z, double radius, long start) {
            this.x = x;
            this.z = z;
            this.radius = radius;
            this.start = start;
        }
    }
}
