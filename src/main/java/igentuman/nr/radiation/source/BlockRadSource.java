package igentuman.nr.radiation.source;

import igentuman.nr.api.AbstractWorldRadSource;
import igentuman.nr.api.RadiationProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class BlockRadSource extends AbstractWorldRadSource {

    public boolean contaminatesArea = false;

    public BlockRadSource(UUID id, ResourceKey<Level> dim, BlockPos pos,
                          RadiationProfile profile, long spawnedTick) {
        this(id, dim, pos, profile, spawnedTick, true);
    }

    public BlockRadSource(UUID id, ResourceKey<Level> dim, BlockPos pos,
                          RadiationProfile profile, long spawnedTick, boolean contamiateArea) {
        super(id, dim, pos, profile, spawnedTick);
        this.contaminatesArea = contamiateArea;
    }

    @Override
    public boolean contaminatesArea() { return contaminatesArea; }
}
