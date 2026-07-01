package igentuman.nr.integration.jei;

import igentuman.nr.recipe.MutationRecipe;
import igentuman.nr.util.TextUtils;
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
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

import static igentuman.nr.util.TextUtils.__;

public class MutationCategory implements IRecipeCategory<MutationRecipe> {

    public static final int WIDTH = 162;
    public static final int HEIGHT = 120;

    private static final int CELL_W = 48, CELL_H = 52;
    private static final int CELL_Y = 14;
    private static final int IN_X = 8, OUT_X = WIDTH + 40 - CELL_W;
    private static final int LABEL_Y = CELL_Y + CELL_H + 2;
    private static final int STATS_Y = LABEL_Y;

    private final IDrawable icon;
    private final Map<MutationRecipe, LivingEntity[]> entityCache = new IdentityHashMap<>();
    private Level cacheLevel;

    public MutationCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(Items.CREEPER_SPAWN_EGG.getDefaultInstance());
    }

    @Override
    public RecipeType<MutationRecipe> getRecipeType() {
        return NRJeiTypes.MUTATION;
    }

    @Override
    public Component getTitle() {
        return __("jei.nuclear_radiation.category.mutation");
    }

    @Override
    public int getWidth() { return WIDTH; }

    @Override
    public int getHeight() { return HEIGHT; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MutationRecipe recipe, IFocusGroup focuses) {
        SpawnEggItem inEgg = SpawnEggItem.byId(recipe.input().type());
        if (inEgg != null) {
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(inEgg));
        }
        SpawnEggItem outEgg = SpawnEggItem.byId(recipe.result().type());
        if (outEgg != null) {
            builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(new ItemStack(outEgg));
        }
    }

    @Override
    public void draw(MutationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics g, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        LivingEntity[] pair = entities(recipe);
        g.pose().pushPose();
        g.pose().scale(0.75F, 0.75F, 1F);
        drawEntity(g, pair == null ? null : pair[0], IN_X, CELL_Y);
        drawEntity(g, pair == null ? null : pair[1], OUT_X, CELL_Y);
        drawCentered(g, font, recipe.input().type().getDescription(), IN_X + CELL_W / 2, LABEL_Y);
        drawCentered(g, font, recipe.result().type().getDescription(), OUT_X + CELL_W / 2, LABEL_Y);
        g.pose().popPose();
        g.drawString(font, "→", WIDTH / 2 - 3, CELL_Y + CELL_H / 2 - 4, 0x404040, false);



        int line = font.lineHeight + 2;
        int y = STATS_Y;
        g.drawString(font, __("jei.nuclear_radiation.mutation.dose", TextUtils.formatSv(recipe.totalDoseSv())), 6, y, 0x0E5A1A, false);
        y += line;
        g.drawString(font, __("jei.nuclear_radiation.mutation.rate"), 6, y, 0x1A3D7A, false);
        y += line;
        g.drawString(font, __("jei.nuclear_radiation.mutation.min_rate", TextUtils.formatSvPerHour(recipe.minSvPerHour())), 6, y, 0x1A3D7A, false);
        if(recipe.maxSvPerHour() < Double.MAX_VALUE) {
            y += line;
            g.drawString(font, __("jei.nuclear_radiation.mutation.max_rate", TextUtils.formatSvPerHour(recipe.maxSvPerHour())), 6, y, 0x1A3D7A, false);
        }
        y += line;
        g.drawString(font, __("jei.nuclear_radiation.mutation.chance", JeiFormat.percent(recipe.chance())), WIDTH / 2 - 30, 10, 0x8B0000, false);
    }

    // Renders the live entity centered in the box. Deliberately avoids
    // InventoryScreen.renderEntityInInventoryFollowsAngle: that helper calls enableScissor with
    // absolute screen coords, but JEI draws with the pose translated to the recipe origin, so the
    // scissor clips the entity to the screen's top-left corner. renderEntityInInventory has no scissor.
    private void drawEntity(GuiGraphics g, LivingEntity entity, int boxX, int boxY) {
        if (entity == null) return;
        float size = Math.max(entity.getBbHeight(), entity.getBbWidth());
        int scale = (int) Math.max(10, Math.min(38, (CELL_H * 0.85f) / Math.max(0.5f, size)));
        float cx = boxX + CELL_W / 2f;
        float cy = boxY + CELL_H / 2f;
        float angleX = 0.6f, angleY = 0.4f;

        Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf camera = new Quaternionf().rotateX(-angleY * 40.0F * ((float) Math.PI / 180F));
        pose.mul(camera);

        float bBody = entity.yBodyRot, bY = entity.getYRot(), bX = entity.getXRot(),
              bHeadO = entity.yHeadRotO, bHead = entity.yHeadRot;
        entity.yBodyRot = 180F + angleX * 20F;
        entity.setYRot(180F + angleX * 40F);
        entity.setXRot(angleY * 20F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        float s = entity.getScale();
        Vector3f translate = new Vector3f(0.0F, entity.getBbHeight() / 2.0F, 0.0F);
        InventoryScreen.renderEntityInInventory(g, cx, cy, scale / s, translate, pose, camera, entity);

        entity.yBodyRot = bBody;
        entity.setYRot(bY);
        entity.setXRot(bX);
        entity.yHeadRotO = bHeadO;
        entity.yHeadRot = bHead;
    }

    private static void drawCentered(GuiGraphics g, Font font, Component text, int centerX, int y) {
        int w = font.width(text);
        g.drawString(font, text, centerX - w / 2, y, 0x202020, false);
    }

    private LivingEntity[] entities(MutationRecipe recipe) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return null;
        if (level != cacheLevel) {
            entityCache.clear();
            cacheLevel = level;
        }
        return entityCache.computeIfAbsent(recipe, r -> new LivingEntity[]{
                make(level, r.input().type(), r.input().nbt()),
                make(level, r.result().type(), r.result().nbt())
        });
    }

    private static LivingEntity make(Level level, EntityType<?> type, Optional<CompoundTag> nbt) {
        Entity created = type.create(level);
        if (!(created instanceof LivingEntity entity)) return null;
        if (nbt.isPresent()) {
            CompoundTag tag = entity.saveWithoutId(new CompoundTag());
            tag.merge(nbt.get());
            entity.load(tag);
        }
        return entity;
    }
}
