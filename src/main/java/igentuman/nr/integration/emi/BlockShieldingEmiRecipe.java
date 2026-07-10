package igentuman.nr.integration.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nr.integration.jei.BlockShieldingEntry;
import igentuman.nr.integration.jei.JeiFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import static igentuman.nr.util.TextUtils.__;

public class BlockShieldingEmiRecipe extends BasicEmiRecipe {

    public static final int WIDTH = 162;
    public static final int HEIGHT = 60;

    private final BlockShieldingEntry entry;

    public BlockShieldingEmiRecipe(BlockShieldingEntry entry) {
        super(NREmiCategories.BLOCK_SHIELDING, synthId(entry), WIDTH, HEIGHT);
        this.entry = entry;
        EmiStack stack = EmiStack.of(entry.stack());
        inputs.add(stack);
        outputs.add(stack);
    }

    private static ResourceLocation synthId(BlockShieldingEntry entry) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(entry.stack().getItem());
        return ResourceLocation.fromNamespaceAndPath("nuclear_radiation",
                "/block_shielding/" + key.getNamespace() + "/" + key.getPath());
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiStack.of(entry.stack()), 6, 6).recipeContext(this);
        widgets.addDrawable(0, 0, WIDTH, HEIGHT, (g, mouseX, mouseY, delta) -> draw(g));
    }

    private void draw(GuiGraphics g) {
        Font font = Minecraft.getInstance().font;
        int x = 30;
        int y = 6;
        int line = font.lineHeight + 1;

        g.drawString(font, entry.stack().getHoverName(), x, y, 0x202020, false);

        int yy = 28;
        g.drawString(font, __("jei.nuclear_radiation.block_shielding.xray", JeiFormat.fmt(entry.coeffs().xray())), 6, yy, 0x1A3D7A, false);
        yy += line;
        g.drawString(font, __("jei.nuclear_radiation.block_shielding.neutron", JeiFormat.fmt(entry.coeffs().neutron())), 6, yy, 0x8B0000, false);
    }
}
