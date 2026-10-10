package igentuman.nr.integration.jei;

import igentuman.nr.api.shielding.ShieldingUpgradeComponent;
import igentuman.nr.api.shielding.ShieldingUpgradeRegistry;
import igentuman.nr.recipe.ShieldingUpgradeRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.world.item.ItemStack;

public class ShieldingUpgradeJeiExtension implements ISmithingCategoryExtension<ShieldingUpgradeRecipe> {

    @Override
    public <T extends IIngredientAcceptor<T>> void setTemplate(ShieldingUpgradeRecipe recipe, T acceptor) {
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setBase(ShieldingUpgradeRecipe recipe, T acceptor) {
        acceptor.add(recipe.base());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setAddition(ShieldingUpgradeRecipe recipe, T acceptor) {
        acceptor.add(recipe.addition());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setOutput(ShieldingUpgradeRecipe recipe, T acceptor) {
        double shielding = recipe.addition().items()
                .map(holder -> ShieldingUpgradeRegistry.get(new ItemStack(holder)))
                .findFirst().orElse(0.0);
        for (var baseHolder : recipe.base().items().toList()) {
            ItemStack base = new ItemStack(baseHolder);
            ItemStack result = base.copy();
            result.set(ShieldingUpgradeComponent.TYPE, shielding);
            acceptor.add(result);
        }
    }

    @Override
    public void onDisplayedIngredientsUpdate(ShieldingUpgradeRecipe recipe,
                                             IRecipeSlotDrawable template,
                                             IRecipeSlotDrawable base,
                                             IRecipeSlotDrawable addition,
                                             IRecipeSlotDrawable output,
                                             IFocusGroup focuses) {
        base.getDisplayedIngredient(VanillaTypes.ITEM_STACK).ifPresent(baseStack -> {
            ItemStack result = baseStack.copy();
            addition.getDisplayedIngredient(VanillaTypes.ITEM_STACK).ifPresent(addStack ->
                    result.set(ShieldingUpgradeComponent.TYPE, ShieldingUpgradeRegistry.get(addStack)));
            output.clearDisplayOverrides();
            output.createDisplayOverrides().add(result);
        });
    }
}
