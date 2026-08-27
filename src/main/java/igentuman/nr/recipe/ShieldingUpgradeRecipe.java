package igentuman.nr.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nr.api.shielding.ShieldingUpgradeComponent;
import igentuman.nr.api.shielding.ShieldingUpgradeRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class ShieldingUpgradeRecipe implements SmithingRecipe {

    private final Ingredient base;
    private final Ingredient addition;

    public ShieldingUpgradeRecipe(Ingredient base, Ingredient addition) {
        this.base = base;
        this.addition = addition;
    }

    public Ingredient base() { return base; }
    public Ingredient addition() { return addition; }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return input.template().isEmpty()
                && this.base.test(input.base())
                && this.addition.test(input.addition());
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        ItemStack result = input.base().copy();
        double value = ShieldingUpgradeRegistry.get(input.addition());
        result.set(ShieldingUpgradeComponent.TYPE, value);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean isIncomplete() {
        return this.base.hasNoItems() || this.addition.hasNoItems();
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return this.base.test(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return this.addition.test(stack);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NRRecipes.SHIELDING_UPGRADE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    public static class Serializer implements RecipeSerializer<ShieldingUpgradeRecipe> {

        public static final MapCodec<ShieldingUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("base").forGetter(r -> r.base),
                Ingredient.CODEC.fieldOf("addition").forGetter(r -> r.addition)
        ).apply(inst, ShieldingUpgradeRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ShieldingUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.base,
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.addition,
                ShieldingUpgradeRecipe::new);

        @Override
        public MapCodec<ShieldingUpgradeRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShieldingUpgradeRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
