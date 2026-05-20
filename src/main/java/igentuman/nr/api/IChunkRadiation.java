package igentuman.nr.api;

import net.minecraft.world.level.ChunkPos;

public interface IChunkRadiation {
    ChunkPos chunk();

    RadiationProfile soil();

    RadiationProfile air();

    RadiationProfile water();
}
