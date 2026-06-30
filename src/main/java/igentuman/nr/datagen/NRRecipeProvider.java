package igentuman.nr.datagen;

import igentuman.nr.medicine.NRMedicineItems;
import igentuman.nr.tools.NRTools;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class NRRecipeProvider extends RecipeProvider {

    public NRRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, NRTools.GEIGER_COUNTER)
                .pattern("III")
                .pattern("RGR")
                .pattern("ICI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('G', Items.GLASS_PANE)
                .define('C', Items.COPPER_INGOT)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, NRTools.DOSIMETER)
                .pattern(" I ")
                .pattern("GCG")
                .pattern(" R ")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS_PANE)
                .define('C', Items.COMPARATOR)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_comparator", has(Items.COMPARATOR))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NRMedicineItems.IODINE_PILL, 2)
                .requires(Items.DRIED_KELP)
                .requires(Items.GLOWSTONE_DUST)
                .requires(Items.SUGAR)
                .unlockedBy("has_dried_kelp", has(Items.DRIED_KELP))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NRMedicineItems.PRUSSIAN_BLUE)
                .requires(Items.LAPIS_LAZULI)
                .requires(Items.IRON_NUGGET, 2)
                .unlockedBy("has_lapis", has(Items.LAPIS_LAZULI))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NRMedicineItems.RAD_PROTECTION_POTION)
                .requires(Items.GLASS_BOTTLE)
                .requires(NRMedicineItems.IODINE_PILL)
                .requires(NRMedicineItems.PRUSSIAN_BLUE)
                .unlockedBy("has_iodine_pill", has(NRMedicineItems.IODINE_PILL))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NRMedicineItems.RAD_PROTECTION_POTION_2)
                .requires(NRMedicineItems.RAD_PROTECTION_POTION)
                .requires(Items.GLOWSTONE_DUST)
                .unlockedBy("has_rad_protection_potion", has(NRMedicineItems.RAD_PROTECTION_POTION))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NRMedicineItems.ANTI_RAD_INJECTION)
                .requires(Items.GLASS_BOTTLE)
                .requires(Items.IRON_NUGGET)
                .requires(Items.GHAST_TEAR)
                .requires(NRMedicineItems.PRUSSIAN_BLUE)
                .unlockedBy("has_ghast_tear", has(Items.GHAST_TEAR))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, NRMedicineItems.RADAWAY)
                .requires(Items.GLASS_BOTTLE)
                .requires(Items.GHAST_TEAR)
                .requires(Items.REDSTONE)
                .requires(Items.GLOWSTONE_DUST)
                .unlockedBy("has_ghast_tear", has(Items.GHAST_TEAR))
                .save(recipeOutput);
    }
}
