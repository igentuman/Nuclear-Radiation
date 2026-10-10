package igentuman.nr.integration.jei;

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

public class BlockShieldingCategory implements IRecipeCategory<BlockShieldingEntry> {

    public static final int WIDTH = 162;
    public static final int HEIGHT = 60;

    private final IDrawable icon;

    public BlockShieldingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(Items.IRON_BLOCK.getDefaultInstance());
    }

    @Override
    public IRecipeType<BlockShieldingEntry> getRecipeType() {
        return NRJeiTypes.BLOCK_SHIELDING;
    }

    @Override
    public @NonNull Component getTitle() {
        return __("jei.nuclear_radiation.category.block_shielding");
    }

    @Override
    public int getWidth() { return WIDTH; }

    @Override
    public int getHeight() { return HEIGHT; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlockShieldingEntry recipe, @NonNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 6).add(recipe.stack());
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).add(recipe.stack());
    }

    @Override
    public void draw(BlockShieldingEntry recipe, @NonNull IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor g, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        int x = 30;
        int y = 6;
        int line = font.lineHeight + 1;

        g.text(font, recipe.stack().getHoverName(), x, y, 0xFF202020, false);

        int yy = 28;
        g.text(font, __("jei.nuclear_radiation.block_shielding.xray", JeiFormat.fmt(recipe.coeffs().xray())), 6, yy, 0xFF1A3D7A, false);
        yy += line;
        g.text(font, __("jei.nuclear_radiation.block_shielding.neutron", JeiFormat.fmt(recipe.coeffs().neutron())), 6, yy, 0xFF8B0000, false);
    }
}
