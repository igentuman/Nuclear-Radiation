package igentuman.nr.util.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class RadiationLevelData extends SavedData {

    public static final String NAME = "nr_radiation";
    public static final int SCHEMA_VERSION = 1;

    private final Map<UUID, CompoundTag> sources = new LinkedHashMap<>();
    private long lastTick;

    public RadiationLevelData() {}

    public static Factory<RadiationLevelData> factory() {
        return new Factory<>(RadiationLevelData::new, RadiationLevelData::load);
    }

    public static RadiationLevelData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), NAME);
    }

    public Map<UUID, CompoundTag> sources() {
        return sources;
    }

    public long lastTick() { return lastTick; }

    public void setLastTick(long t) {
        if (t != this.lastTick) {
            this.lastTick = t;
            setDirty();
        }
    }

    public void putSource(UUID id, CompoundTag data) {
        sources.put(id, data);
        setDirty();
    }

    public void removeSource(UUID id) {
        if (sources.remove(id) != null) setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        tag.putInt("version", SCHEMA_VERSION);
        tag.putLong("last_tick", lastTick);
        ListTag list = new ListTag();
        for (Map.Entry<UUID, CompoundTag> e : sources.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("id", e.getKey());
            entry.put("data", e.getValue());
            list.add(entry);
        }
        tag.put("sources", list);
        return tag;
    }

    public static RadiationLevelData load(CompoundTag tag, HolderLookup.Provider lookup) {
        RadiationLevelData d = new RadiationLevelData();
        d.lastTick = tag.getLong("last_tick");
        ListTag list = tag.getList("sources", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID id = entry.getUUID("id");
            CompoundTag data = entry.getCompound("data");
            d.sources.put(id, data);
        }
        return d;
    }
}
