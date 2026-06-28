package igentuman.nr.block.client;

import igentuman.nr.network.CreativeRadSourceConfigPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class CreativeRadSourceScreen extends Screen {

    private static final int PANEL_W = 240;
    private static final int PANEL_H = 160;
    private static final int FIELD_W = 120;
    private static final int FIELD_H = 20;
    private static final int LABEL_COLOR = 0xE0E0E0;

    private final BlockPos blockPos;
    private final double initialAlpha;
    private final double initialBeta;
    private final double initialXRay;
    private final double initialNeutron;

    private EditBox alphaField;
    private EditBox betaField;
    private EditBox xRayField;
    private EditBox neutronField;

    public CreativeRadSourceScreen(BlockPos pos, double alpha, double beta, double xray, double neutron) {
        super(Component.translatable("block.nuclear_radiation.creative_rad_source"));
        this.blockPos = pos;
        this.initialAlpha = alpha;
        this.initialBeta = beta;
        this.initialXRay = xray;
        this.initialNeutron = neutron;
    }

    @Override
    protected void init() {
        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;

        int fieldX = left + 110;
        int row0 = top + 30;
        int rowStep = 26;

        alphaField = addEditBox(fieldX, row0, initialAlpha);
        betaField  = addEditBox(fieldX, row0 + rowStep, initialBeta);
        xRayField  = addEditBox(fieldX, row0 + rowStep * 2, initialXRay);
        neutronField = addEditBox(fieldX, row0 + rowStep * 3, initialNeutron);

        addRenderableWidget(Button.builder(
                        Component.translatable("gui.nuclear_radiation.creative_rad_source.apply"),
                        btn -> applyConfig())
                .bounds(left + (PANEL_W - 80) / 2, top + PANEL_H - 28, 80, 20)
                .build());
    }

    private EditBox addEditBox(int x, int y, double initial) {
        EditBox box = new EditBox(font, x, y, FIELD_W, FIELD_H, Component.empty());
        box.setMaxLength(32);
        box.setValue(formatBq(initial));
        box.setFilter(s -> s.isEmpty() || s.matches("[0-9]*\\.?[0-9]*([eE][+-]?[0-9]*)?"));
        addRenderableWidget(box);
        return box;
    }

    private String formatBq(double bq) {
        if (bq == 0.0) return "0";
        return String.valueOf(bq / 1_000_000.0);
    }

    private double parseBq(EditBox box) {
        try {
            String text = box.getValue().trim();
            if (text.isEmpty()) return 0.0;
            return Math.max(0.0, Double.parseDouble(text)) * 1_000_000.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void applyConfig() {
        double alpha   = parseBq(alphaField);
        double beta    = parseBq(betaField);
        double xray    = parseBq(xRayField);
        double neutron = parseBq(neutronField);
        PacketDistributor.sendToServer(new CreativeRadSourceConfigPayload(
                blockPos, alpha, beta, xray, neutron));
        onClose();
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        gfx.fill(0, 0, width, height, 0x80000000);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderBackground(gfx, mouseX, mouseY, partialTick);

        int left = (width - PANEL_W) / 2;
        int top  = (height - PANEL_H) / 2;

        gfx.fill(left, top, left + PANEL_W, top + PANEL_H, 0xC0101010);
        gfx.renderOutline(left, top, PANEL_W, PANEL_H, 0xFF555555);

        gfx.drawCenteredString(font,
                Component.translatable("block.nuclear_radiation.creative_rad_source"),
                width / 2, top + 8, 0xFFFFFF);

        int labelX = left + 10;
        int row0 = top + 36;
        int rowStep = 26;

        gfx.drawString(font, Component.translatable("gui.nuclear_radiation.creative_rad_source.alpha"),
                labelX, row0, LABEL_COLOR);
        gfx.drawString(font, Component.translatable("gui.nuclear_radiation.creative_rad_source.beta"),
                labelX, row0 + rowStep, LABEL_COLOR);
        gfx.drawString(font, Component.translatable("gui.nuclear_radiation.creative_rad_source.xray"),
                labelX, row0 + rowStep * 2, LABEL_COLOR);
        gfx.drawString(font, Component.translatable("gui.nuclear_radiation.creative_rad_source.neutron"),
                labelX, row0 + rowStep * 3, LABEL_COLOR);

        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
