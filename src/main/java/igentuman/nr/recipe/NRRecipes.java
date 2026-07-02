package igentuman.nr.recipe;

import igentuman.nr.NuclearRadiation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NRRecipes {
    private NRRecipes() {}

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, NuclearRadiation.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, NuclearRadiation.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MutationRecipe>> MUTATION_TYPE =
            TYPES.register("mutation", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "mutation")));

    public static final DeferredHolder<RecipeSerializer<?>, MutationRecipe.Serializer> MUTATION_SERIALIZER =
            SERIALIZERS.register("mutation", MutationRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<BlockIrradiationRecipe>> BLOCK_IRRADIATION_TYPE =
            TYPES.register("block_irradiation", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "block_irradiation")));

    public static final DeferredHolder<RecipeSerializer<?>, BlockIrradiationRecipe.Serializer> BLOCK_IRRADIATION_SERIALIZER =
            SERIALIZERS.register("block_irradiation", BlockIrradiationRecipe.Serializer::new);

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        SERIALIZERS.register(bus);
    }
}
