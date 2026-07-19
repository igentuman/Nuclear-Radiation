package igentuman.nr.integration.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import igentuman.nr.integration.jei.ArmorProtectionEntry;
import igentuman.nr.integration.jei.JeiFormat;
import igentuman.nr.api.shielding.ArmorProtectionRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import static igentuman.nr.util.TextUtils.__;

public class ArmorProtectionEmiRecipe extends BasicEmiRecipe {

    public static final int WIDTH = 162;
    public static final int HEIGHT = 80;

    private final ArmorProtectionEntry entry;

    public ArmorProtectionEmiRecipe(ArmorProtectionEntry entry) {
        super(NREmiCategories.ARMOR_PROTECTION, synthId(entry), WIDTH, HEIGHT);
        this.entry = entry;
        inputs.add(EmiStack.of(entry.stack()));
    }

    private static ResourceLocation synthId(ArmorProtectionEntry entry) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(entry.stack().getItem());
        return ResourceLocation.fromNamespaceAndPath("nuclear_radiation",
                "/armor_protection/" + key.getNamespace() + "/" + key.getPath());
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(EmiStack.of(entry.stack()), 6, 6);
        widgets.addDrawable(0, 0, WIDTH, HEIGHT, (g, mouseX, mouseY, delta) -> draw(g));
    }

    private void draw(GuiGraphics g) {
        Font font = Minecraft.getInstance().font;
        int x = 30;
        int y = 6;
        int line = font.lineHeight + 1;

        g.drawString(font, entry.stack().getHoverName(), x, y, 0x202020, false);

        ArmorProtectionRegistry.Protection p = entry.protection();
        int yy = 30;
        g.drawString(font, __("jei.nuclear_radiation.armor_protection.xray", JeiFormat.percent((float) p.xray())), 6, yy, 0x1A3D7A, false);
        yy += line;
        g.drawString(font, __("jei.nuclear_radiation.armor_protection.alpha", JeiFormat.percent((float) p.alpha())), 6, yy, 0x0E5A1A, false);
        yy += line;
        g.drawString(font, __("jei.nuclear_radiation.armor_protection.beta", JeiFormat.percent((float) p.beta())), 6, yy, 0x8B4500, false);
        yy += line;
        g.drawString(font, __("jei.nuclear_radiation.armor_protection.neutron", JeiFormat.percent((float) p.neutron())), 6, yy, 0x8B0000, false);
    }
}
