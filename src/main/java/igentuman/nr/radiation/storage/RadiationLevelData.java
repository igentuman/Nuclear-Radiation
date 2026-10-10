package igentuman.nr.radiation.storage;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class RadiationLevelData extends SavedData {

    public static final String NAME = "nr_radiation";
    public static final int SCHEMA_VERSION = 1;

    private final Map<UUID, CompoundTag> sources = new LinkedHashMap<>();
    private long lastTick;

    public RadiationLevelData() {}

    private static final Codec<RadiationLevelData> CODEC = CompoundTag.CODEC.xmap(RadiationLevelData::load, RadiationLevelData::save);
    public static final SavedDataType<RadiationLevelData> TYPE =
            new SavedDataType<>(Identifier.fromNamespaceAndPath("nuclear_radiation", NAME), RadiationLevelData::new, CODEC);

    public static RadiationLevelData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
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

    private CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", SCHEMA_VERSION);
        tag.putLong("last_tick", lastTick);
        ListTag list = new ListTag();
        for (Map.Entry<UUID, CompoundTag> e : sources.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putIntArray("id", UUIDUtil.uuidToIntArray(e.getKey()));
            entry.put("data", e.getValue());
            list.add(entry);
        }
        tag.put("sources", list);
        return tag;
    }

    private static RadiationLevelData load(CompoundTag tag) {
        RadiationLevelData d = new RadiationLevelData();
        d.lastTick = tag.getLongOr("last_tick", 0L);
        ListTag list = tag.getListOrEmpty("sources");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompoundOrEmpty(i);
            entry.getIntArray("id").filter(bytes -> bytes.length == 4).ifPresent(bytes ->
                    d.sources.put(UUIDUtil.uuidFromIntArray(bytes), entry.getCompoundOrEmpty("data")));
        }
        return d;
    }
}
