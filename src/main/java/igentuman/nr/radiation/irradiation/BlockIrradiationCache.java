package igentuman.nr.radiation.irradiation;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.recipe.BlockIrradiationRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;

import java.util.*;

/**
 * Pre-resolved {@code Block -> recipes} lookup. Tags are expanded to concrete blocks once per
 * datapack reload, so the simulator hot path is a pure map lookup. Recipe lists are sorted
 * ascending by {@link BlockIrradiationRecipe#minBq()} so the local-Bq threshold filter can
 * early-break.
 *
 * <p>Rebuilt lazily on the server tick via {@link #ensureBuilt}. Minecraft 26.1 reloads recipes
 * inside an existing {@link RecipeManager}, so manager identity cannot detect datapack changes.
 */
public final class BlockIrradiationCache {

    private BlockIrradiationCache() {}

    private static volatile Map<Block, List<BlockIrradiationRecipe>> byBlock = Map.of();
    private static List<RecipeHolder<?>> lastRecipes = List.of();

    public static void ensureBuilt(RecipeManager mgr) {
        List<RecipeHolder<?>> recipes = mgr.getRecipes().stream()
                .filter(holder -> holder.value() instanceof BlockIrradiationRecipe)
                .toList();
        if (recipes.equals(lastRecipes)) return;
        rebuild(recipes);
        lastRecipes = recipes;
    }

    public static void rebuild(RecipeManager mgr) {
        List<RecipeHolder<?>> recipes = mgr.getRecipes().stream()
                .filter(holder -> holder.value() instanceof BlockIrradiationRecipe)
                .toList();
        rebuild(recipes);
        lastRecipes = recipes;
    }

    private static void rebuild(List<RecipeHolder<?>> recipes) {
        Map<Block, List<BlockIrradiationRecipe>> map = new HashMap<>();
        for (RecipeHolder<?> holder : recipes) {
            BlockIrradiationRecipe r = (BlockIrradiationRecipe) holder.value();
            for (Block b : r.resolvedInputBlocks()) {
                map.computeIfAbsent(b, k -> new ArrayList<>()).add(r);
            }
        }
        map.values().forEach(list ->
                list.sort(Comparator.comparingDouble(BlockIrradiationRecipe::minBq)));
        byBlock = Map.copyOf(map);
        NuclearRadiation.LOGGER.info("Indexed {} block irradiation recipes for {} blocks", recipes.size(), map.size());
    }

    public static List<BlockIrradiationRecipe> forBlock(Block b) {
        return byBlock.getOrDefault(b, List.of());
    }
}
