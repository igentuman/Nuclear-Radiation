package igentuman.nr.api;

import igentuman.nr.api.shielding.IRadiationSource;
import net.minecraft.world.phys.Vec3;

public interface IPointRadiationSource extends IRadiationSource {
    double radius();

    Vec3 emissionCenter();

    double xRayBq();

    double alphaBq();

    double betaBq();

    double neutronBq();
}
