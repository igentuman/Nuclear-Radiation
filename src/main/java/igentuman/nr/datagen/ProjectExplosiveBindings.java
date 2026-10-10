package igentuman.nr.datagen;

import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.registry.Isotopes;

import java.util.List;

import static igentuman.nr.datagen.RadiationBindingProvider.rec;

/** Registry names verified against Project Explosive 0.7.2-beta for NeoForge 1.21.1. */
final class ProjectExplosiveBindings {
    private ProjectExplosiveBindings() {}

    static void addTo(List<Isotope.BindingDefinition> entries) {
        uranium(entries, "raw_uranium", 2.51e24, 1.84e22);
        uranium(entries, "uranium_ingot", 4.27e25, 3.14e22);
        uranium(entries, "uranium_ore", 9.72e21, 7.11e19);
        uranium(entries, "deepslate_uranium_ore", 9.72e21, 7.11e19);

        // Ash is fallout deposited by the nuclear explosion, not uranium ore.
        entries.add(Isotope.BindingDefinition.builder("projectexplosive_radioactive_ash_item")
                .item(rec("projectexplosive:radioactive_ash"))
                .isotope(Isotopes.CS_137, 4.4e18)
                .isotope(Isotopes.SR_90, 2.2e18)
                .build());
        entries.add(Isotope.BindingDefinition.builder("projectexplosive_radioactive_ash_block")
                .block(rec("projectexplosive:radioactive_ash"))
                .isotope(Isotopes.CS_137, 4.4e18)
                .isotope(Isotopes.SR_90, 2.2e18)
                .build());
    }

    private static void uranium(List<Isotope.BindingDefinition> entries, String name,
                                double u238, double u235) {
        String id = "projectexplosive:" + name;
        entries.add(Isotope.BindingDefinition.builder("projectexplosive_" + name + "_item")
                .item(rec(id)).isotope(Isotopes.U_238, u238).isotope(Isotopes.U_235, u235).build());
        if (name.endsWith("_ore")) {
            entries.add(Isotope.BindingDefinition.builder("projectexplosive_" + name + "_block")
                    .block(rec(id)).isotope(Isotopes.U_238, u238).isotope(Isotopes.U_235, u235).build());
        }
    }
}
