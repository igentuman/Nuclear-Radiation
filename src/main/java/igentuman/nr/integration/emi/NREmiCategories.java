package igentuman.nr.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import igentuman.nr.NuclearRadiation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import static igentuman.nr.util.TextUtils.__;

public final class NREmiCategories {
    private NREmiCategories() {}

    public static final EmiRecipeCategory ISOTOPE_STATS = cat("isotope_stats",
            EmiStack.of(Items.GLOWSTONE_DUST));
    public static final EmiRecipeCategory RADIOACTIVE_ITEMS = cat("radioactive_items",
            EmiStack.of(Items.GUNPOWDER));
    public static final EmiRecipeCategory ARMOR_PROTECTION = cat("armor_protection",
            EmiStack.of(Items.IRON_CHESTPLATE));
    public static final EmiRecipeCategory BLOCK_SHIELDING = cat("block_shielding",
            EmiStack.of(Items.IRON_BLOCK));
    public static final EmiRecipeCategory MUTATION = cat("mutation",
            EmiStack.of(Items.CREEPER_SPAWN_EGG));
    public static final EmiRecipeCategory BLOCK_IRRADIATION = cat("block_irradiation",
            EmiStack.of(NuclearRadiation.CREATIVE_RAD_SOURCE_ITEM.get()));

    private static EmiRecipeCategory cat(String path, EmiStack icon) {
        return new NREmiCategory(
                ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, path),
                icon,
                __("jei.nuclear_radiation.category." + path));
    }
}
