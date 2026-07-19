package igentuman.nr.integration.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nr.api.isotope.IsotopeStack;
import igentuman.nr.integration.jei.JeiFormat;
import igentuman.nr.integration.jei.RadioactiveItemEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import static igentuman.nr.util.TextUtils.__;

public class RadioactiveItemEmiRecipe extends BasicEmiRecipe {

    public static final int WIDTH = 162;
    public static final int HEIGHT = 128;

    private final RadioactiveItemEntry entry;

    public RadioactiveItemEmiRecipe(RadioactiveItemEntry entry) {
        super(NREmiCategories.RADIOACTIVE_ITEMS, synthId(entry), WIDTH, HEIGHT);
        this.entry = entry;
        EmiStack stack = EmiStack.of(entry.stack());
        inputs.add(stack);
        outputs.add(stack);
    }

    private static ResourceLocation synthId(RadioactiveItemEntry entry) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(entry.stack().getItem());
        return ResourceLocation.fromNamespaceAndPath("nuclear_radiation",
                "/radioactive_items/" + key.getNamespace() + "/" + key.getPath());
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
        y += line;
        g.drawString(font, __("jei.nuclear_radiation.radioactive_items.total", JeiFormat.activity(entry.profile().totalActivityBq())),
                x, y, 0x0E5A1A, false);

        y += line;
        x = 6;
        g.drawString(font, __("jei.nuclear_radiation.radioactive_items.radiation_mix"), x, y, 0x8B4500, false);
        double total = entry.profile().totalActivityBq();
        y += line;
        float scale = 0.75f;
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, __("jei.nuclear_radiation.radioactive_items.alpha", JeiFormat.percent((float) (entry.profile().alphaActivityBq() / total))), (int) (x / scale), (int) (y / scale), 0x303030, false);
        y += (int) (line * scale);
        g.drawString(font, __("jei.nuclear_radiation.radioactive_items.beta", JeiFormat.percent((float) (entry.profile().betaActivityBq() / total))), (int) (x / scale), (int) (y / scale), 0x303030, false);
        y += (int) (line * scale);
        g.drawString(font, __("jei.nuclear_radiation.radioactive_items.gamma", JeiFormat.percent((float) (entry.profile().xRayActivityBq() / total))), (int) (x / scale), (int) (y / scale), 0x303030, false);
        y += (int) (line * scale);
        g.drawString(font, __("jei.nuclear_radiation.radioactive_items.neutron", JeiFormat.percent((float) (entry.profile().neutronActivityBq() / total))), (int) (x / scale), (int) (y / scale), 0x303030, false);
        y += (int) (line * scale);
        g.pose().popPose();

        y += 2;
        g.drawString(font, __("jei.nuclear_radiation.radioactive_items.isotopes"), 6, y, 0x8B4500, false);
        y += line;

        int max = Math.min(6, entry.profile().stacks().size());
        int i = 0;
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1f);
        for (IsotopeStack s : entry.profile().stacks()) {
            if (i >= max) break;
            String idShort = stripNs(s.isotope().id()).toUpperCase().replace("_", "-");
            g.drawString(font, __("jei.nuclear_radiation.radioactive_items.isotope_entry", padRight(idShort, 8), JeiFormat.activity(s.currentActivityBq())), (int) (6 / scale), (int) (y / scale), 0x303030, false);
            y += (int) (line * scale);
            i++;
        }
        g.pose().popPose();
    }

    private static String stripNs(String id) {
        int c = id.indexOf(':');
        return c < 0 ? id : id.substring(c + 1);
    }

    private static String padRight(String s, int len) {
        if (s.length() >= len) return s;
        StringBuilder b = new StringBuilder(s);
        while (b.length() < len) b.append(' ');
        return b.toString();
    }
}
