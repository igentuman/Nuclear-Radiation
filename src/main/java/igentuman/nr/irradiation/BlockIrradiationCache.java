package igentuman.nr.irradiation;

import igentuman.nr.recipe.BlockIrradiationRecipe;
import igentuman.nr.recipe.NRRecipes;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pre-resolved {@code Block -> recipes} lookup. Tags are expanded to concrete blocks once per
 * datapack reload, so the simulator hot path is a pure map lookup. Recipe lists are sorted
 * ascending by {@link BlockIrradiationRecipe#minBq()} so the local-Bq threshold filter can
 * early-break.
 *
 * <p>Rebuilt lazily on the server tick via {@link #ensureBuilt}: a {@code /reload} recreates the
 * {@link RecipeManager}, so an identity change signals stale data. This works on dedicated servers
 * (where the client-only {@code RecipesUpdatedEvent} never fires).
 */
public final class BlockIrradiationCache {

    private BlockIrradiationCache() {}

    private static volatile Map<Block, List<BlockIrradiationRecipe>> byBlock = Map.of();
    private static volatile RecipeManager lastBuiltFrom;

    public static void ensureBuilt(RecipeManager mgr) {
        if (mgr == lastBuiltFrom) return;
        rebuild(mgr);
        lastBuiltFrom = mgr;
    }

    public static void rebuild(RecipeManager mgr) {
        Map<Block, List<BlockIrradiationRecipe>> map = new HashMap<>();
        for (RecipeHolder<BlockIrradiationRecipe> holder :
                mgr.getAllRecipesFor(NRRecipes.BLOCK_IRRADIATION_TYPE.get())) {
            BlockIrradiationRecipe r = holder.value();
            for (Block b : r.resolvedInputBlocks()) {
                map.computeIfAbsent(b, k -> new ArrayList<>()).add(r);
            }
        }
        map.values().forEach(list ->
                list.sort(Comparator.comparingDouble(BlockIrradiationRecipe::minBq)));
        byBlock = Map.copyOf(map);
    }

    public static List<BlockIrradiationRecipe> forBlock(Block b) {
        return byBlock.getOrDefault(b, List.of());
    }
}
