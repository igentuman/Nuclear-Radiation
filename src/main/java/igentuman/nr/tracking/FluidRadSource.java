package igentuman.nr.tracking;

import igentuman.nr.core.RadiationProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class FluidRadSource extends AbstractWorldRadSource {

    public FluidRadSource(UUID id, ResourceKey<Level> dim, BlockPos pos,
                          RadiationProfile profile, long spawnedTick) {
        super(id, dim, pos, profile, spawnedTick);
    }

    @Override
    public boolean contaminatesArea() { return true; }
}
