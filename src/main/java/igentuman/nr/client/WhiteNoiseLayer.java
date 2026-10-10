package igentuman.nr.client;

import igentuman.nr.network.ClientRadiationCache;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.gui.GuiLayer;

/** Scattered white flecks at high ambient radiation, following the old WhiteNoiseOverlay. */
public final class WhiteNoiseLayer implements GuiLayer {
    private static final double START_SVH = 1.0e-4;
    private final RandomSource random = RandomSource.create();

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int level = noiseLevel(ClientRadiationCache.svPerHourAmbient());
        if (level < 5) return;

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        if (width <= 0 || height <= 0) return;

        int count = random.nextInt(level);
        for (int i = 0; i < count; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            graphics.fill(x, y, x + 1, y + 1, 0xE6FFFFFF);
        }
    }

    private static int noiseLevel(double svPerHourAmbient) {
        if (svPerHourAmbient < START_SVH) return 0;
        // The old overlay used radiation / 100000 and started drawing at level 5.
        return Math.min(50, 5 + (int) Math.floor(15.0 * Math.log10(svPerHourAmbient / START_SVH)));
    }
}
