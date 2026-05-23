package igentuman.nr.integration.jei;

import igentuman.nr.shielding.ArmorProtectionRegistry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import static igentuman.nr.util.TextUtils.__;

public class ArmorProtectionCategory implements IRecipeCategory<ArmorProtectionEntry> {

    public static final int WIDTH = 162;
    public static final int HEIGHT = 80;

    private final IDrawable icon;

    public ArmorProtectionCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(Items.IRON_CHESTPLATE.getDefaultInstance());
    }

    @Override
    public RecipeType<ArmorProtectionEntry> getRecipeType() {
        return NRJeiTypes.ARMOR_PROTECTION;
    }

    @Override
    public Component getTitle() {
        return __("jei.nuclear_radiation.category.armor_protection");
    }

    @Override
    public int getWidth() { return WIDTH; }

    @Override
    public int getHeight() { return HEIGHT; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ArmorProtectionEntry recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 6).addItemStack(recipe.stack());
    }

    @Override
    public void draw(ArmorProtectionEntry recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics g, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        int x = 30;
        int y = 6;
        int line = font.lineHeight + 1;

        g.drawString(font, recipe.stack().getHoverName(), x, y, 0x202020, false);

        ArmorProtectionRegistry.Protection p = recipe.protection();
        int yy = 30;
        g.drawString(font, __("jei.nuclear_radiation.armor_protection.xray", JeiFormat.percent((float) p.xray())), 6, yy, 0x1A3D7A, false);
        yy += line;
        g.drawString(font, __("jei.nuclear_radiation.armor_protection.alpha", JeiFormat.percent((float) p.alpha())), 6, yy, 0x0E5A1A, false);
        yy += line;
        g.drawString(font, __("jei.nuclear_radiation.armor_protection.beta", JeiFormat.percent((float) p.beta())), 6, yy, 0x8B4500, false);
        yy += line;
        g.drawString(font, __("jei.nuclear_radiation.armor_protection.neutron", JeiFormat.percent((float) p.neutron())), 6, yy, 0x8B0000, false);
    }
}
