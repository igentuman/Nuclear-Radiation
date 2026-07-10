package igentuman.nr.integration.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nr.integration.jei.JeiFormat;
import igentuman.nr.recipe.BlockIrradiationRecipe;
import igentuman.nr.recipe.BlockIrradiationRecipe.BlockOutput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nr.util.TextUtils.__;

public class BlockIrradiationEmiRecipe extends BasicEmiRecipe {

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

    private final BlockIrradiationRecipe recipe;

    public BlockIrradiationEmiRecipe(ResourceLocation id, BlockIrradiationRecipe recipe) {
        super(NREmiCategories.BLOCK_IRRADIATION, id, WIDTH, HEIGHT);
        this.recipe = recipe;

        List<EmiStack> in = new ArrayList<>();
        for (Block b : recipe.resolvedInputBlocks()) {
            ItemStack s = new ItemStack(b);
            if (!s.isEmpty()) in.add(EmiStack.of(s));
        }
        if (!in.isEmpty()) inputs.add(EmiIngredient.of(in));

        int shown = 0;
        for (BlockOutput o : recipe.outputs()) {
            if (shown >= MAX_OUT_SLOTS) break;
            ItemStack s = new ItemStack(o.block());
            if (!s.isEmpty()) {
                outputs.add(EmiStack.of(s));
                shown++;
            }
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        if (!inputs.isEmpty()) {
            widgets.addSlot(inputs.get(0), IN_X, SLOT_Y);
        }
        int x = OUT_X0;
        int shown = 0;
        for (BlockOutput o : recipe.outputs()) {
            if (shown >= MAX_OUT_SLOTS) break;
            ItemStack s = new ItemStack(o.block());
            if (!s.isEmpty()) {
                widgets.addSlot(EmiStack.of(s), x, SLOT_Y).recipeContext(this);
            }
            x += OUT_STEP;
            shown++;
        }
        widgets.addDrawable(0, 0, WIDTH, HEIGHT, (g, mouseX, mouseY, delta) -> draw(g));
    }

    private void draw(GuiGraphics g) {
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
