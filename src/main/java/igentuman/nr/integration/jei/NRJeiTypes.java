package igentuman.nr.integration.jei;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.Isotope;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

public final class NRJeiTypes {
    private NRJeiTypes() {}

    public static final ResourceLocation PLUGIN_UID =
            ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "jei_plugin");

    public static final RecipeType<Isotope> ISOTOPE_STATS = RecipeType.create(
            NuclearRadiation.MODID, "isotope_stats", Isotope.class);

    public static final RecipeType<RadioactiveItemEntry> RADIOACTIVE_ITEMS = RecipeType.create(
            NuclearRadiation.MODID, "radioactive_items", RadioactiveItemEntry.class);

    public static final RecipeType<Isotope> DECAY_GRAPH = RecipeType.create(
            NuclearRadiation.MODID, "decay_graph", Isotope.class);

    public static final RecipeType<ArmorProtectionEntry> ARMOR_PROTECTION = RecipeType.create(
            NuclearRadiation.MODID, "armor_protection", ArmorProtectionEntry.class);
}
