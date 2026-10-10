package igentuman.nr.integration.jei;

import igentuman.nr.recipe.MutationRecipe;
import igentuman.nr.util.TextUtils;
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
import net.minecraft.world.item.SpawnEggItem;

import static igentuman.nr.util.TextUtils.__;

public class MutationCategory implements IRecipeCategory<MutationRecipe> {

    public static final int WIDTH = 162;
    public static final int HEIGHT = 100;

    private final IDrawable icon;

    public MutationCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(Items.CREEPER_SPAWN_EGG.getDefaultInstance());
    }

    @Override
    public IRecipeType<MutationRecipe> getRecipeType() {
        return NRJeiTypes.MUTATION;
    }

    @Override
    public Component getTitle() {
        return __("jei.nuclear_radiation.category.mutation");
    }

    @Override
    public int getWidth() { return WIDTH; }

    @Override
    public int getHeight() { return HEIGHT; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MutationRecipe recipe, IFocusGroup focuses) {
        SpawnEggItem.byId(recipe.input().type()).ifPresent(egg ->
                builder.addSlot(RecipeIngredientRole.INPUT, 22, 4).add(egg.value()));
        SpawnEggItem.byId(recipe.result().type()).ifPresent(egg ->
                builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 4).add(egg.value()));
    }

    @Override
    public void draw(MutationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor g, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        g.text(font, "→", WIDTH / 2 - 3, 8, 0xFF404040, false);
        g.text(font, recipe.input().type().getDescription(), 4, 25, 0xFF202020, false);
        g.text(font, recipe.result().type().getDescription(), 83, 25, 0xFF202020, false);

        int line = font.lineHeight + 2;
        int y = 43;
        g.text(font, __("jei.nuclear_radiation.mutation.dose", TextUtils.formatSv(recipe.totalDoseSv())), 6, y, 0xFF0E5A1A, false);
        y += line;
        g.text(font, __("jei.nuclear_radiation.mutation.rate"), 6, y, 0xFF1A3D7A, false);
        y += line;
        g.text(font, __("jei.nuclear_radiation.mutation.min_rate", TextUtils.formatSvPerHour(recipe.minSvPerHour())), 6, y, 0xFF1A3D7A, false);
        if (recipe.maxSvPerHour() < Double.MAX_VALUE) {
            y += line;
            g.text(font, __("jei.nuclear_radiation.mutation.max_rate", TextUtils.formatSvPerHour(recipe.maxSvPerHour())), 6, y, 0xFF1A3D7A, false);
        }
        g.text(font, __("jei.nuclear_radiation.mutation.chance", JeiFormat.percent(recipe.chance())), 92, 38, 0xFF8B0000, false);
    }
}
