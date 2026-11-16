package igentuman.nr.compat.ntm.mixin;

import igentuman.nr.compat.ntm.NtmCompatibility;
import igentuman.nr.compat.ntm.NtmNativeWorldOwner;
import igentuman.nr.compat.ntm.NtmWorldBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.hbm.handler.radiation.RadiationWorldHandler", remap = false)
public abstract class RadiationWorldHandlerMixin {
    @Inject(method = "handleWorldDestruction", at = @At("HEAD"), cancellable = true)
    private static void nr$worldEffects(@Coerce Object world, CallbackInfo ci) {
        if (!NtmCompatibility.enabled()) return;
        NtmWorldBridge.drainWorldEffects(((NtmNativeWorldOwner) world).nr$level());
        ci.cancel();
    }
}
