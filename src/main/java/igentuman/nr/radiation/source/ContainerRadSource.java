package igentuman.nr.radiation.source;

import igentuman.nr.api.AbstractWorldRadSource;
import igentuman.nr.api.RadiationProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class ContainerRadSource extends AbstractWorldRadSource {

    private double containerAttenuation;

    public ContainerRadSource(UUID id, ResourceKey<Level> dim, BlockPos pos,
                              RadiationProfile profile, long spawnedTick,
                              double containerAttenuation) {
        super(id, dim, pos, profile, spawnedTick);
        this.containerAttenuation = containerAttenuation;
    }

    public double containerAttenuation() { return containerAttenuation; }
    public void setContainerAttenuation(double v) { this.containerAttenuation = v; }

    @Override public double xRayBq() {
        return super.xRayBq() * (1.0 - containerAttenuation);
    }

    @Override public double neutronBq() {
        return super.neutronBq() * (1.0 - containerAttenuation);
    }

    @Override public double alphaBq() { return 0.0; }
    @Override public double betaBq()  { return 0.0; }

    @Override
    public boolean contaminatesArea() { return false; }
}
