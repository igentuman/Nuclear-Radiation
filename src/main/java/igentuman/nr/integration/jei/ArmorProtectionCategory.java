package igentuman.nr.integration.jei;

import igentuman.nr.api.shielding.ArmorProtectionRegistry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import static igentuman.nr.util.TextUtils.__;

public class ArmorProtectionCategory implements IRecipeCategory<ArmorProtectionEntry> {

    public static final int WIDTH = 162;
    public static final int HEIGHT = 80;

    private final IDrawable icon;

    public ArmorProtectionCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(Items.IRON_CHESTPLATE.getDefaultInstance());
    }

    @Override
    public @NonNull IRecipeType<ArmorProtectionEntry> getRecipeType() {
        return NRJeiTypes.ARMOR_PROTECTION;
    }

    @Override
    public @NonNull Component getTitle() {
        return __("jei.nuclear_radiation.category.armor_protection");
    }

    @Override
    public int getWidth() { return WIDTH; }

    @Override
    public int getHeight() { return HEIGHT; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ArmorProtectionEntry recipe, @NonNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 6).add(recipe.stack());
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).add(recipe.stack());
    }

    @Override
    public void draw(ArmorProtectionEntry recipe, @NonNull IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor g, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        int x = 30;
        int y = 6;
        int line = font.lineHeight + 1;

        g.text(font, recipe.stack().getHoverName(), x, y, 0xFF202020, false);

        ArmorProtectionRegistry.Protection p = recipe.protection();
        int yy = 30;
        g.text(font, __("jei.nuclear_radiation.armor_protection.xray", JeiFormat.percent((float) p.xray())), 6, yy, 0xFF1A3D7A, false);
        yy += line;
        g.text(font, __("jei.nuclear_radiation.armor_protection.alpha", JeiFormat.percent((float) p.alpha())), 6, yy, 0xFF0E5A1A, false);
        yy += line;
        g.text(font, __("jei.nuclear_radiation.armor_protection.beta", JeiFormat.percent((float) p.beta())), 6, yy, 0xFF8B4500, false);
        yy += line;
        g.text(font, __("jei.nuclear_radiation.armor_protection.neutron", JeiFormat.percent((float) p.neutron())), 6, yy, 0xFF8B0000, false);
    }
}
