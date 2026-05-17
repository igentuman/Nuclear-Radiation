package igentuman.nr.simulation;

import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;

public final class RadiationResult {

    public final Map<ChunkPos, ChunkRadVector> vectorUpdates = new HashMap<>();
    public final long tick;

    public RadiationResult(long tick) {
        this.tick = tick;
    }
}
