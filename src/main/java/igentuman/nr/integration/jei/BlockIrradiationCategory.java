package igentuman.nr.integration.jei;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.recipe.BlockIrradiationRecipe;
import igentuman.nr.recipe.BlockIrradiationRecipe.BlockOutput;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nr.util.TextUtils.__;

public class BlockIrradiationCategory implements IRecipeCategory<BlockIrradiationRecipe> {

    public static final int WIDTH = 162;
    public static final int HEIGHT = 62;

    private static final int SLOT_Y = 4;
    private static final int IN_X = 8;
    private static final int ARROW_X = 30;
    private static final int OUT_X0 = 46;
    private static final int OUT_STEP = 20;
    private static final int MAX_OUT_SLOTS = 5;
    private static final int WEIGHT_Y = 24;
    private static final int STAT_Y1 = 38;
    private static final int STAT_Y2 = 50;

    private final IDrawable icon;

    public BlockIrradiationCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(NuclearRadiation.CREATIVE_RAD_SOURCE_ITEM.get()));
    }

    @Override
    public RecipeType<BlockIrradiationRecipe> getRecipeType() {
        return NRJeiTypes.BLOCK_IRRADIATION;
    }

    @Override
    public Component getTitle() {
        return __("jei.nuclear_radiation.category.block_irradiation");
    }

    @Override
    public int getWidth() { return WIDTH; }

    @Override
    public int getHeight() { return HEIGHT; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlockIrradiationRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> inputs = new ArrayList<>();
        for (Block b : recipe.resolvedInputBlocks()) {
            ItemStack s = new ItemStack(b);
            if (!s.isEmpty()) inputs.add(s);
        }
        if (!inputs.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, IN_X, SLOT_Y).addItemStacks(inputs);
        }

        int x = OUT_X0;
        int shown = 0;
        for (BlockOutput o : recipe.outputs()) {
            if (shown >= MAX_OUT_SLOTS) break;
            ItemStack s = new ItemStack(o.block());
            if (!s.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, SLOT_Y).addItemStack(s);
            }
            x += OUT_STEP;
            shown++;
        }
    }

    @Override
    public void draw(BlockIrradiationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics g, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        g.drawString(font, "→", ARROW_X, SLOT_Y + 4, 0x404040, false);

        int total = 0;
        for (BlockOutput o : recipe.outputs()) total += Math.max(0, o.weight());

        float scale = 0.75f;
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1f);
        int x = OUT_X0;
        int shown = 0;
        for (BlockOutput o : recipe.outputs()) {
            if (shown >= MAX_OUT_SLOTS) break;
            String pct = JeiFormat.percent(total > 0 ? (float) Math.max(0, o.weight()) / total : 0f);
            g.drawString(font, pct, (int) (x / scale), (int) (WEIGHT_Y / scale), 0x303030, false);
            x += OUT_STEP;
            shown++;
        }
        g.pose().popPose();

        g.drawString(font, __("jei.nuclear_radiation.block_irradiation.min_bq", JeiFormat.activity(recipe.minBq())),
                6, STAT_Y1, 0x0E5A1A, false);
        g.drawString(font, __("jei.nuclear_radiation.block_irradiation.chance", JeiFormat.percent(recipe.chance())),
                6, STAT_Y2, 0x8B0000, false);
    }
}
