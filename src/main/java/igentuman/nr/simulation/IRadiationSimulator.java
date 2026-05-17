package igentuman.nr.simulation;

import igentuman.nr.api.IChunkRadiation;
import igentuman.nr.api.IRadiationSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.List;

public interface IRadiationSimulator {
    void tick(ServerLevel level);

    void tickWorld(ServerLevel level);

    void tickEntities(ServerLevel level);

    void submitSources(List<IRadiationSource> sources);

    void submitChunks(List<IChunkRadiation> chunks);

    ChunkRadVector getChunkVector(ChunkPos pos);
}
