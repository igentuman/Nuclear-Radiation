package igentuman.nr.compat.ntm;

import igentuman.nr.api.IChunkRadiation;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.api.isotope.IsotopeStack;
import igentuman.nr.config.NtmCompatConfig;
import igentuman.nr.util.Units;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Profile calibration for environmental releases, independent of entity dose/exposure units. */
public final class NtmEmissionProfiles {
    public static final String FALLOUT = "nuclear_radiation:ntm_fallout";
    public static final String SCHRABIDIUM = "nuclear_radiation:ntm_schrabidium";
    public static final String SOLINIUM = "nuclear_radiation:ntm_solinium";
    public static final String BALEFIRE = "nuclear_radiation:ntm_balefire";

    private NtmEmissionProfiles() {}

    /** Fictional gameplay definitions, not claims about physical elements. Data packs may override them. */
    public static List<IChunkRadiation.IsotopeDefinition> defaults() {
        return List.of(
                IChunkRadiation.IsotopeDefinition.create(FALLOUT)
                        .xray(1.0f).halfLife(12000L).build(),
                IChunkRadiation.IsotopeDefinition.create(SCHRABIDIUM)
                        .alpha(0.25f).beta(0.25f).xray(0.4f).neutron(0.1f)
                        .halfLife(631128000000L).build(),
                IChunkRadiation.IsotopeDefinition.create(SOLINIUM)
                        .alpha(0.1f).beta(0.2f).xray(0.4f).neutron(0.3f)
                        .halfLife(631128000L).build(),
                IChunkRadiation.IsotopeDefinition.create(BALEFIRE)
                        .beta(0.2f).xray(0.5f).neutron(0.3f).halfLife(120000L).build());
    }

    public static Calibration configured() {
        Map<String, Double> weights = new LinkedHashMap<>();
        for (String entry : NtmCompatConfig.FALLBACK_COMPOSITION.get()) {
            int split = entry.lastIndexOf('=');
            if (split <= 0) throw new IllegalArgumentException("Expected isotope_id=activity_weight: " + entry);
            weights.merge(entry.substring(0, split).trim(), Double.parseDouble(entry.substring(split + 1).trim()), Double::sum);
        }
        return new Calibration(NtmCompatConfig.BQ_PER_EMISSION_UNIT.get(), weights);
    }

    public record Calibration(double bqPerUnit, Map<String, Double> composition) {
        public Calibration {
            if (!Double.isFinite(bqPerUnit) || bqPerUnit <= 0.0) {
                throw new IllegalArgumentException("Emission activity must be finite and positive");
            }
            composition = Map.copyOf(composition);
            double total = 0.0;
            for (var entry : composition.entrySet()) {
                if (!Double.isFinite(entry.getValue()) || entry.getValue() <= 0.0) {
                    throw new IllegalArgumentException("Activity weights must be finite and positive");
                }
                total += entry.getValue();
            }
            if (composition.isEmpty() || !Double.isFinite(total)) {
                throw new IllegalArgumentException("Fallback composition must contain finite activity weights");
            }
        }

        public RadiationProfile profile(double nativeUnits, long tick) {
            RadiationProfile profile = RadiationProfile.empty();
            // Epsilon belongs to native set/increment semantics, not to profile arithmetic:
            // a cap may accept only a sub-epsilon addition to an already nonzero pocket.
            double units = Double.isNaN(nativeUnits) ? 0.0
                    : Math.max(0.0, Math.min(nativeUnits, NtmWorldField.MAX_RADIATION));
            double activity = units * bqPerUnit;
            if (!Double.isFinite(activity)) throw new IllegalArgumentException("Emission activity overflow");
            double total = composition.values().stream().mapToDouble(Double::doubleValue).sum();
            for (var entry : composition.entrySet()) {
                Isotope isotope = requireIsotope(entry.getKey());
                double atoms = activity * (entry.getValue() / total)
                        / Units.atomsToBq(1.0, isotope.halfLifeTicks());
                if (!Double.isFinite(atoms)) throw new IllegalArgumentException("Emission atom count overflow");
                profile.put(new IsotopeStack(isotope, atoms, tick));
            }
            return profile;
        }

        public double nativeUnits(RadiationProfile profile) {
            return NtmWorldField.sanitize(profile.totalActivityBq() / bqPerUnit);
        }

        /** Total-field reads use an unprotected physical sample, never entity dose or its smoothed HUD rate. */
        public double nativeWorldReading(double physicalSvPerSecond) {
            if (!Double.isFinite(physicalSvPerSecond) || physicalSvPerSecond <= 0.0) return 0.0;
            double reference = unshieldedSvPerSecond(profile(1.0, 0));
            if (!Double.isFinite(reference) || reference <= 0.0) {
                throw new IllegalStateException("Fallback composition has no radiation emission");
            }
            return NtmWorldField.sanitize(physicalSvPerSecond / reference);
        }
    }

    static Isotope requireIsotope(String id) {
        Isotope isotope = IsotopeRegistry.get(id);
        if (isotope == null || isotope.halfLifeTicks() <= 0L) {
            throw new IllegalArgumentException("Missing or non-decaying emission isotope: " + id);
        }
        return isotope;
    }

    /** Unit coupling only. Spatial transport and shielding belong to the NR sampler. */
    public static double unshieldedSvPerSecond(RadiationProfile profile) {
        double result = 0.0;
        for (IsotopeStack stack : profile.stacks()) {
            Isotope isotope = stack.isotope();
            var quality = isotope.quality();
            result += stack.currentActivityBq() * Units.DEFAULT_GY_PER_BQ_SECOND
                    * (isotope.xRayStrength() * quality.qXRay + isotope.betaStrength() * quality.qBeta
                    + isotope.alphaStrength() * quality.qAlpha + isotope.neutronStrength() * quality.qNeutron);
        }
        return result;
    }
}
