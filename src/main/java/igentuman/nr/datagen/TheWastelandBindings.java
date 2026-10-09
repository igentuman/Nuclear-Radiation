package igentuman.nr.datagen;

import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.registry.Isotopes;

import java.util.List;

import static igentuman.nr.datagen.RadiationBindingProvider.rec;

/** IDs verified against The Wasteland Reworked 1.0.5 for NeoForge 1.21.1. */
final class TheWastelandBindings {
    private static final String MOD = "the_wasteland_reworked:";

    private TheWastelandBindings() {}

    static void addTo(List<Isotope.BindingDefinition> entries) {
        uranium(entries, "raw_uranium", 2.51e24, 1.84e22, false);
        uranium(entries, "uranium_ingot", 4.27e25, 3.14e22, false);
        uranium(entries, "uranium_powder", 2.51e24, 1.84e22, false);
        uranium(entries, "uranium_rod", 2.0e26, 1.4e24, false);
        uranium(entries, "uranium_ore", 9.72e21, 7.11e19, true);
        uranium(entries, "raw_uranium_block", 2.26e25, 1.66e23, true);
        uranium(entries, "uranium_block", 3.84e26, 2.83e23, true);

        // The archive flags these as radioactive but supplies no isotope composition.
        // Cs-137/Sr-90 are NR's representative long-lived contamination mixture.
        fallout(entries, "incrusted_wasteland_moss", 8.0e17, 4.0e17, true, false);
        fallout(entries, "waste_barrel", 4.4e18, 2.2e18, false, true);
        fallout(entries, "xp_reactor_core", 4.4e18, 2.2e18, false, true);
    }

    private static void uranium(List<Isotope.BindingDefinition> entries, String name,
                                double u238, double u235, boolean block) {
        String id = MOD + name;
        entries.add(Isotope.BindingDefinition.builder("the_wasteland_" + name + "_item")
                .item(rec(id)).isotope(Isotopes.U_238, u238).isotope(Isotopes.U_235, u235).build());
        if (block) {
            entries.add(Isotope.BindingDefinition.builder("the_wasteland_" + name + "_block")
                    .block(rec(id)).isotope(Isotopes.U_238, u238).isotope(Isotopes.U_235, u235).build());
        }
    }

    private static void fallout(List<Isotope.BindingDefinition> entries, String name,
                                double cs137, double sr90, boolean item, boolean block) {
        String id = MOD + name;
        if (item) {
            entries.add(Isotope.BindingDefinition.builder("the_wasteland_" + name + "_item")
                    .item(rec(id)).isotope(Isotopes.CS_137, cs137).isotope(Isotopes.SR_90, sr90).build());
        }
        if (block) {
            entries.add(Isotope.BindingDefinition.builder("the_wasteland_" + name + "_block")
                    .block(rec(id)).isotope(Isotopes.CS_137, cs137).isotope(Isotopes.SR_90, sr90).build());
        }
    }
}
