package igentuman.nr.mixin;

import igentuman.nr.config.RadiationConfig;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Stops the mod's legacy player dose and symptom tick when Nuclear Radiation is installed. */
@Mixin(targets = "net.mcreator.thewastelandreworked.procedures.PlayerRadiationExpostitionProcedure", remap = false)
public abstract class TheWastelandDoseMixin {
    @Inject(method = "onPlayerTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$stopLegacyDose(PlayerTickEvent.Post event, CallbackInfo ci) {
        if (RadiationConfig.masterEnabled()) ci.cancel();
    }
}
