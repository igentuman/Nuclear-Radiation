package igentuman.nr.client;

import igentuman.nr.network.ChunkContaminationDebugPayload;
import igentuman.nr.network.ClientChunkContaminationCache;
import igentuman.nr.util.TextUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

public class ContaminationHudLayer implements LayeredDraw.Layer {

    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 8;
    private static final int LABEL_WIDTH = 48;
    private static final int ROW_GAP = 4;
    private static final int VALUE_GAP = 6;

    private static final int COLOR_AIR = 0xFF66FFFF;
    private static final int COLOR_WATER = 0xFF3366FF;
    private static final int COLOR_SOIL = 0xFF8C5A26;
    private static final int COLOR_BG = 0x80101010;
    private static final int COLOR_FRAME = 0xFFAAAAAA;

    private static final double FULL_SCALE_BQ = 1.0e9;

    @Override
    public void render(GuiGraphics g, DeltaTracker delta) {
        if (!RadiationDebugRenderer.isEnabled()) return;
        ChunkContaminationDebugPayload p = ClientChunkContaminationCache.get();
        if (p == null) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ChunkPos cp = player.chunkPosition();
        int idx = findIndex(p, cp.x, cp.z);
        if (idx < 0) return;

        double air = p.airBq()[idx];
        double water = p.waterBq()[idx];
        double soil = p.soilBq()[idx];
        if (air <= 0 && water <= 0 && soil <= 0) return;

        Font font = mc.font;
        int screenW = g.guiWidth();
        int x = screenW - (LABEL_WIDTH + BAR_WIDTH + VALUE_GAP + 100) - 6;
        int y = 6;

        g.fill(x - 4, y - 4,
                x + LABEL_WIDTH + BAR_WIDTH + VALUE_GAP + 100,
                y + 3 * (BAR_HEIGHT + ROW_GAP) + 6,
                COLOR_BG);

        y = drawRow(g, font, x, y, "Air",   air,   COLOR_AIR);
        y = drawRow(g, font, x, y, "Water", water, COLOR_WATER);
        drawRow(g, font, x, y, "Soil",  soil,  COLOR_SOIL);
    }

    private int drawRow(GuiGraphics g, Font font, int x, int y, String label, double bq, int color) {
        g.drawString(font, label, x, y, 0xFFFFFFFF, true);
        int bx = x + LABEL_WIDTH;
        g.fill(bx, y, bx + BAR_WIDTH, y + BAR_HEIGHT, 0xFF202020);
        int filled = (int) Math.round(BAR_WIDTH * fraction(bq));
        if (filled > 0) {
            g.fill(bx, y, bx + filled, y + BAR_HEIGHT, color);
        }
        g.renderOutline(bx, y, BAR_WIDTH, BAR_HEIGHT, COLOR_FRAME);
        String val = bq > 0 ? TextUtils.formatBq(bq) : "0 Bq";
        g.drawString(font, val, bx + BAR_WIDTH + VALUE_GAP, y, 0xFFFFFFFF, true);
        return y + BAR_HEIGHT + ROW_GAP;
    }

    private double fraction(double bq) {
        if (bq <= 0) return 0.0;
        double f = Math.log10(bq + 1.0) / Math.log10(FULL_SCALE_BQ);
        if (f < 0) return 0;
        if (f > 1) return 1;
        return f;
    }

    private static int findIndex(ChunkContaminationDebugPayload p, int cx, int cz) {
        int n = p.chunkX().length;
        for (int i = 0; i < n; i++) {
            if (p.chunkX()[i] == cx && p.chunkZ()[i] == cz) return i;
        }
        return -1;
    }
}
