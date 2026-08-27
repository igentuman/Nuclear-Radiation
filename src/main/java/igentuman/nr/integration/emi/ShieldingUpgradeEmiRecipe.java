package igentuman.nr.integration.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nr.api.shielding.ShieldingUpgradeComponent;
import igentuman.nr.integration.jei.ShieldingUpgradeEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;

import java.util.List;

public class ShieldingUpgradeEmiRecipe extends BasicEmiRecipe {

    public ShieldingUpgradeEmiRecipe(ShieldingUpgradeEntry entry) {
        super(VanillaEmiRecipeCategories.SMITHING, synthId(entry), 112, 18);
        inputs.add(EmiIngredient.of(List.of()));
        inputs.add(EmiIngredient.of(Ingredient.of(Tags.Items.ARMORS)));
        inputs.add(EmiStack.of(entry.material()));

        ItemStack result = new ItemStack(Items.IRON_CHESTPLATE);
        result.set(ShieldingUpgradeComponent.TYPE, entry.value());
        outputs.add(EmiStack.of(result));
    }

    private static ResourceLocation synthId(ShieldingUpgradeEntry entry) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(entry.material().getItem());
        return ResourceLocation.fromNamespaceAndPath("nuclear_radiation",
                "shielding_upgrade/" + key.getNamespace() + "/" + key.getPath());
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 62, 1);
        widgets.addSlot(inputs.get(0), 0, 0);
        widgets.addSlot(inputs.get(1), 18, 0);
        widgets.addSlot(inputs.get(2), 36, 0);
        widgets.addSlot(outputs.get(0), 94, 0).recipeContext(this);
    }
}
