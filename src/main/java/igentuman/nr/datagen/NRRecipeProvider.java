package igentuman.nr.datagen;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.registry.NRArmorItems;
import igentuman.nr.registry.NRMedicineItems;
import igentuman.nr.registry.NRShieldingItems;
import igentuman.nr.recipe.BlockIrradiationRecipe;
import igentuman.nr.recipe.BlockIrradiationRecipe.BlockOutput;
import igentuman.nr.recipe.EntityIngredient;
import igentuman.nr.recipe.EntityResult;
import igentuman.nr.recipe.MutationRecipe;
import igentuman.nr.recipe.ShieldingUpgradeRecipe;
import igentuman.nr.registry.NRTools;
import igentuman.nr.registry.RadiationTags;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
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

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, NRArmorItems.HAZMAT_HELMET)
                .pattern("HXH")
                .pattern("H H")
                .pattern("   ")
                .define('X', Items.LEATHER_HELMET)
                .define('H', Items.PHANTOM_MEMBRANE)
                .unlockedBy("has_phantom_membrane", has(Items.PHANTOM_MEMBRANE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, NRArmorItems.HAZMAT_CHESTPLATE)
                .pattern("H H")
                .pattern("HXH")
                .pattern("HHH")
                .define('X', Items.LEATHER_CHESTPLATE)
                .define('H', Items.PHANTOM_MEMBRANE)
                .unlockedBy("has_phantom_membrane", has(Items.PHANTOM_MEMBRANE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, NRArmorItems.HAZMAT_LEGGINGS)
                .pattern("HXH")
                .pattern("H H")
                .pattern("H H")
                .define('X', Items.LEATHER_LEGGINGS)
                .define('H', Items.PHANTOM_MEMBRANE)
                .unlockedBy("has_phantom_membrane", has(Items.PHANTOM_MEMBRANE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, NRArmorItems.HAZMAT_BOOTS)
                .pattern("   ")
                .pattern("   ")
                .pattern("HXH")
                .define('X', Items.LEATHER_BOOTS)
                .define('H', Items.PHANTOM_MEMBRANE)
                .unlockedBy("has_phantom_membrane", has(Items.PHANTOM_MEMBRANE))
                .save(recipeOutput);

        buildShieldingItems(recipeOutput);
        buildMutations(recipeOutput);
        buildBlockIrradiations(recipeOutput);
        buildShieldingUpgrade(recipeOutput);
    }

    private void buildShieldingItems(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NRShieldingItems.RAD_SHIELDING_LIGHT.get(), 4)
                .pattern("BLB")
                .pattern("ICI")
                .pattern("BLB")
                .define('L', Items.LEATHER)
                .define('I', Items.IRON_NUGGET)
                .define('C', Items.CLAY_BALL)
                .define('B', Items.COPPER_INGOT)
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NRShieldingItems.RAD_SHIELDING_MEDIUM.get(), 2)
                .pattern("ILI")
                .pattern("LGL")
                .pattern("ILI")
                .define('G', Items.GOLD_BLOCK)
                .define('I', Items.IRON_INGOT)
                .define('L', Items.LAPIS_LAZULI)
                .unlockedBy("has_lapis", has(Items.LAPIS_LAZULI))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NRShieldingItems.RAD_SHIELDING_HEAVY.get())
                .pattern("OGO")
                .pattern("GPG")
                .pattern("OGO")
                .define('P', Items.PRISMARINE)
                .define('O', Items.OBSIDIAN)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_obsidian", has(Items.OBSIDIAN))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NRShieldingItems.RAD_SHIELDING_DPS.get())
                .pattern("DDD")
                .pattern("DND")
                .pattern("DDD")
                .define('D', Items.DIAMOND)
                .define('N', Items.NETHERITE_INGOT)
                .unlockedBy("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                .save(recipeOutput);
    }

    private void buildShieldingUpgrade(RecipeOutput recipeOutput) {
        recipeOutput.accept(NuclearRadiation.rl("shielding_upgrade"),
                new ShieldingUpgradeRecipe(
                        Ingredient.of(Tags.Items.ARMORS),
                        Ingredient.of(RadiationTags.SHIELDING_UPGRADE)
                ),
                null);
    }

    private void buildBlockIrradiations(RecipeOutput recipeOutput) {
        blockIrradiation(recipeOutput, "irradiate_grass_block", "minecraft:grass_block", 1.0e10, 0.02f,
                new BlockOutput(Blocks.COARSE_DIRT, 3),
                new BlockOutput(Blocks.GRAVEL, 1));

        blockIrradiation(recipeOutput, "irradiate_leaves", "#minecraft:leaves", 5.0e9, 0.02f,
                new BlockOutput(Blocks.AIR, 1));
    }

    private void blockIrradiation(RecipeOutput out, String name, String input,
                                  double minBq, float chance, BlockOutput... outputs) {
        out.accept(ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, name),
                new BlockIrradiationRecipe(input, minBq, List.of(outputs), chance),
                null);
    }

    private void buildMutations(RecipeOutput recipeOutput) {
        mutation(recipeOutput, "mutate_cow_mooshroom",
                ing(EntityType.COW), res(EntityType.MOOSHROOM),
                0.1, 0.05, 2, 0.3f);

        mutation(recipeOutput, "mutate_villager_zombie",
                ing(EntityType.VILLAGER), res(EntityType.ZOMBIE_VILLAGER),
                0.5, 0.5, 5, 0.5f);

        mutation(recipeOutput, "mutate_sheep_red",
                ing(EntityType.SHEEP, sheepColor(0)), res(EntityType.SHEEP, sheepColor(14)),
                0.1, 0.01, 1.0, 0.5f);

        mutation(recipeOutput, "mutate_zombie_skeleton",
                ing(EntityType.ZOMBIE), res(EntityType.SKELETON),
                0.5, 0.5, 2, 0.7f);

        mutation(recipeOutput, "mutate_skeleton_wither_skeleton",
                ing(EntityType.SKELETON), res(EntityType.WITHER_SKELETON),
                1.0, 1, 10, 0.75f);
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
