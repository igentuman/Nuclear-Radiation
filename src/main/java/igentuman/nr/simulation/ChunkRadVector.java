package igentuman.nr.simulation;

import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ChunkRadVector {
    public record Contrib(double x, double y, double z, double xRayBq, double neutronBq) {}

    public Vec3 gradientXRay = Vec3.ZERO;
    public Vec3 gradientNeutron = Vec3.ZERO;
    public double centerScalarXRay;
    public double centerScalarNeutron;
    public double centerY;
    public double maxBq;
    public long computedTick;
    public long ttlTicks;
    public List<Contrib> contribs = List.of();

    public boolean isExpired(long now) {
        return now - computedTick > ttlTicks;
    }
}
