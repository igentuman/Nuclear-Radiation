package igentuman.nr.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "lil1llilol11.lo1i1iloioil.liooillio0101i10iiio010l", remap = false)
public abstract class ProjectExplosiveConfigMixin {
    @Inject(method = "l0o001o0ol0ilooioooil11i()Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$useNuclearRadiation(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
