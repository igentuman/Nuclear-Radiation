package igentuman.nr.radiation.storage;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class CoriumSavedData extends SavedData {

    private static final String ID = "nr_corium";

    private static final Factory<CoriumSavedData> FACTORY =
            new Factory<>(CoriumSavedData::new, CoriumSavedData::load, null);

    private final Long2IntOpenHashMap cooling = new Long2IntOpenHashMap();

    public CoriumSavedData() {
        cooling.defaultReturnValue(0);
    }

    public static CoriumSavedData getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, ID);
    }

    public static CoriumSavedData get(ServerLevel level) {
        return level.getDataStorage().get(FACTORY, ID);
    }

    private static CoriumSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        CoriumSavedData data = new CoriumSavedData();
        long[] keys = tag.getLongArray("positions");
        int[] counts = tag.getIntArray("cooling");
        for (int i = 0; i < keys.length; i++) {
            data.cooling.put(keys[i], i < counts.length ? counts[i] : 0);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        long[] keys = cooling.keySet().toLongArray();
        int[] counts = new int[keys.length];
        for (int i = 0; i < keys.length; i++) {
            counts[i] = cooling.get(keys[i]);
        }
        tag.putLongArray("positions", keys);
        tag.putIntArray("cooling", counts);
        return tag;
    }

    public boolean isEmpty() {
        return cooling.isEmpty();
    }

    public long[] snapshot() {
        return cooling.keySet().toLongArray();
    }

    public void track(long pos) {
        if (!cooling.containsKey(pos)) {
            cooling.put(pos, 0);
            setDirty();
        }
    }

    public void untrack(long pos) {
        if (cooling.containsKey(pos)) {
            cooling.remove(pos);
            setDirty();
        }
    }

    /** Adds water-contact cooling to a tracked position and returns the new total. */
    public int addCooling(long pos, int amount) {
        int total = cooling.get(pos) + amount;
        cooling.put(pos, total);
        setDirty();
        return total;
    }
}
