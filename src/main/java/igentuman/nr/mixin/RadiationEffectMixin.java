package igentuman.nr.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.nuclearteam.createnuclear.content.effects.RadiationEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RadiationEffect.class, remap = false)
public abstract class RadiationEffectMixin {

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void nr$onConstruct(CallbackInfo ci) {
    }

    @Inject(method = "applyEffectTick", at = @At("HEAD"), remap = false, cancellable = true)
    private void nr$suppressEffect(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
