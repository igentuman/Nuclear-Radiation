package igentuman.nr.compat.ntm;

import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.api.isotope.IsotopeStack;
import igentuman.nr.util.Units;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * NTM-free item calibration: native carried radiation (RAD/s per item) to an NR isotope profile.
 * Material identity only chooses isotopes; the native level alone sets the dose-equivalent magnitude,
 * so relative NTM balance survives different isotope qualities.
 */
public final class NtmItemProfiles {
    private NtmItemProfiles() {}

    /** Spent/processed products are not their source material; leave them on the fallback mixture. */
    private static final Set<String> NON_SOURCE_TOKENS = Set.of("waste", "depleted", "spent", "fallout");

    /** Provisional activity fractions. Only exact real-isotope or explicit fictional matches are listed. */
    public static List<String> defaultCompositions() {
        return List.of(
                "uranium=nr:u_238:0.49,nr:u_234:0.49,nr:u_235:0.02",
                "u233=nr:u_233:1",
                "u235=nr:u_235:1",
                "u238=nr:u_238:1",
                "thorium=nr:th_232:1",
                "plutonium=nr:pu_238:0.05,nr:pu_239:0.35,nr:pu_240:0.35,nr:pu_241:0.25",
                "pu_mix=nr:pu_239:0.5,nr:pu_240:0.5",
                "pu238=nr:pu_238:1",
                "pu239=nr:pu_239:1",
                "pu240=nr:pu_240:1",
                "pu241=nr:pu_241:1",
                "am241=nr:am_241:1",
                "am242=nr:am_242:1",
                "am_mix=nr:am_241:0.5,nr:am_242:0.5",
                "neptunium=nr:np_237:1",
                "polonium=nr:po_210:1",
                "co60=nr:co_60:1",
                "sr90=nr:sr_90:1",
                "cs137=nr:cs_137:1",
                "i131=nr:i_131:1",
                "schrabidium=nuclear_radiation:ntm_schrabidium:1",
                "schrabidate=nuclear_radiation:ntm_schrabidium:1",
                "schraranium=nuclear_radiation:ntm_schrabidium:1",
                "solinium=nuclear_radiation:ntm_solinium:1",
                "balefire=nuclear_radiation:ntm_balefire:1");
    }

    /** Parses {@code material=isotope:weight,isotope:weight}. Isotope ids contain ':', so split on the last one. */
    public static Map<String, Map<String, Double>> parse(Collection<? extends String> entries) {
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();
        for (String entry : entries) {
            int split = entry.indexOf('=');
            if (split <= 0) throw new IllegalArgumentException("Expected material=isotope:weight,...: " + entry);
            String material = entry.substring(0, split).trim().toLowerCase(Locale.ROOT);
            Map<String, Double> weights = new LinkedHashMap<>();
            for (String part : entry.substring(split + 1).split(",")) {
                int colon = part.lastIndexOf(':');
                if (colon <= 0) throw new IllegalArgumentException("Expected isotope:weight in " + entry);
                double weight = Double.parseDouble(part.substring(colon + 1).trim());
                if (!Double.isFinite(weight) || weight <= 0.0) {
                    throw new IllegalArgumentException("Activity weights must be finite and positive: " + entry);
                }
                weights.merge(part.substring(0, colon).trim(), weight, Double::sum);
            }
            if (weights.isEmpty()) throw new IllegalArgumentException("Empty composition: " + entry);
            result.put(material, Map.copyOf(weights));
        }
        return result;
    }

    /**
     * Material key from item tag paths ({@code c:<form>/<material>}) first, then from the item path.
     * Returns null when nothing matches or the item is a processed/non-source product.
     */
    public static String material(Collection<String> itemTagPaths, String itemPath, Set<String> known) {
        String path = itemPath.toLowerCase(Locale.ROOT);
        for (String token : path.split("_")) if (NON_SOURCE_TOKENS.contains(token)) return null;
        for (String tagPath : itemTagPaths) {
            int slash = tagPath.lastIndexOf('/');
            String candidate = slash < 0 ? tagPath : tagPath.substring(slash + 1);
            if (known.contains(candidate)) return candidate;
        }
        // Longest key first so "pu_mix" wins over a shorter overlapping key.
        String best = null;
        String bounded = "_" + path + "_";
        for (String key : known) {
            if (bounded.contains("_" + key + "_") && (best == null || key.length() > best.length())) best = key;
        }
        return best;
    }

    /** Reference dose-equivalent coupling of one Bq of the normalized mixture, or 0 if unusable. */
    public static double referenceSvPerBq(Map<String, Double> weights) {
        double total = 0.0;
        for (double weight : weights.values()) total += weight;
        if (!(total > 0.0) || !Double.isFinite(total)) return 0.0;
        double result = 0.0;
        for (var entry : weights.entrySet()) {
            Isotope isotope = IsotopeRegistry.get(entry.getKey());
            if (isotope == null || isotope.halfLifeTicks() <= 0L) return 0.0;
            result += entry.getValue() / total * emissionSvPerBq(isotope);
        }
        return result;
    }

    private static double emissionSvPerBq(Isotope isotope) {
        var quality = isotope.quality();
        return Units.DEFAULT_GY_PER_BQ_SECOND * (isotope.xRayStrength() * quality.qXRay
                + isotope.betaStrength() * quality.qBeta + isotope.alphaStrength() * quality.qAlpha
                + isotope.neutronStrength() * quality.qNeutron);
    }

    /**
     * Per-item profile. {@code svPerUnit} is the reference Sv/s per native RAD/s at
     * {@link Units#DEFAULT_GY_PER_BQ_SECOND}; NR's configured coupling, geometry, slot factors and
     * shielding then apply exactly as for any other NR item. Returns empty when the level or mixture is unusable.
     */
    public static RadiationProfile profile(double nativeLevel, Map<String, Double> weights, double svPerUnit) {
        if (!(nativeLevel > 1.0e-5) || !Double.isFinite(nativeLevel)) return RadiationProfile.empty();
        double reference = referenceSvPerBq(weights);
        if (!(reference > 0.0)) return RadiationProfile.empty();
        double activity = nativeLevel * svPerUnit / reference;
        if (!Double.isFinite(activity)) return RadiationProfile.empty();
        double total = 0.0;
        for (double weight : weights.values()) total += weight;
        RadiationProfile profile = RadiationProfile.empty();
        for (var entry : weights.entrySet()) {
            Isotope isotope = IsotopeRegistry.get(entry.getKey());
            double atoms = activity * (entry.getValue() / total) / Units.atomsToBq(1.0, isotope.halfLifeTicks());
            if (!Double.isFinite(atoms)) return RadiationProfile.empty();
            profile.put(new IsotopeStack(isotope, atoms, 0L));
        }
        return profile;
    }

    /**
     * Native reacher handling of one carried stack: {@code r = level*count/20} per tick, reduced to
     * {@code r/49} with 528 mode or {@code sqrt(r + 1/(r+2)^2) - 1/(r+2)} otherwise. Returns the multiplier.
     */
    public static double reacherScale(double stackLevel, boolean mode528) {
        double r = stackLevel / 20.0;
        if (!(r > 0.0) || !Double.isFinite(r)) return 1.0;
        double reduced = mode528 ? r / 49.0 : Math.sqrt(r + 1.0 / ((r + 2.0) * (r + 2.0))) - 1.0 / (r + 2.0);
        return Math.clamp(reduced / r, 0.0, 1.0);
    }
}
