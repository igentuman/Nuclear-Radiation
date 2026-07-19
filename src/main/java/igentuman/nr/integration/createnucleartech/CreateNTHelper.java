package igentuman.nr.integration.createnucleartech;

import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.api.isotope.IsotopeStack;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.registry.Isotopes;

public class CreateNTHelper {

    // NuclearExplosion passes centralStrength = max(1, cfg) * 450.
    private static final double NUKE_REFERENCE_STRENGTH = 450.0;

    // Nuclear blast fallout: fission-product signature, persists years+.
    public static RadiationProfile buildFalloutProfile(double centralStrength, int radiusChunks, long gameTime) {
        double scale = Math.max(centralStrength / NUKE_REFERENCE_STRENGTH, 0.1)
                * Math.max(1.0, radiusChunks / 4.0);
        RadiationProfile profile = new RadiationProfile();
        addStack(profile, Isotopes.XE_133, 6.0e16 * scale, gameTime);
        addStack(profile, Isotopes.I_131,  4.0e16 * scale, gameTime);
        addStack(profile, Isotopes.CS_137, 5.0e16 * scale, gameTime);
        addStack(profile, Isotopes.SR_90,  2.5e16 * scale, gameTime);
        addStack(profile, Isotopes.CO_60,  1.2e16 * scale, gameTime);
        addStack(profile, Isotopes.KR_85,  8.0e15 * scale, gameTime);
        addStack(profile, Isotopes.H_3,    1.2e16 * scale, gameTime);
        addStack(profile, Isotopes.PU_239, 3.0e14 * scale, gameTime);
        return profile;
    }

    // Containment leak / diffuse chunk radiation: smaller transient release.
    public static RadiationProfile buildLeakProfile(double strength, long gameTime) {
        double scale = Math.clamp(strength / 50.0, 0.05, 10.0);
        RadiationProfile profile = new RadiationProfile();
        addStack(profile, Isotopes.XE_133, 3.0e17 * scale, gameTime);
        addStack(profile, Isotopes.I_131,  2.0e17 * scale, gameTime);
        addStack(profile, Isotopes.CS_137, 1.5e17 * scale, gameTime);
        addStack(profile, Isotopes.SR_90,  8.0e16 * scale, gameTime);
        return profile;
    }

    private static void addStack(RadiationProfile profile, String isotopeId, double atoms, long gameTime) {
        Isotope iso = IsotopeRegistry.get(isotopeId);
        if (iso != null) {
            profile.put(new IsotopeStack(iso, atoms, gameTime));
        }
    }
}
