package igentuman.nr.mixin.client;

import com.nred.nuclearcraft.handler.TooltipHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = TooltipHandler.class, remap = false)
public abstract class NCTooltipHandlerMixin {

    @Inject(method = "addRadiationTooltip", at = @At("HEAD"), remap = false, cancellable = true)
    private static void nr$hideRadiationTooltip(List<Component> tooltip, ItemStack stack, CallbackInfo ci) {
       // ci.cancel();
    }

    @Inject(method = "addArmorRadiationTooltip", at = @At("HEAD"), remap = false, cancellable = true)
    private static void nr$hideArmorRadiationTooltip(List<Component> tooltip, ItemStack stack, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "addFoodRadiationTooltip", at = @At("HEAD"), remap = false, cancellable = true)
    private static void nr$hideFoodRadiationTooltip(List<Component> tooltip, ItemStack stack, CallbackInfo ci) {
        ci.cancel();
    }
}
