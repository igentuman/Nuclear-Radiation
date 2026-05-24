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
            double bq = ClientRadiationCache.bqAtPlayer();
            int x = 4;
            int y = 4;
            graphics.drawString(font, "Rate: " + TextUtils.formatSvPerHour(svh), x, y, rateColor, true);
            y += 10;
            graphics.drawString(font, "CPM: " + TextUtils.formatSi(bq, "cpm"), x, y, 0x55FF55, true);
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

            double t = barFraction(svh);
            int fillWidth = (int) Math.round(BAR_WIDTH * t);
            int fillColor = rateColor | 0xFF000000;
            if (fillWidth > 0) {
                graphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, fillColor);
            }
        }
    }

    /** Log-scale fraction: 0 at 1 nSv/h, 1.0 at 10 Sv/h (10 decades). */
    private double barFraction(double svh) {
        if (svh <= 1e-9) return 0.0;
        if (svh >= 10.0) return 1.0;
        double t = (Math.log10(svh) + 9.0) / 10.0;
        if (t < 0) return 0;
        if (t > 1) return 1;
        return t;
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
     * Thresholds (Sv/h):
     *   <0.3 µSv/h  background    green
     *   <10  µSv/h  elevated      yellow-green
     *   <5   mSv/h  controlled    orange
     *   <100 mSv/h  dangerous     red
     *   <1    Sv/h  very dangerous deep red
     *   >=1   Sv/h  lethal/hours  magenta
     */
    private int colorForRate(double svh) {
        if (svh < 3e-7) return 0x55FF55;
        if (svh < 1e-5) return 0xCCFF55;
        if (svh < 5e-3) return 0xFFAA00;
        if (svh < 0.1)  return 0xFF5555;
        if (svh < 1.0)  return 0xCC0000;
        return 0xFF55FF;
    }
}
