package igentuman.nr.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.nuclearteam.createnuclear.content.radiation.RadiationEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RadiationEffect.class, remap = false)
public abstract class CreateNuclearRadiationEffectMixin {
    @Inject(method = "applyEffectTick", at = @At("HEAD"), cancellable = true)
    private void nr$suppressRadiationEffect(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "onContaminate", at = @At("HEAD"), cancellable = true)
    private void nr$suppressContagion(LivingEntity entity, CallbackInfo ci) {
        ci.cancel();
    }
}
