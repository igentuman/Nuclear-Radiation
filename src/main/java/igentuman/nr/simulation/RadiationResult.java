package igentuman.nr.simulation;

import java.util.HashMap;
import java.util.Map;

public final class RadiationResult {

    public final Map<Long, SubChunkRadVector> vectorUpdates = new HashMap<>();
    public final long tick;

    public RadiationResult(long tick) {
        this.tick = tick;
    }
}
