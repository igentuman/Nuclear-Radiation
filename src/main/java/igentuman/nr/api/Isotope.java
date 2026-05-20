package igentuman.nr.api;

import java.util.Optional;

public interface Isotope {
    String id();

    float xRayStrength();

    float alphaStrength();

    float betaStrength();

    float neutronStrength();

    long halfLifeTicks();

    Optional<Isotope> decaysTo();

    RadiationQuality quality();
}
