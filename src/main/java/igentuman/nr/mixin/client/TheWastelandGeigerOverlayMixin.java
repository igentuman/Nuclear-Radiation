package igentuman.nr.mixin.client;

import igentuman.nr.config.RadiationConfig;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The Nuclear Radiation HUD reads the real dose rate for this counter. */
@Mixin(targets = "net.mcreator.thewastelandreworked.client.screens.GeigerCounterOverlayOverlay", remap = false)
public abstract class TheWastelandGeigerOverlayMixin {
    @Inject(method = "eventHandler", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$hideLegacyReading(RenderGuiEvent.Pre event, CallbackInfo ci) {
        if (RadiationConfig.masterEnabled()) ci.cancel();
    }
}
