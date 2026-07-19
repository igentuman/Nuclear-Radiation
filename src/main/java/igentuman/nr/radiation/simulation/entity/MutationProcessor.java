package igentuman.nr.radiation.simulation.entity;

import igentuman.nr.recipe.EntityResult;
import igentuman.nr.recipe.MutationRecipe;
import igentuman.nr.recipe.NRRecipes;
import igentuman.nr.radiation.storage.EntityRadiationData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Applies radiation-driven entity mutations. Each recipe is a one-shot per entity: the first
 * tick an entity satisfies a recipe's dose + rate window it rolls once and is flagged so that
 * recipe never fires again for it (persisted in {@link EntityRadiationData}). On a successful
 * roll the input entity is discarded and the output spawned in its place.
 */
public final class MutationProcessor {

    private MutationProcessor() {}

    public static void tryMutate(ServerLevel level, LivingEntity entity, EntityRadiationData data) {
        if (entity instanceof Player) return;
        if (!entity.isAlive()) return;

        double svTotal = data.svTotalCareer();
        double svPerHour = data.svPerHour();

        for (RecipeHolder<MutationRecipe> holder : level.getRecipeManager().getAllRecipesFor(NRRecipes.MUTATION_TYPE.get())) {
            String id = holder.id().toString();
            if (data.hasAttemptedMutation(id)) continue;

            MutationRecipe recipe = holder.value();
            if (!recipe.matchesEntity(entity)) continue;
            if (!recipe.inRange(svTotal, svPerHour)) continue;

            data.markAttemptedMutation(id);
            if (level.random.nextFloat() < recipe.chance()) {
                // Defer the spawn/discard: EntityDoseProcessor.tick runs inside a live
                // iteration over the level's entities, and addFreshEntity would mutate it.
                EntityResult result = recipe.result();
                level.getServer().execute(() -> {
                    if (!entity.isAlive()) return;
                    LivingEntity mutated = result.spawn(level, entity);
                    if (mutated != null) entity.discard();
                });
                return;
            }
        }
    }
}
