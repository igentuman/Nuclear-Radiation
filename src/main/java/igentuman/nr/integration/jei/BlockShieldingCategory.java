package igentuman.nr.integration.jei;

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

public class BlockShieldingCategory implements IRecipeCategory<BlockShieldingEntry> {

    public static final int WIDTH = 162;
    public static final int HEIGHT = 60;

    private final IDrawable icon;

    public BlockShieldingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(Items.IRON_BLOCK.getDefaultInstance());
    }

    @Override
    public RecipeType<BlockShieldingEntry> getRecipeType() {
        return NRJeiTypes.BLOCK_SHIELDING;
    }

    @Override
    public Component getTitle() {
        return __("jei.nuclear_radiation.category.block_shielding");
    }

    @Override
    public int getWidth() { return WIDTH; }

    @Override
    public int getHeight() { return HEIGHT; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlockShieldingEntry recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 6).addItemStack(recipe.stack());
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(recipe.stack());
    }

    @Override
    public void draw(BlockShieldingEntry recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics g, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        int x = 30;
        int y = 6;
        int line = font.lineHeight + 1;

        g.drawString(font, recipe.stack().getHoverName(), x, y, 0x202020, false);

        int yy = 28;
        g.drawString(font, __("jei.nuclear_radiation.block_shielding.xray", JeiFormat.fmt(recipe.coeffs().xray())), 6, yy, 0x1A3D7A, false);
        yy += line;
        g.drawString(font, __("jei.nuclear_radiation.block_shielding.neutron", JeiFormat.fmt(recipe.coeffs().neutron())), 6, yy, 0x8B0000, false);
    }
}
