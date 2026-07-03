package igentuman.nr.mixin;

import com.nred.nuclearcraft.radiation.RadiationHandler;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RadiationHandler.class, remap = false)
public abstract class NCRadiationHandlerMixin {

    @Inject(method = "updatePlayerRadiation", at = @At("HEAD"), remap = false, cancellable = true)
    public void nr$suppressPlayerRadiation(PlayerTickEvent.Pre event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "updateLevelRadiation", at = @At("HEAD"), remap = false, cancellable = true)
    public void nr$suppressLevelRadiation(LevelTickEvent.Pre event, CallbackInfo ci) {
        ci.cancel();
    }
}
