package igentuman.nr.api;

import igentuman.nr.config.RadiationConfig;
import igentuman.nr.api.isotope.IsotopeStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public abstract class AbstractWorldRadSource implements DecayGraph.WorldRadSource {

    protected final UUID id;
    protected final ResourceKey<Level> dimension;
    protected BlockPos pos;
    protected final RadiationProfile profile;
    protected final long spawnedTick;
    protected boolean alive = true;
    protected long expiryGameTime;

    protected AbstractWorldRadSource(UUID id, ResourceKey<Level> dim, BlockPos pos,
                                     RadiationProfile profile, long spawnedTick) {
        this.id = id;
        this.dimension = dim;
        this.pos = pos;
        this.profile = profile;
        this.spawnedTick = spawnedTick;
        this.expiryGameTime = computeExpiry();
    }

    private long computeExpiry() {
        return profile.expiryTick(RadiationConfig.ACTIVITY_FLOOR_BQ.get());
    }

    public void recomputeExpiry() {
        this.expiryGameTime = computeExpiry();
    }

    // Reset atoms to the fresh profile (no accumulation) and push expiry back out.
    // Used to refresh a re-asserted source in place instead of stacking duplicates.
    public void refresh(RadiationProfile fresh) {
        for (IsotopeStack s : fresh.stacks()) profile.put(s);
        recomputeExpiry();
    }

    @Override public long expiryGameTime() { return expiryGameTime; }

    @Override public UUID getId() { return id; }
    @Override public ResourceKey<Level> getDimension() { return dimension; }
    @Override public BlockPos getPosition() { return pos; }
    @Override public RadiationProfile getProfile() { return profile; }
    @Override public long spawnedTick() { return spawnedTick; }
    @Override public boolean isActive() { return alive && Math.abs(profile.totalActivityBq()) > 0.0; }

    public void markDead() { alive = false; }

    @Override public double activityBq() { return profile.totalActivityBq(); }
    @Override public double radius() { return 32.0; }
    @Override public Vec3 emissionCenter() {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }
    @Override public double xRayBq()    { return profile.xRayActivityBq(); }
    @Override public double alphaBq()   { return profile.alphaActivityBq(); }
    @Override public double betaBq()    { return profile.betaActivityBq(); }
    @Override public double neutronBq() { return profile.neutronActivityBq(); }

    public void advanceDecay(long currentTick) {
        this.expiryGameTime = profile.advanceDecay(currentTick, RadiationConfig.ACTIVITY_FLOOR_BQ.get());
    }
}
