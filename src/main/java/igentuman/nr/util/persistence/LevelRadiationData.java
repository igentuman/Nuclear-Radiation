package igentuman.nr.util.persistence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nr.api.RadiationProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LevelRadiationData {

    public record SourceEntry(
            String type,
            UUID id,
            ResourceKey<Level> dimension,
            BlockPos pos,
            RadiationProfile profile,
            long spawnedTick,
            boolean contaminatesArea
    ) {}

    private static final Codec<SourceEntry> ENTRY_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("type").forGetter(SourceEntry::type),
            UUIDUtil.CODEC.fieldOf("id").forGetter(SourceEntry::id),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(SourceEntry::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(SourceEntry::pos),
            RadiationProfileCodec.CODEC.fieldOf("profile").forGetter(SourceEntry::profile),
            Codec.LONG.fieldOf("spawned_tick").forGetter(SourceEntry::spawnedTick),
            Codec.BOOL.optionalFieldOf("contaminates", true).forGetter(SourceEntry::contaminatesArea)
    ).apply(inst, SourceEntry::new));

    public static final Codec<LevelRadiationData> CODEC = ENTRY_CODEC.listOf()
            .xmap(LevelRadiationData::new, d -> d.sources);

    private final List<SourceEntry> sources;

    public LevelRadiationData() {
        this.sources = new ArrayList<>();
    }

    public LevelRadiationData(List<SourceEntry> sources) {
        this.sources = new ArrayList<>(sources);
    }

    public List<SourceEntry> sources() {
        return sources;
    }

    public void add(SourceEntry entry) {
        sources.add(entry);
    }

    public void clear() {
        sources.clear();
    }
}
