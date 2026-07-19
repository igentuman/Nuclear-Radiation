package igentuman.nr.integration.nuclearcraft;

import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.api.isotope.IsotopeStack;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.registry.Isotopes;

public class NCNHelper {

    public static RadiationProfile buildMeltdownProfile(double magnitude, long gameTime) {
        RadiationProfile profile = new RadiationProfile();
        double scale = Math.max(magnitude, 1.0);
        addStack(profile, Isotopes.XE_133, 4.5e16 * scale, gameTime);
        addStack(profile, Isotopes.CS_137, 4.2e16 * scale, gameTime);
        addStack(profile, Isotopes.SR_90,  2.1e16 * scale, gameTime);
        addStack(profile, Isotopes.I_131,  2.9e16 * scale, gameTime);
        addStack(profile, Isotopes.H_3,    1.2e16 * scale, gameTime);
        addStack(profile, Isotopes.KR_85,  8.0e15 * scale, gameTime);
        return profile;
    }

    private static void addStack(RadiationProfile profile, String isotopeId, double atoms, long gameTime) {
        Isotope iso = IsotopeRegistry.get(isotopeId);
        if (iso != null) {
            profile.put(new IsotopeStack(iso, atoms, gameTime));
        }
    }
}
