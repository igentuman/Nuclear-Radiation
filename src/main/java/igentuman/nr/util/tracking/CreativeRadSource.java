package igentuman.nr.util.tracking;

import igentuman.nr.api.RadiationProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class CreativeRadSource implements WorldRadSource {

    private final UUID id;
    private final ResourceKey<Level> dimension;
    private final BlockPos pos;
    private double alphaBq;
    private double betaBq;
    private double xRayBq;
    private double neutronBq;
    private final long spawnedTick;

    public CreativeRadSource(UUID id, ResourceKey<Level> dim, BlockPos pos,
                              double alphaBq, double betaBq, double xRayBq, double neutronBq,
                              long spawnedTick) {
        this.id = id;
        this.dimension = dim;
        this.pos = pos;
        this.alphaBq = alphaBq;
        this.betaBq = betaBq;
        this.xRayBq = xRayBq;
        this.neutronBq = neutronBq;
        this.spawnedTick = spawnedTick;
    }

    public void update(double alpha, double beta, double xray, double neutron) {
        this.alphaBq = alpha;
        this.betaBq = beta;
        this.xRayBq = xray;
        this.neutronBq = neutron;
    }

    @Override public UUID getId()                        { return id; }
    @Override public ResourceKey<Level> getDimension()  { return dimension; }
    @Override public BlockPos getPosition()             { return pos; }
    @Override public RadiationProfile getProfile()      { return RadiationProfile.empty(); }
    @Override public long spawnedTick()                 { return spawnedTick; }
    @Override public boolean contaminatesArea()         { return false; }
    @Override public long expiryGameTime()              { return Long.MAX_VALUE; }
    @Override public boolean isActive()                 { return activityBq() > 0.0; }
    @Override public double activityBq()                { return alphaBq + betaBq + xRayBq + neutronBq; }
    @Override public double radius()                    { return 32.0; }
    @Override public Vec3 emissionCenter() {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }
    @Override public double xRayBq()    { return xRayBq; }
    @Override public double alphaBq()   { return alphaBq; }
    @Override public double betaBq()    { return betaBq; }
    @Override public double neutronBq() { return neutronBq; }
}
