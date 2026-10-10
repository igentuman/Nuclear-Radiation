package igentuman.nr.integration.jei;

import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.recipe.BlockIrradiationRecipe;
import igentuman.nr.recipe.MutationRecipe;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.resources.Identifier;

import static igentuman.nr.NuclearRadiation.MODID;
import static igentuman.nr.NuclearRadiation.rl;

public final class NRJeiTypes {
    private NRJeiTypes() {}

    public static final Identifier PLUGIN_UID = rl("jei_plugin");

    public static final IRecipeType<Isotope> ISOTOPE_STATS = IRecipeType.create(MODID, "isotope_stats", Isotope.class);

    public static final IRecipeType<RadioactiveItemEntry> RADIOACTIVE_ITEMS = IRecipeType.create(MODID, "radioactive_items", RadioactiveItemEntry.class);

    public static final IRecipeType<Isotope> DECAY_GRAPH = IRecipeType.create(MODID, "decay_graph", Isotope.class);

    public static final IRecipeType<ArmorProtectionEntry> ARMOR_PROTECTION = IRecipeType.create(MODID, "armor_protection", ArmorProtectionEntry.class);

    public static final IRecipeType<BlockShieldingEntry> BLOCK_SHIELDING = IRecipeType.create(MODID, "block_shielding", BlockShieldingEntry.class);

    public static final IRecipeType<MutationRecipe> MUTATION = IRecipeType.create(MODID, "mutation", MutationRecipe.class);

    public static final IRecipeType<BlockIrradiationRecipe> BLOCK_IRRADIATION = IRecipeType.create(MODID, "block_irradiation", BlockIrradiationRecipe.class);
}
