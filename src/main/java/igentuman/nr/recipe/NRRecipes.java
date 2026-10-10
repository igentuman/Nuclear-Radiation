package igentuman.nr.recipe;

import igentuman.nr.NuclearRadiation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
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
                    Identifier.fromNamespaceAndPath(NuclearRadiation.MODID, "mutation")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MutationRecipe>> MUTATION_SERIALIZER =
            SERIALIZERS.register("mutation", () -> new RecipeSerializer<>(MutationRecipe.Serializer.CODEC, MutationRecipe.Serializer.STREAM_CODEC));

    public static final DeferredHolder<RecipeType<?>, RecipeType<BlockIrradiationRecipe>> BLOCK_IRRADIATION_TYPE =
            TYPES.register("block_irradiation", () -> RecipeType.simple(
                    Identifier.fromNamespaceAndPath(NuclearRadiation.MODID, "block_irradiation")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BlockIrradiationRecipe>> BLOCK_IRRADIATION_SERIALIZER =
            SERIALIZERS.register("block_irradiation", () -> new RecipeSerializer<>(BlockIrradiationRecipe.Serializer.CODEC, BlockIrradiationRecipe.Serializer.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShieldingUpgradeRecipe>> SHIELDING_UPGRADE_SERIALIZER =
            SERIALIZERS.register("shielding_upgrade", () -> new RecipeSerializer<>(ShieldingUpgradeRecipe.Serializer.CODEC, ShieldingUpgradeRecipe.Serializer.STREAM_CODEC));

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        SERIALIZERS.register(bus);
    }
}
