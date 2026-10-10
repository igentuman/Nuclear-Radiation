package igentuman.nr.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nr.api.shielding.ShieldingUpgradeComponent;
import igentuman.nr.api.shielding.ShieldingUpgradeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

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
    public ItemStack assemble(SmithingRecipeInput input) {
        ItemStack result = input.base().copy();
        double value = ShieldingUpgradeRegistry.get(input.addition());
        result.set(ShieldingUpgradeComponent.TYPE, value);
        return result;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.createFromOptionals(List.of(Optional.empty(), Optional.of(base), Optional.of(addition)));
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() { return false; }

    @Override
    public String group() { return ""; }

    @Override
    public Optional<Ingredient> templateIngredient() { return Optional.empty(); }

    @Override
    public Ingredient baseIngredient() { return base; }

    @Override
    public Optional<Ingredient> additionIngredient() { return Optional.of(addition); }

    @Override
    public RecipeSerializer<ShieldingUpgradeRecipe> getSerializer() {
        return NRRecipes.SHIELDING_UPGRADE_SERIALIZER.get();
    }

    public static class Serializer {

        public static final MapCodec<ShieldingUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("base").forGetter(r -> r.base),
                Ingredient.CODEC.fieldOf("addition").forGetter(r -> r.addition)
        ).apply(inst, ShieldingUpgradeRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ShieldingUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.base,
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.addition,
                ShieldingUpgradeRecipe::new);

    }
}
