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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RadiationHudLayer implements LayeredDraw.Layer {

    @Override
    public void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p == null) return;
        boolean geiger = isHeld(p, GeigerCounterItem.class);
        boolean dosi   = isHeld(p, DosimeterItem.class);
        if (!geiger && !dosi) return;

        Font font = mc.font;
        int x = 4;
        int y = 4;
        if (geiger) {
            graphics.drawString(font, TextUtils.formatBq(ClientRadiationCache.bqAtPlayer()),
                    x, y, 0x55FF55, true);
            y += 10;
        }
        if (dosi) {
            double svh = ClientRadiationCache.svPerHour();
            int color = colorForRate(svh);
            graphics.drawString(font, "Rate: " + TextUtils.formatSvPerHour(svh), x, y, color, true);
            y += 10;
            graphics.drawString(font, "Total: " + TextUtils.formatSv(ClientRadiationCache.svTotal()),
                    x, y, 0xFFFFFF, true);
        }
    }

    private boolean isHeld(Player p, Class<?> itemClass) {
        return matches(p.getMainHandItem(), itemClass) || matches(p.getOffhandItem(), itemClass);
    }

    private boolean matches(ItemStack s, Class<?> cls) {
        return !s.isEmpty() && cls.isInstance(s.getItem());
    }

    private int colorForRate(double svh) {
        if (svh < 0.001) return 0x55FF55;
        if (svh < 0.01)  return 0xFFFF55;
        if (svh < 0.1)   return 0xFFAA00;
        return 0xFF5555;
    }
}
