package igentuman.nr.client.particle;

import igentuman.nr.NuclearRadiation;
import net.minecraft.core.BlockPos;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Persists active radiation-plume emission sources per dimension and emits the plume particles
 * each server tick until a source expires. Sources are registered by external mods on events
 * such as a fission meltdown via {@link #emit(ServerLevel, BlockPos)}.
 */
public class MeltdownParticles extends SavedData {

    public static final int DEFAULT_DURATION_TICKS = 2400;

    private static final String ID = "nr_meltdown_particles";
    private static final Codec<MeltdownParticles> CODEC = CompoundTag.CODEC.xmap(MeltdownParticles::load, MeltdownParticles::save);
    private static final SavedDataType<MeltdownParticles> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(NuclearRadiation.MODID, ID), MeltdownParticles::new, CODEC);

    private static final int PARTICLES_PER_EMIT = 6;
    private static final double SPREAD_XZ = 0.4;
    private static final double SPREAD_Y = 3;
    private static final double UPWARD_SPEED = 0.18;

    private final List<Entry> entries = new ArrayList<>();

    public record Entry(long pos, long expiryTick) {}

    public static MeltdownParticles getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public static void emit(ServerLevel level, BlockPos center) {
        emit(level, center, DEFAULT_DURATION_TICKS);
    }

    public static void emit(ServerLevel level, BlockPos center, int durationTicks) {
        getOrCreate(level).register(center, level.getGameTime() + durationTicks);
    }

    public void register(BlockPos center, long expiryTick) {
        entries.add(new Entry(center.asLong(), expiryTick));
        setDirty();
    }

    /** Emits one particle burst per active source and prunes expired ones. Call every server tick. */
    public void spawnTick(ServerLevel level) {
        if (entries.isEmpty()) return;
        long now = level.getGameTime();
        if (entries.removeIf(e -> e.expiryTick() <= now)) {
            setDirty();
        }
        for (Entry entry : entries) {
            BlockPos center = BlockPos.of(entry.pos());
            level.sendParticles(
                    NuclearRadiation.RADIATION_PARTICLE.get(),
                    center.getX() + 0.5, center.getY() + 2.0, center.getZ() + 0.5,
                    PARTICLES_PER_EMIT,
                    SPREAD_XZ, SPREAD_Y, SPREAD_XZ,
                    UPWARD_SPEED);
        }
    }

    private static MeltdownParticles load(CompoundTag tag) {
        MeltdownParticles data = new MeltdownParticles();
        ListTag list = tag.getListOrEmpty("meltdowns");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompoundOrEmpty(i);
            data.entries.add(new Entry(entry.getLongOr("pos", 0L), entry.getLongOr("expiry", 0L)));
        }
        return data;
    }

    private CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Entry e : entries) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("pos", e.pos());
            entry.putLong("expiry", e.expiryTick());
            list.add(entry);
        }
        tag.put("meltdowns", list);
        return tag;
    }
}
