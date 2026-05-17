package igentuman.nr.api;

import igentuman.nr.core.RadiationQuality;

import java.util.Optional;

public interface Isotope {
    String id();

    float xRayStrength();

    float alphaBetaStrength();

    float neutronStrength();

    long halfLifeTicks();

    Optional<Isotope> decaysTo();

    RadiationQuality quality();
}
