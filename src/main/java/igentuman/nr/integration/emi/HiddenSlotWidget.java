package igentuman.nr.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.List;

/** Invisible slot: carries {@code recipeContext} for EMI output resolution without rendering. */
public class HiddenSlotWidget extends SlotWidget {

    public HiddenSlotWidget(EmiIngredient stack, int x, int y) {
        super(stack, x, y);
        drawBack(false);
    }

    @Override
    public void drawBackground(GuiGraphics draw, int mouseX, int mouseY, float delta) {}

    @Override
    public void drawStack(GuiGraphics draw, int mouseX, int mouseY, float delta) {}

    @Override
    public void drawOverlay(GuiGraphics draw, int mouseX, int mouseY, float delta) {}

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        return List.of();
    }
}
