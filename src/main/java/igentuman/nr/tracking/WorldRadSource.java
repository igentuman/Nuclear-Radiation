package igentuman.nr.tracking;

import igentuman.nr.api.IPointRadiationSource;
import igentuman.nr.api.RadiationProfile;

public interface WorldRadSource extends IPointRadiationSource {

    long spawnedTick();

    @Override
    RadiationProfile getProfile();

    boolean contaminatesArea();

    default long expiryGameTime() { return Long.MAX_VALUE; }
}
