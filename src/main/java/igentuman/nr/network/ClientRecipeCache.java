package igentuman.nr.network;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.recipe.BlockIrradiationRecipe;
import igentuman.nr.recipe.MutationRecipe;

import java.util.List;
import java.util.function.BiConsumer;

/** Client recipe data shared with an optional recipe viewer. */
public final class ClientRecipeCache {
    private static List<MutationRecipe> mutations = List.of();
    private static List<BlockIrradiationRecipe> blockIrradiations = List.of();
    private static BiConsumer<List<MutationRecipe>, List<BlockIrradiationRecipe>> listener;
    private static int generation;

    private ClientRecipeCache() {}

    public static List<MutationRecipe> mutations() { return mutations; }
    public static List<BlockIrradiationRecipe> blockIrradiations() { return blockIrradiations; }
    public static int generation() { return generation; }

    public static void update(RecipeSyncPayload payload) {
        mutations = List.copyOf(payload.mutations());
        blockIrradiations = List.copyOf(payload.blockIrradiations());
        generation++;
        NuclearRadiation.LOGGER.debug("Synced {} mutation and {} block irradiation recipes for recipe viewers.",
                mutations.size(), blockIrradiations.size());
        if (listener != null) listener.accept(mutations, blockIrradiations);
    }

    public static void setListener(BiConsumer<List<MutationRecipe>, List<BlockIrradiationRecipe>> newListener) {
        listener = newListener;
    }
}
