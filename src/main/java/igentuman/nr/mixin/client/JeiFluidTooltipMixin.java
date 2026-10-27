package igentuman.nr.mixin.client;

import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.config.GeneralConfig;
import igentuman.nr.events.RadiationTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "mezz.jei.neoforge.platform.FluidHelper", remap = false)
public abstract class JeiFluidTooltipMixin {

    @Inject(method = "getTooltip(Ljava/util/List;Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/minecraft/world/item/TooltipFlag;)V",
            at = @At("TAIL"), remap = false)
    private void nr$addRadiationTooltip(List<Component> tooltip, FluidStack ingredient,
                                        TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (!GeneralConfig.RADIATION_ENABLED.get() || ingredient.isEmpty()) return;
        RadiationTooltip.addRadiationTooltip(tooltip,
                RadiationBindings.of(ingredient.getFluid().defaultFluidState()), 1);
    }
}
