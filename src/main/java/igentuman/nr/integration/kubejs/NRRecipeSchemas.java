package igentuman.nr.integration.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.CustomObjectRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import igentuman.nr.NuclearRadiation;
import net.minecraft.resources.ResourceLocation;

/**
 * Registers KubeJS recipe schemas for the mod's non-crafting recipe types, giving scripters typed
 * builders in {@code ServerEvents.recipes}:
 * <pre>
 * event.recipes.nuclear_radiation.mutation(input, result, totalDoseSv, minSvPerHour)
 * event.recipes.nuclear_radiation.block_irradiation(input, minBq, outputs)
 * </pre>
 * The schema keys serialize to the exact JSON shape the mod's registered recipe serializers parse,
 * so the final recipe object is still built by the mod's own {@code MapCodec}. {@code event.custom}
 * with raw JSON remains available as a fallback.
 */
public final class NRRecipeSchemas {

    private NRRecipeSchemas() {}

    public static void register(RecipeSchemaRegistry registry) {
        // {entity: "id", nbt?: "{snbt}"} — matches EntityIngredient/EntityResult codecs.
        RecipeComponent<?> entityObj = RecipeComponent.builder(
                new CustomObjectRecipeComponent.Key("entity", StringComponent.STRING.instance()),
                new CustomObjectRecipeComponent.Key("nbt", StringComponent.OPTIONAL_STRING.instance(), true));

        // {block: "id", weight?: int} — matches BlockIrradiationRecipe.BlockOutput codec.
        RecipeComponent<?> blockOutput = RecipeComponent.builder(
                new CustomObjectRecipeComponent.Key("block", StringComponent.STRING.instance()),
                new CustomObjectRecipeComponent.Key("weight", NumberComponent.INT, true));

        RecipeKey<?> mutationInput = entityObj.otherKey("input");
        RecipeKey<?> mutationResult = entityObj.otherKey("result");
        RecipeKey<Double> totalDose = NumberComponent.DOUBLE.otherKey("total_dose_sv");
        RecipeKey<Double> minRate = NumberComponent.DOUBLE.otherKey("min_sv_per_hour");
        RecipeKey<Double> maxRate = NumberComponent.DOUBLE.otherKey("max_sv_per_hour").optional(Double.MAX_VALUE);
        RecipeKey<Float> mutationChance = NumberComponent.FLOAT.otherKey("chance").optional(1.0f);

        registry.register(nr("mutation"), new RecipeSchema(
                mutationInput, mutationResult, totalDose, minRate, maxRate, mutationChance));

        RecipeKey<String> irradiationInput = StringComponent.STRING.otherKey("input");
        RecipeKey<Double> minBq = NumberComponent.DOUBLE.otherKey("min_bq");
        RecipeKey<?> outputs = blockOutput.asList().otherKey("outputs");
        RecipeKey<Float> irradiationChance = NumberComponent.FLOAT.otherKey("chance").optional(1.0f);

        registry.register(nr("block_irradiation"), new RecipeSchema(
                irradiationInput, minBq, outputs, irradiationChance));
    }

    private static ResourceLocation nr(String path) {
        return ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, path);
    }
}
