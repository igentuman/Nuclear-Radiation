package igentuman.nr.integration.nuclear_science;

import igentuman.nr.api.Isotope;
import igentuman.nr.api.IsotopeStack;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.registry.IsotopeRegistry;
import igentuman.nr.registry.Isotopes;
import voltaic.api.radiation.SimpleRadiationSource;

public class NuclearScienceHelper {

    public static RadiationProfile buildProfile(SimpleRadiationSource source, long gameTime) {
        double scale = Math.max(source.amount() * 0.01, 1.0);
        return source.shouldLinger()
                ? buildLongTerm(scale, gameTime)
                : buildShortTerm(scale, gameTime);
    }

    // Transient release: noble gas + iodine puff, fades in days–weeks.
    private static RadiationProfile buildShortTerm(double scale, long gameTime) {
        RadiationProfile profile = new RadiationProfile();
        addStack(profile, Isotopes.XE_133, 6.0e16 * scale, gameTime);
        addStack(profile, Isotopes.I_131,  3.5e16 * scale, gameTime);
        addStack(profile, Isotopes.PO_210, 5.0e15 * scale, gameTime);
        return profile;
    }

    // Lasting contamination: fallout signature, persists years+.
    private static RadiationProfile buildLongTerm(double scale, long gameTime) {
        RadiationProfile profile = new RadiationProfile();
        addStack(profile, Isotopes.CS_137, 4.2e16 * scale, gameTime);
        addStack(profile, Isotopes.SR_90,  2.1e16 * scale, gameTime);
        addStack(profile, Isotopes.CO_60,  1.5e16 * scale, gameTime);
        addStack(profile, Isotopes.KR_85,  8.0e15 * scale, gameTime);
        addStack(profile, Isotopes.H_3,    1.2e16 * scale, gameTime);
        addStack(profile, Isotopes.PU_239, 2.0e14 * scale, gameTime);
        return profile;
    }

    private static void addStack(RadiationProfile profile, String isotopeId, double atoms, long gameTime) {
        Isotope iso = IsotopeRegistry.get(isotopeId);
        if (iso != null) {
            profile.put(new IsotopeStack(iso, atoms, gameTime));
        }
    }
}
