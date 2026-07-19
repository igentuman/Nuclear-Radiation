package igentuman.nr.integration.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.api.RadiationQuality;
import igentuman.nr.integration.jei.JeiFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

import static igentuman.nr.util.TextUtils.__;

public class IsotopeStatsEmiRecipe extends BasicEmiRecipe {

    public static final int WIDTH = 162;
    public static final int HEIGHT = 90;

    private final Isotope isotope;

    public IsotopeStatsEmiRecipe(Isotope isotope) {
        super(NREmiCategories.ISOTOPE_STATS, synthId(isotope), WIDTH, HEIGHT);
        this.isotope = isotope;
    }

    private static ResourceLocation synthId(Isotope iso) {
        return ResourceLocation.fromNamespaceAndPath("nuclear_radiation",
                "/isotope_stats/" + iso.id().toLowerCase(Locale.ROOT).replace(':', '_'));
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addDrawable(0, 0, WIDTH, HEIGHT, (g, mouseX, mouseY, delta) -> draw(g));
    }

    private void draw(GuiGraphics g) {
        Font font = Minecraft.getInstance().font;
        int x = 4;
        int y = 4;
        int line = font.lineHeight + 2;
        String id = isotope.id().toUpperCase().replace("NR:", "").replace("_", "-");
        g.drawString(font, __("jei.nuclear_radiation.isotope_stats.id", id), x, y, 0x202020, false);
        y += line;
        g.drawString(font, __("jei.nuclear_radiation.isotope_stats.half_life", JeiFormat.halfLife(isotope.halfLifeTicks())), x, y, 0x0E5A1A, false);
        y += line;

        RadiationQuality q = isotope.quality();
        float wAlpha = isotope.alphaStrength() * q.qAlpha;
        float wBeta = isotope.betaStrength() * q.qBeta;
        float wXRay = isotope.xRayStrength() * q.qXRay;
        float wNeutron = isotope.neutronStrength() * q.qNeutron;
        float total = wAlpha + wBeta + wXRay + wNeutron;

        if (total > 0f) {
            g.drawString(font, __("jei.nuclear_radiation.isotope_stats.dose_mix"), x, y, 0x8B4500, false);
            y += line;

            float scale = 0.75f;
            int smallLine = Math.max(1, (int) Math.ceil(font.lineHeight * scale) + 1);
            g.pose().pushPose();
            g.pose().scale(scale, scale, 1f);
            int sx = Math.round(x / scale);
            int sy = Math.round(y / scale);
            int rows = 0;
            if (isotope.alphaStrength() > 0f) {
                g.drawString(font, __("jei.nuclear_radiation.isotope_stats.alpha", JeiFormat.doseShare(wAlpha / total)), sx, sy, 0x303030, false);
                sy += font.lineHeight + 2;
                rows++;
            }
            if (isotope.betaStrength() > 0f) {
                g.drawString(font, __("jei.nuclear_radiation.isotope_stats.beta", JeiFormat.doseShare(wBeta / total)), sx, sy, 0x303030, false);
                sy += font.lineHeight + 2;
                rows++;
            }
            if (isotope.xRayStrength() > 0f) {
                g.drawString(font, __("jei.nuclear_radiation.isotope_stats.x_gamma", JeiFormat.doseShare(wXRay / total)), sx, sy, 0x303030, false);
                sy += font.lineHeight + 2;
                rows++;
            }
            if (isotope.neutronStrength() > 0f) {
                g.drawString(font, __("jei.nuclear_radiation.isotope_stats.neutron", JeiFormat.doseShare(wNeutron / total)), sx, sy, 0x303030, false);
                sy += font.lineHeight + 2;
                rows++;
            }
            g.pose().popPose();
            y += smallLine * rows;
        }

        String decay = isotope.decaysTo().map(Isotope::id).orElse("stable");
        decay = decay.toUpperCase().replace("NR:", "").replace("_", "-");
        g.drawString(font, __("jei.nuclear_radiation.isotope_stats.decays_to", decay), x, y, 0x8B0000, false);
    }
}
