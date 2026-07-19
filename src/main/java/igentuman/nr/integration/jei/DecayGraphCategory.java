package igentuman.nr.integration.jei;

import igentuman.nr.api.isotope.Isotope;
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

public class DecayGraphCategory implements IRecipeCategory<Isotope> {

    public static final int WIDTH = 162;
    public static final int HEIGHT = 110;

    private static final int GRAPH_X = 16;
    private static final int GRAPH_Y = 24;
    private static final int GRAPH_W = 130;
    private static final int GRAPH_H = 60;

    private static final int COLOR_AXIS = 0xFF808080;
    private static final int COLOR_GRID = 0xFF303030;
    private static final int COLOR_CURVE = 0xFF40D0FF;
    private static final int COLOR_HALF_LINE = 0xFFA0A040;

    private final IDrawable icon;

    public DecayGraphCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(Items.CLOCK.getDefaultInstance());
    }

    @Override
    public RecipeType<Isotope> getRecipeType() {
        return NRJeiTypes.DECAY_GRAPH;
    }

    @Override
    public Component getTitle() {
        return __("jei.nuclear_radiation.category.decay_graph");
    }

    @Override
    public int getWidth() { return WIDTH; }

    @Override
    public int getHeight() { return HEIGHT; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Isotope recipe, IFocusGroup focuses) {
        // no slots
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
        long halfLife = recipe.halfLifeTicks();

        g.drawString(font, Component.literal(recipe.id()), 4, 4, 0x202020, false);
        g.drawString(font, Component.literal("T1/2 = " + JeiFormat.halfLife(halfLife)),
                4, 4 + font.lineHeight + 1, 0x0E5A1A, false);

        int x0 = GRAPH_X;
        int y0 = GRAPH_Y;
        int w  = GRAPH_W;
        int h  = GRAPH_H;

        // background
        g.fill(x0, y0, x0 + w, y0 + h, 0xFF101820);

        // grid lines at each half-life (5 visible half-lives)
        for (int i = 1; i <= 5; i++) {
            int gx = x0 + (int) (w * (i / 5.0));
            g.fill(gx, y0, gx + 1, y0 + h, COLOR_GRID);
        }
        // mid horizontal grid (50% activity line)
        int midY = y0 + h / 2;
        g.fill(x0, midY, x0 + w, midY + 1, COLOR_HALF_LINE);

        // axes
        g.fill(x0, y0 + h, x0 + w, y0 + h + 1, COLOR_AXIS); // x axis
        g.fill(x0, y0, x0 + 1, y0 + h, COLOR_AXIS);         // y axis

        // curve: N(t)/N0 = exp(-ln2 * t / T)
        // map x in [0, w] -> t in [0, 5*T]
        int prevY = -1;
        for (int dx = 0; dx < w; dx++) {
            double frac = (dx / (double) w) * 5.0; // 0..5 half-lives
            double activity = Math.pow(0.5, frac);
            int py = y0 + h - (int) Math.round(activity * h);
            if (py < y0) py = y0;
            if (py > y0 + h - 1) py = y0 + h - 1;
            if (prevY >= 0) {
                int from = Math.min(prevY, py);
                int to = Math.max(prevY, py);
                g.fill(x0 + dx, from, x0 + dx + 1, to + 1, COLOR_CURVE);
            } else {
                g.fill(x0 + dx, py, x0 + dx + 1, py + 1, COLOR_CURVE);
            }
            prevY = py;
        }

        // axis labels (rendered outside dark plot, on JEI light-gray background)
        g.drawString(font, Component.literal("100%"), x0 - 14, y0 - 2, 0x303030, false);
        g.drawString(font, Component.literal("50%"),  x0 - 14, midY - 4, 0x303030, false);
        g.drawString(font, Component.literal("0%"),   x0 - 14, y0 + h - 4, 0x303030, false);

        for (int i = 1; i <= 5; i++) {
            int gx = x0 + (int) (w * (i / 5.0));
            g.drawString(font, Component.literal(i + "T"), gx - 4, y0 + h + 2, 0x303030, false);
        }
    }
}
