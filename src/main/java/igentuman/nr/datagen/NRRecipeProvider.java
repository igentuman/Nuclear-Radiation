package igentuman.nr.datagen;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.medicine.NRMedicineItems;
import igentuman.nr.recipe.EntityIngredient;
import igentuman.nr.recipe.EntityResult;
import igentuman.nr.recipe.MutationRecipe;
import igentuman.nr.tools.NRTools;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;

import java.util.Optional;
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

        buildMutations(recipeOutput);
    }

    private void buildMutations(RecipeOutput recipeOutput) {
        mutation(recipeOutput, "mutate_cow_mooshroom",
                ing(EntityType.COW), res(EntityType.MOOSHROOM),
                3.0, 0.05, 10, 0.3f);

        mutation(recipeOutput, "mutate_villager_zombie",
                ing(EntityType.VILLAGER), res(EntityType.ZOMBIE_VILLAGER),
                6.0, 1, 5, 0.5f);

        mutation(recipeOutput, "mutate_sheep_red",
                ing(EntityType.SHEEP, sheepColor(0)), res(EntityType.SHEEP, sheepColor(14)),
                1.5, 0.02, 2.0, 0.5f);

        mutation(recipeOutput, "mutate_zombie_skeleton",
                ing(EntityType.ZOMBIE), res(EntityType.SKELETON),
                10.0, 5, 20, 0.7f);

        mutation(recipeOutput, "mutate_skeleton_wither_skeleton",
                ing(EntityType.SKELETON), res(EntityType.WITHER_SKELETON),
                10.0, 5, 30, 0.5f);
    }

    private static EntityIngredient ing(EntityType<?> type) {
        return new EntityIngredient(type, Optional.empty());
    }

    private static EntityIngredient ing(EntityType<?> type, CompoundTag nbt) {
        return new EntityIngredient(type, Optional.of(nbt));
    }

    private static EntityResult res(EntityType<?> type) {
        return new EntityResult(type, Optional.empty());
    }

    private static EntityResult res(EntityType<?> type, CompoundTag nbt) {
        return new EntityResult(type, Optional.of(nbt));
    }

    private static CompoundTag sheepColor(int color) {
        CompoundTag tag = new CompoundTag();
        tag.putByte("Color", (byte) color);
        return tag;
    }

    private void mutation(RecipeOutput out, String name,
                          EntityIngredient input, EntityResult result,
                          double totalDoseSv, double minSvPerHour, double maxSvPerHour, float chance) {
        out.accept(ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, name),
                new MutationRecipe(input, result, totalDoseSv, minSvPerHour, maxSvPerHour, chance),
                null);
    }
}
