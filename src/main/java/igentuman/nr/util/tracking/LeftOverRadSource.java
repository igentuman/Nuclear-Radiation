package igentuman.nr.util.tracking;

import igentuman.nr.api.RadiationProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class LeftOverRadSource extends AbstractWorldRadSource {

    public final boolean contaminatesArea;

    public LeftOverRadSource(Level level, BlockPos pos, RadiationProfile profile, long spawnedTick, boolean contaminatesArea) {
        super(UUID.randomUUID(), level.dimension(), pos, profile, spawnedTick);
        this.contaminatesArea = contaminatesArea;
    }

    public LeftOverRadSource(UUID id, ResourceKey<Level> dimension, BlockPos pos, RadiationProfile profile, long spawnedTick, boolean contaminatesArea) {
        super(id, dimension, pos, profile, spawnedTick);
        this.contaminatesArea = contaminatesArea;
    }

    @Override
    public boolean contaminatesArea() { return contaminatesArea; }
}
