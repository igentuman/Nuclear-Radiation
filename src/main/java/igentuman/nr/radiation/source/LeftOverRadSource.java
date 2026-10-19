package igentuman.nr.radiation.source;

import igentuman.nr.api.AbstractWorldRadSource;
import igentuman.nr.api.RadiationProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class LeftOverRadSource extends AbstractWorldRadSource {

    private static final long ITEM_RESIDUE_LIFETIME_TICKS = 1200;

    public final boolean contaminatesArea;
    private final boolean itemResidue;

    public LeftOverRadSource(Level level, BlockPos pos, RadiationProfile profile, long spawnedTick, boolean contaminatesArea) {
        super(UUID.randomUUID(), level.dimension(), pos, profile, spawnedTick);
        this.contaminatesArea = contaminatesArea;
        this.itemResidue = false;
    }

    public LeftOverRadSource(UUID id, ResourceKey<Level> dimension, BlockPos pos, RadiationProfile profile, long spawnedTick, boolean contaminatesArea) {
        this(id, dimension, pos, profile, spawnedTick, contaminatesArea, false);
    }

    public LeftOverRadSource(UUID id, ResourceKey<Level> dimension, BlockPos pos, RadiationProfile profile,
                             long spawnedTick, boolean contaminatesArea, boolean itemResidue) {
        super(id, dimension, pos, profile, spawnedTick);
        this.contaminatesArea = contaminatesArea;
        this.itemResidue = itemResidue;
    }

    public static LeftOverRadSource fromItem(Level level, BlockPos pos, RadiationProfile profile, long spawnedTick) {
        return new LeftOverRadSource(UUID.randomUUID(), level.dimension(), pos, profile, spawnedTick, false, true);
    }

    public boolean isItemResidue() { return itemResidue; }

    @Override
    public long expiryGameTime() {
        return itemResidue ? Math.min(super.expiryGameTime(), spawnedTick + ITEM_RESIDUE_LIFETIME_TICKS)
                : super.expiryGameTime();
    }

    @Override
    public boolean contaminatesArea() { return contaminatesArea; }
}
