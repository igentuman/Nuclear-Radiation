package igentuman.nr.datagen;

import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.registry.Isotopes;

import java.util.List;

/**
 * MEGATON: Nuclear Warfare 1.5.2 (nuclearcraft, NeoForge 26.1.2).
 * IDs come from the published jar's ModItems/ModBlocks registries. Its three
 * RadioactiveItem strengths are 0.6, 1.0 and 3.0 rad/s. Atom counts use NR's
 * existing raw uranium and ingot scales; the enriched blend is an approximation.
 */
final class MegatonBindings {
    private MegatonBindings() {}

    static void addTo(List<Isotope.BindingDefinition> list) {
        uraniumItem(list, "raw_uranium", 2.51e24, 1.84e22);
        uraniumItem(list, "uranium_ingot", 4.27e25, 3.14e22);
        uraniumItem(list, "enriched_uranium", 4.27e25, 9.42e22);

        for (String ore : new String[]{"uranium_ore", "deepslate_uranium_ore"}) {
            uraniumBlock(list, ore, 9.72e21, 7.11e19);
            uraniumItem(list, ore, 9.72e21, 7.11e19);
        }
        uraniumBlock(list, "uranium_block", 3.843e26, 2.826e23);
        uraniumItem(list, "uranium_block", 3.843e26, 2.826e23);
    }

    private static void uraniumItem(List<Isotope.BindingDefinition> list, String name, double u238, double u235) {
        list.add(Isotope.BindingDefinition.builder("megaton_" + name + "_item")
                .item(RadiationBindingProvider.rec("nuclearcraft:" + name))
                .isotope(Isotopes.U_238, u238).isotope(Isotopes.U_235, u235).build());
    }

    private static void uraniumBlock(List<Isotope.BindingDefinition> list, String name, double u238, double u235) {
        list.add(Isotope.BindingDefinition.builder("megaton_" + name + "_block")
                .block(RadiationBindingProvider.rec("nuclearcraft:" + name))
                .isotope(Isotopes.U_238, u238).isotope(Isotopes.U_235, u235).build());
    }
}
