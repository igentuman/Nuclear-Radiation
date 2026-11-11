package igentuman.nr.integration.jei;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.config.GeneralConfig;
import igentuman.nr.events.RadiationTooltip;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds fluid radiation information through public APIs, without transforming JEI classes.
 * Kept inside the JEI plugin so it is never registered when JEI is absent.
 */
final class JeiFluidTooltip {
    private static final Logger LOGGER = LogUtils.getLogger();
    private IJeiRuntime runtime;
    private boolean registered;
    private boolean disabled;

    void register(IJeiRuntime runtime) {
        if (disabled) return;
        try {
            this.runtime = runtime;
            if (!registered) {
                NeoForge.EVENT_BUS.addListener(this::onTooltip);
                registered = true;
            }
        } catch (RuntimeException | LinkageError e) {
            disable(e);
        }
    }

    void clearRuntime() {
        runtime = null;
    }

    private void onTooltip(RenderTooltipEvent.GatherComponents event) {
        if (disabled || runtime == null) return;
        try {
            if (!GeneralConfig.RADIATION_ENABLED.get() || !event.getItemStack().isEmpty()
                    || event.getTooltipElements().isEmpty()) return;

            FluidStack fluid = getFluidUnderMouse();
            if (fluid == null || fluid.isEmpty()) return;

            // GatherComponents also fires for unrelated UI tooltips. Only augment a fluid title.
            String title = event.getTooltipElements().getFirst().left()
                    .map(text -> text.getString()).orElse("");
            String fluidName = fluid.getHoverName().getString();
            String flowingName = Component.translatable("jei.tooltip.liquid.flowing", fluid.getHoverName()).getString();
            if (!title.equals(fluidName) && !title.equals(flowingName)) return;

            // Prepare all lines before touching the original tooltip, so failures leave it intact.
            List<Component> lines = new ArrayList<>();
            RadiationTooltip.addRadiationTooltip(lines,
                    RadiationBindings.of(fluid.getFluid().defaultFluidState()), 1);
            for (Component line : lines) {
                var element = Either.<FormattedText, TooltipComponent>left(line);
                if (!event.getTooltipElements().contains(element)) {
                    event.getTooltipElements().add(element);
                }
            }
        } catch (RuntimeException | LinkageError e) {
            disable(e);
        }
    }

    private FluidStack getFluidUnderMouse() {
        FluidStack fluid = runtime.getIngredientListOverlay().getIngredientUnderMouse(NeoForgeTypes.FLUID_STACK);
        if (fluid != null) return fluid;
        fluid = runtime.getBookmarkOverlay().getIngredientUnderMouse(NeoForgeTypes.FLUID_STACK);
        if (fluid != null) return fluid;
        return runtime.getRecipesGui().getIngredientUnderMouse(NeoForgeTypes.FLUID_STACK).orElse(null);
    }

    private void disable(Throwable cause) {
        disabled = true;
        runtime = null;
        LOGGER.warn("JEI fluid radiation tooltips disabled for this session after a compatibility failure", cause);
    }
}
