package igentuman.nr.mixin;

import igentuman.nr.config.RadiationConfig;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Nuclear Radiation handles the same block, inventory, and biome exposure sources. */
@Mixin(targets = "net.mcreator.thewastelandreworked.procedures.IsPlayerExposedToRadiationProcedure", remap = false)
public abstract class TheWastelandExposureMixin {
    @Inject(method = "onPlayerTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$stopLegacyExposure(PlayerTickEvent.Post event, CallbackInfo ci) {
        if (RadiationConfig.masterEnabled()) ci.cancel();
    }
}
