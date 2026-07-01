package igentuman.nr.tools.client;

import igentuman.nr.network.ClientRadiationCache;
import igentuman.nr.tools.DosimeterItem;
import igentuman.nr.tools.GeigerCounterItem;
import igentuman.nr.util.TextUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RadiationHudLayer implements LayeredDraw.Layer {

    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 6;
    private static final double LETHAL_TOTAL_SV = 50.0; // ~LD50 acute whole-body: bar full

    @Override
    public void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p == null) return;

        boolean geigerHeld = isHeld(p, GeigerCounterItem.class);
        boolean dosiPresent = hasInInventory(p, DosimeterItem.class);
        if (!geigerHeld && !dosiPresent) return;

        Font font = mc.font;
        double svh = ClientRadiationCache.svPerHour();
        int rateColor = colorForRate(svh);

        if (geigerHeld) {
            double cpm = GeigerCounterItem.svhToCpm(svh);
            int x = 4;
            int y = 4;
            graphics.drawString(font, "Rate: " + TextUtils.formatSvPerHour(svh), x, y, rateColor, true);
            y += 10;
            graphics.drawString(font, "CPM: " + TextUtils.formatSi(cpm, "cpm"), x, y, 0x55FF55, true);
        }

        if (dosiPresent) {
            double total = ClientRadiationCache.svTotal();
            String totalStr = "Total: " + TextUtils.formatSv(total);
            String rateStr = "Rate: " + TextUtils.formatSvPerHour(svh);

            int screenW = graphics.guiWidth();
            int screenH = graphics.guiHeight();
            int barX = (screenW - BAR_WIDTH) / 2;
            int barY = screenH - 55;

            int textY = barY - 22;
            graphics.drawString(font, totalStr,
                    (screenW - font.width(totalStr)) / 2, textY, 0xFFFFFF, true);
            graphics.drawString(font, rateStr,
                    (screenW - font.width(rateStr)) / 2, textY + 10, rateColor, true);

            graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, 0xAA000000);
            graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFF222222);

            double t = barFraction(total);
            int fillWidth = (int) Math.round(BAR_WIDTH * t);
            int fillColor = colorForTotalFraction(t) | 0xFF000000;
            if (fillWidth > 0) {
                graphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, fillColor);
            }
        }
    }

    /** Linear fraction of accumulated dose toward acute-lethal reference. */
    private double barFraction(double svTotal) {
        if (svTotal <= 0) return 0.0;
        double t = svTotal / LETHAL_TOTAL_SV;
        if (t < 0) return 0;
        if (t > 1) return 1;
        return t;
    }

    private int colorForTotalFraction(double t) {
        if (t < 0.25) return 0x55FF55;
        if (t < 0.5)  return 0xFFFF55;
        if (t < 0.75) return 0xFFAA00;
        return 0xFF5555;
    }

    private boolean isHeld(Player p, Class<?> cls) {
        return matches(p.getMainHandItem(), cls) || matches(p.getOffhandItem(), cls);
    }

    private boolean hasInInventory(Player p, Class<?> cls) {
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (matches(inv.getItem(i), cls)) return true;
        }
        return false;
    }

    private boolean matches(ItemStack s, Class<?> cls) {
        return !s.isEmpty() && cls.isInstance(s.getItem());
    }

    /**
     * Mekanism RadiationScale bands (Sv/h):
     *   <1e-7  background  green
     *   <1e-5  NONE        gray      (10 µSv/h)
     *   <1e-3  LOW         yellow    (1 mSv/h)
     *   <0.1   MEDIUM      orange    (100 mSv/h)
     *   <10    ELEVATED    red       (10 Sv/h)
     *   <100   HIGH        dark red  (100 Sv/h)
     *   >=100  EXTREME     magenta
     */
    private int colorForRate(double svh) {
        if (svh < 1e-7)  return 0x55FF55;
        if (svh < 1e-5)  return 0xAAAAAA;
        if (svh < 1e-3)  return 0xFFFF55;
        if (svh < 0.1)   return 0xFFAA00;
        if (svh < 10.0)  return 0xFF5555;
        if (svh < 100.0) return 0xAA0000;
        return 0xFF55FF;
    }
}
