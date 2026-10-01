package igentuman.nr.datagen;

import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.registry.Isotopes;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Radiation bindings for the built-in nuclear materials in Modern Industrialization 1.21.1. */
final class ModernIndustrializationBindings {
    private static final String MOD_ID = "modern_industrialization";

    // Existing uranium bindings use this order of magnitude for one ingot. Keep
    // one MI ingot/dust equal to one material unit, then scale nuggets, rods,
    // blocks and fuel rods according to MI's recipes.
    private static final double ATOMS_PER_INGOT = 4.27e23;
    private static final double NATURAL_U238 = 4.27e25;
    private static final double NATURAL_U235 = 3.14e22;

    private ModernIndustrializationBindings() {}

    static void addTo(List<Isotope.BindingDefinition> bindings) {
        // The unprocessed forms retain the project's existing uranium values.
        add(bindings, "uranium_ore", "item", new Mix(9.72e21, 7.11e19, 0, 0));
        add(bindings, "deepslate_uranium_ore", "item", new Mix(9.72e21, 7.11e19, 0, 0));
        add(bindings, "uranium_ore", "block", new Mix(9.72e21, 7.11e19, 0, 0));
        add(bindings, "deepslate_uranium_ore", "block", new Mix(9.72e21, 7.11e19, 0, 0));
        add(bindings, "raw_uranium", "item", new Mix(2.51e24, 1.84e22, 0, 0));
        add(bindings, "raw_uranium_block", "item", new Mix(18.51e24, 9.84e22, 0, 0));
        add(bindings, "raw_uranium_block", "block", new Mix(18.51e24, 9.84e22, 0, 0));

        material(bindings, "uranium", new Mix(NATURAL_U238, NATURAL_U235, 0, 0), true);
        material(bindings, "uranium_238", new Mix(ATOMS_PER_INGOT, 0, 0, 0), false);
        material(bindings, "uranium_235", new Mix(0, ATOMS_PER_INGOT, 0, 0), false);
        material(bindings, "plutonium", new Mix(0, 0, ATOMS_PER_INGOT, 0), false);

        // MI's alloy mixer recipes use 8:1 for LE and 6:3 for HE fuels.
        material(bindings, "le_uranium", new Mix(ATOMS_PER_INGOT * 8 / 9, ATOMS_PER_INGOT / 9, 0, 0), true);
        material(bindings, "he_uranium", new Mix(ATOMS_PER_INGOT * 6 / 9, ATOMS_PER_INGOT * 3 / 9, 0, 0), true);
        material(bindings, "le_mox", new Mix(ATOMS_PER_INGOT * 8 / 9, 0, ATOMS_PER_INGOT / 9, 0), true);
        material(bindings, "he_mox", new Mix(ATOMS_PER_INGOT * 6 / 9, 0, ATOMS_PER_INGOT * 3 / 9, 0), true);

        // Each depleted rod's recoverable U/Pu mix follows MI's centrifuge
        // outputs (tiny dust counts). Cs-137 represents unrecovered fission
        // products; this deliberately keeps a spent rod hazardous.
        depleted(bindings, "uranium", 53, 1, 18);
        depleted(bindings, "le_uranium", 48, 6, 18);
        depleted(bindings, "he_uranium", 36, 18, 19);
        depleted(bindings, "le_mox", 48, 0, 18);
        depleted(bindings, "he_mox", 36, 0, 19);

        // The battery recipe contains two plutonium dusts. Its casing reduces
        // the exposed inventory source, while retaining a nonzero emission.
        add(bindings, "plutonium_battery", "item", new Mix(0, 0, ATOMS_PER_INGOT / 10, 0));

        // Tritium is registered as a fluid, a fluid block and a bucket item.
        addTritium(bindings, "tritium", "fluid");
        addTritium(bindings, "tritium", "block");
        addTritium(bindings, "tritium_bucket", "item");
    }

    private static void material(List<Isotope.BindingDefinition> bindings, String name, Mix ingot, boolean hasFuelRod) {
        add(bindings, name + "_ingot", "item", ingot);
        add(bindings, name + "_dust", "item", ingot);
        add(bindings, name + "_nugget", "item", ingot.scale(1.0 / 9));
        add(bindings, name + "_tiny_dust", "item", ingot.scale(1.0 / 9));
        add(bindings, name + "_block", "item", ingot.scale(9));
        add(bindings, name + "_block", "block", ingot.scale(9));

        if (hasFuelRod) {
            add(bindings, name + "_rod", "item", ingot.scale(0.5));
            // Cutting yields two rods per ingot; the assembler uses 18 rods.
            add(bindings, name + "_fuel_rod", "item", ingot.scale(9));
            add(bindings, name + "_fuel_rod_double", "item", ingot.scale(18));
            add(bindings, name + "_fuel_rod_quad", "item", ingot.scale(36));
        }
    }

    private static void depleted(List<Isotope.BindingDefinition> bindings, String name,
                                 int u238TinyDusts, int u235TinyDusts, int plutoniumTinyDusts) {
        double atomsPerTinyDust = ATOMS_PER_INGOT / 9;
        add(bindings, name + "_fuel_rod_depleted", "item",
                new Mix(u238TinyDusts * atomsPerTinyDust,
                        u235TinyDusts * atomsPerTinyDust,
                        plutoniumTinyDusts * atomsPerTinyDust, 1.0e20));
    }

    private static void addTritium(List<Isotope.BindingDefinition> bindings, String name, String type) {
        ResourceLocation target = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
        Isotope.BindingDefinition.Builder builder = Isotope.BindingDefinition.builder("mi/" + type + "/" + name);
        switch (type) {
            case "item" -> builder.item(target);
            case "block" -> builder.block(target);
            case "fluid" -> builder.fluid(target);
            default -> throw new IllegalArgumentException(type);
        }
        bindings.add(builder.isotope(Isotopes.H_3, 1.0e20).build());
    }

    private static void add(List<Isotope.BindingDefinition> bindings, String name, String type, Mix mix) {
        ResourceLocation target = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
        Isotope.BindingDefinition.Builder builder = Isotope.BindingDefinition.builder("mi/" + type + "/" + name);
        switch (type) {
            case "item" -> builder.item(target);
            case "block" -> builder.block(target);
            default -> throw new IllegalArgumentException(type);
        }
        if (mix.u238 > 0) builder.isotope(Isotopes.U_238, mix.u238);
        if (mix.u235 > 0) builder.isotope(Isotopes.U_235, mix.u235);
        if (mix.pu239 > 0) builder.isotope(Isotopes.PU_239, mix.pu239);
        if (mix.cs137 > 0) builder.isotope(Isotopes.CS_137, mix.cs137);
        bindings.add(builder.build());
    }

    private record Mix(double u238, double u235, double pu239, double cs137) {
        Mix scale(double factor) {
            return new Mix(u238 * factor, u235 * factor, pu239 * factor, cs137 * factor);
        }
    }
}
