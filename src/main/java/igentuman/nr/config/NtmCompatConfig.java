package igentuman.nr.config;

import igentuman.nr.compat.ntm.NtmItemProfiles;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/** Optional compatibility settings. Changing the dose scale changes reverse NTM reads too. */
public final class NtmCompatConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.DoubleValue SV_PER_DOSE_UNIT;
    public static final ModConfigSpec.DoubleValue SV_PER_EXPOSURE_UNIT;
    public static final ModConfigSpec.DoubleValue BQ_PER_EMISSION_UNIT;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FALLBACK_COMPOSITION;
    public static final ModConfigSpec.DoubleValue SV_PER_ITEM_RADIATION_UNIT;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_COMPOSITIONS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ENABLED = builder.comment("Use NR entity dose, sickness and world radiation when NTM Continuum is loaded.",
                        "Timed contamination and neutron activation retain their native lifecycles in this stage.",
                        "Restart after changing this option. Disabling retains NR dose, world profiles and migration markers.",
                        "Re-enabling resumes migrated NR state and ignores intervening native changes for migrated chunks.")
                .define("enabled", true);
        SV_PER_DOSE_UNIT = builder.comment("Sv per accumulated NTM radiation unit. 1000 NTM units = 8 Sv by default.",
                        "Used for migration, medicine, direct dose setters and reverse threshold/instrument reads.",
                        "NR's rate display gain is never applied to accumulated dose.")
                .defineInRange("sv_per_dose_unit", 0.008, 1.0e-12, 1.0);
        SV_PER_EXPOSURE_UNIT = builder.comment("Sv per positive NTM exposure unit (after NTM protection).",
                        "Negative increments (medicine) use sv_per_dose_unit. Kept separate to allow emission balance tuning.",
                        "Default matches the dose scale; values still need gameplay calibration.")
                .defineInRange("sv_per_exposure_unit", 0.008, 1.0e-12, 1.0);
        BQ_PER_EMISSION_UNIT = builder.comment("Bq per native environmental radiation density unit, independent of entity dose/exposure scales.",
                        "Used for native world migration and future environmental release writes.",
                        "Default gamma reference is 0.008 Sv/s; actual physical rate uses NR's configured Gy per Bq/s.",
                        "Provisional gameplay calibration. Changes affect future release writes and reverse scalar reads, not saved atoms.")
                .defineInRange("bq_per_emission_unit", 8.0e12, 1.0e-12, 1.0e24);
        FALLBACK_COMPOSITION = builder.comment("Activity weights for unknown scalar releases: isotope_id=positive_weight.",
                        "Weights are normalized by total activity. Explicit real-material profiles take precedence.",
                        "Unknown/non-decaying isotopes or invalid weights reject conversion before migration commits.",
                        "The synthetic ntm_fallout profile avoids inventing a real isotope from a scalar reading.")
                .defineList("fallback_composition", List.of("nuclear_radiation:ntm_fallout=1.0"),
                        value -> value instanceof String text && text.contains("="));
        SV_PER_ITEM_RADIATION_UNIT = builder.comment("Reference Sv/s per native NTM item radiation unit (RAD/s per carried item), at 1e-15 Gy per Bq/s.",
                        "NTM item levels, transformers and modifiers become NR item profiles with this dose-equivalent magnitude;",
                        "NR's configured Gy per Bq/s, slot factors, geometry and shielding then apply as for any NR item.",
                        "Independent of world emission and entity dose scales. Provisional gameplay calibration.")
                .defineInRange("sv_per_item_radiation_unit", 0.008, 1.0e-12, 1.0);
        ITEM_COMPOSITIONS = builder.comment("Isotope activity fractions for NTM item materials: material=isotope:weight,isotope:weight.",
                        "The material is matched against item tag paths (c:<form>/<material>), then item-path words.",
                        "Unmatched, waste/depleted/spent or invalid entries use fallback_composition. Explicit NR bindings",
                        "and NR radiation components always take priority over imported NTM item radiation.")
                .defineList("item_compositions", NtmItemProfiles.defaultCompositions(),
                        value -> value instanceof String text && text.contains("="));
        SPEC = builder.build();
    }

    private NtmCompatConfig() {}
}
