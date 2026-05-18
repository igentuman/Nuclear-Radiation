package igentuman.nr.integration.jei;

import igentuman.nr.api.Isotope;
import igentuman.nr.core.RadiationQuality;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import static igentuman.nr.util.TextUtils.__;

public class IsotopeStatsCategory implements IRecipeCategory<Isotope> {

    public static final int WIDTH = 162;
    public static final int HEIGHT = 90;

    private final IDrawable icon;

    public IsotopeStatsCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(Items.GLOWSTONE_DUST.getDefaultInstance());
    }

    @Override
    public RecipeType<Isotope> getRecipeType() {
        return NRJeiTypes.ISOTOPE_STATS;
    }

    @Override
    public Component getTitle() {
        return __("jei.nuclear_radiation.category.isotope_stats");
    }

    @Override
    public int getWidth() { return WIDTH; }

    @Override
    public int getHeight() { return HEIGHT; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Isotope recipe, IFocusGroup focuses) {
        // No item slots — purely informational
    }

    @Override
    public ResourceLocation getRegistryName(Isotope recipe) {
        String id = recipe.id();
        int colon = id.indexOf(':');
        if (colon < 0) return ResourceLocation.fromNamespaceAndPath("nr", id);
        return ResourceLocation.fromNamespaceAndPath(id.substring(0, colon), id.substring(colon + 1));
    }

    @Override
    public void draw(Isotope recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics g, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        int x = 4;
        int y = 4;
        int line = font.lineHeight + 2;

        g.drawString(font, __("jei.nuclear_radiation.isotope_stats.id", recipe.id()), x, y, 0x202020, false);
        y += line;
        g.drawString(font, __("jei.nuclear_radiation.isotope_stats.half_life", JeiFormat.halfLife(recipe.halfLifeTicks())), x, y, 0x0E5A1A, false);
        y += line;

        g.drawString(font, __("jei.nuclear_radiation.isotope_stats.radiation_mix"), x, y, 0x8B4500, false);
        y += line;

        float scale = 0.75f;
        int smallLine = Math.max(1, (int) Math.ceil(font.lineHeight * scale) + 1);
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1f);
        int sx = Math.round(x / scale);
        int sy = Math.round(y / scale);
        g.drawString(font, __("jei.nuclear_radiation.isotope_stats.alpha_beta", JeiFormat.percent(recipe.alphaBetaStrength())), sx, sy, 0x303030, false);
        sy += font.lineHeight + 2;
        g.drawString(font, __("jei.nuclear_radiation.isotope_stats.x_gamma", JeiFormat.percent(recipe.xRayStrength())), sx, sy, 0x303030, false);
        sy += font.lineHeight + 2;
        g.drawString(font, __("jei.nuclear_radiation.isotope_stats.neutron", JeiFormat.percent(recipe.neutronStrength())), sx, sy, 0x303030, false);
        sy += font.lineHeight + 2;

        RadiationQuality q = recipe.quality();
        g.drawString(font, __("jei.nuclear_radiation.isotope_stats.quality",
                String.format("%.1f", q.qXRay), String.format("%.1f", q.qBeta),
                String.format("%.1f", q.qAlpha), String.format("%.1f", q.qNeutron)), sx, sy, 0x1A3D7A, false);
        g.pose().popPose();
        y += smallLine * 4;

        String decay = recipe.decaysTo().map(Isotope::id).orElse("stable");
        g.drawString(font, __("jei.nuclear_radiation.isotope_stats.decays_to", decay), x, y, 0x8B0000, false);
    }
}
