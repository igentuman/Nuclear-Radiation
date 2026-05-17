package igentuman.nr.api;

import igentuman.nr.core.RadiationProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public interface IRadiationSource {
    UUID getId();

    RadiationProfile getProfile();

    BlockPos getPosition();

    ResourceKey<Level> getDimension();

    double activityBq();

    boolean isActive();
}
