package igentuman.nr.integration.jei;

import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.recipe.BlockIrradiationRecipe;
import igentuman.nr.recipe.MutationRecipe;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

import static igentuman.nr.NuclearRadiation.MODID;
import static igentuman.nr.NuclearRadiation.rl;

public final class NRJeiTypes {
    private NRJeiTypes() {}

    public static final ResourceLocation PLUGIN_UID = rl("jei_plugin");

    public static final RecipeType<Isotope> ISOTOPE_STATS = RecipeType.create(MODID, "isotope_stats", Isotope.class);

    public static final RecipeType<RadioactiveItemEntry> RADIOACTIVE_ITEMS = RecipeType.create(MODID, "radioactive_items", RadioactiveItemEntry.class);

    public static final RecipeType<Isotope> DECAY_GRAPH = RecipeType.create(MODID, "decay_graph", Isotope.class);

    public static final RecipeType<ArmorProtectionEntry> ARMOR_PROTECTION = RecipeType.create(MODID, "armor_protection", ArmorProtectionEntry.class);

    public static final RecipeType<BlockShieldingEntry> BLOCK_SHIELDING = RecipeType.create(MODID, "block_shielding", BlockShieldingEntry.class);

    public static final RecipeType<MutationRecipe> MUTATION = RecipeType.create(MODID, "mutation", MutationRecipe.class);

    public static final RecipeType<BlockIrradiationRecipe> BLOCK_IRRADIATION = RecipeType.create(MODID, "block_irradiation", BlockIrradiationRecipe.class);
}
