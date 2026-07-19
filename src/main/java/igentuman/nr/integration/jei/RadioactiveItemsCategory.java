package igentuman.nr.integration.jei;

import igentuman.nr.api.isotope.IsotopeStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import static igentuman.nr.util.TextUtils.__;

public class RadioactiveItemsCategory implements IRecipeCategory<RadioactiveItemEntry> {

    public static final int WIDTH = 162;
    public static final int HEIGHT = 128;

    private final IDrawable icon;

    public RadioactiveItemsCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(Items.GUNPOWDER.getDefaultInstance());
    }

    @Override
    public RecipeType<RadioactiveItemEntry> getRecipeType() {
        return NRJeiTypes.RADIOACTIVE_ITEMS;
    }

    @Override
    public Component getTitle() {
        return __("jei.nuclear_radiation.category.radioactive_items");
    }

    @Override
    public int getWidth() { return WIDTH; }

    @Override
    public int getHeight() { return HEIGHT; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RadioactiveItemEntry recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 6).addItemStack(recipe.stack());
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(recipe.stack());
    }

    @Override
    public void draw(RadioactiveItemEntry recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics g, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        int x = 30;
        int y = 6;
        int line = font.lineHeight + 1;

        g.drawString(font, recipe.stack().getHoverName(), x, y, 0x202020, false);
        y += line;
        g.drawString(font, __("jei.nuclear_radiation.radioactive_items.total", JeiFormat.activity(recipe.profile().totalActivityBq())),
                x, y, 0x0E5A1A, false);

        y += line;
        x = 6;
        g.drawString(font, __("jei.nuclear_radiation.radioactive_items.radiation_mix"), x, y, 0x8B4500, false);
        double total = recipe.profile().totalActivityBq();
        y += line;
        float scale = 0.75f;
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, __("jei.nuclear_radiation.radioactive_items.alpha", JeiFormat.percent((float) (recipe.profile().alphaActivityBq() / total))), (int) (x / scale), (int) (y / scale), 0x303030, false);
        y += (int) (line * scale);
        g.drawString(font, __("jei.nuclear_radiation.radioactive_items.beta", JeiFormat.percent((float) (recipe.profile().betaActivityBq() / total))), (int) (x / scale), (int) (y / scale), 0x303030, false);
        y += (int) (line * scale);
        g.drawString(font, __("jei.nuclear_radiation.radioactive_items.gamma", JeiFormat.percent((float) (recipe.profile().xRayActivityBq() / total))), (int) (x / scale), (int) (y / scale), 0x303030, false);
        y += (int) (line * scale);
        g.drawString(font, __("jei.nuclear_radiation.radioactive_items.neutron", JeiFormat.percent((float) (recipe.profile().neutronActivityBq() / total))), (int) (x / scale), (int) (y / scale), 0x303030, false);
        y += (int) (line * scale);
        g.pose().popPose();

        y += 2;
        g.drawString(font, __("jei.nuclear_radiation.radioactive_items.isotopes"), 6, y, 0x8B4500, false);
        y += line;

        int max = Math.min(6, recipe.profile().stacks().size());
        int i = 0;
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1f);
        for (IsotopeStack s : recipe.profile().stacks()) {
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
