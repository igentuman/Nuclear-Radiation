package igentuman.nr.simulation;

import net.minecraft.world.phys.Vec3;

public class ChunkRadVector {
    public Vec3 gradientXRay = Vec3.ZERO;
    public Vec3 gradientNeutron = Vec3.ZERO;
    public double centerScalarXRay;
    public double centerScalarNeutron;
    public double centerY;
    public double maxBq;
    public long computedTick;
    public long ttlTicks;

    public boolean isExpired(long now) {
        return now - computedTick > ttlTicks;
    }
}
