package igentuman.nr.client;

import igentuman.nr.items.DosimeterItem;
import igentuman.nr.items.GeigerCounterItem;
import igentuman.nr.network.ClientRadiationCache;
import igentuman.nr.util.TextUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RadiationHudLayer implements GuiLayer {

    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 6;
    private static final double FULL_BAR_SV = 5.0;
    private static final Identifier WASTELAND_GEIGER =
            Identifier.parse("the_wasteland_reworked:geiger_counter");

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p == null) return;

        boolean geigerPresent = isHeld(p, GeigerCounterItem.class)
                || hasInInventory(p, GeigerCounterItem.class)
                || isHeldItem(p, WASTELAND_GEIGER)
                || hasInInventoryItem(p, WASTELAND_GEIGER);
        boolean dosiPresent = hasInInventory(p, DosimeterItem.class);
        if (!geigerPresent && !dosiPresent) return;

        Font font = mc.font;

        if (geigerPresent) {
            double ambientSvh = ClientRadiationCache.svPerHourAmbient();
            double cpm = GeigerCounterItem.svhToCpm(ambientSvh);
            int x = 4;
            int y = 4;
            graphics.text(font, Component.translatable("hud.nuclear_radiation.rate", TextUtils.formatSvPerHour(ambientSvh)),
                    x, y, colorForRate(ambientSvh), true);
            y += 10;
            graphics.text(font, Component.translatable("hud.nuclear_radiation.cpm", TextUtils.formatSi(cpm, "cpm")),
                    x, y, 0xFF55FF55, true);
        }

        if (dosiPresent) {
            double svh = ClientRadiationCache.svPerHour();
            double total = ClientRadiationCache.svTotal();
            int rateColor = colorForRate(svh);
            Component totalStr = Component.translatable("hud.nuclear_radiation.total", TextUtils.formatSv(total));
            Component rateStr = Component.translatable("hud.nuclear_radiation.rate", TextUtils.formatSvPerHour(svh));

            int screenW = graphics.guiWidth();
            int screenH = graphics.guiHeight();
            int barX = (screenW - BAR_WIDTH) / 2;
            int barY = screenH - 62;

            graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, 0xAA000000);
            graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFF222222);

            double t = barFraction(total);
            int fillWidth = (int) Math.round(BAR_WIDTH * t);
            int fillColor = colorForTotalFraction(t) | 0xFF000000;
            if (fillWidth > 0) {
                graphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, fillColor);
            }

            int textY = barY + BAR_HEIGHT + 4;
            graphics.text(font, totalStr,
                    (screenW - font.width(totalStr)) / 2, textY, 0xFFFFFFFF, true);
            graphics.text(font, rateStr,
                    (screenW - font.width(rateStr)) / 2, textY + 10, rateColor, true);
        }
    }

    /** Linear fraction of accumulated dose toward acute-lethal reference. */
    private double barFraction(double svTotal) {
        if (svTotal <= 0) return 0.0;
        double t = svTotal / FULL_BAR_SV;
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

    private boolean isHeldItem(Player p, Identifier id) {
        return id.equals(BuiltInRegistries.ITEM.getKey(p.getMainHandItem().getItem()))
                || id.equals(BuiltInRegistries.ITEM.getKey(p.getOffhandItem().getItem()));
    }

    private boolean hasInInventory(Player p, Class<?> cls) {
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (matches(inv.getItem(i), cls)) return true;
        }
        return false;
    }

    private boolean hasInInventoryItem(Player p, Identifier id) {
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && id.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) return true;
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
        if (svh < 1e-7)  return 0xFF55FF55;
        if (svh < 1e-5)  return 0xFFAAAAAA;
        if (svh < 1e-3)  return 0xFFFFFF55;
        if (svh < 0.1)   return 0xFFFFAA00;
        if (svh < 10.0)  return 0xFFFF5555;
        if (svh < 100.0) return 0xFFAA0000;
        return 0xFFFF55FF;
    }
}
