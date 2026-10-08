package igentuman.nr.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.content.radiation.capability.RadiationCapability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RadiationCapability.class, remap = false)
public class CreateNuclearRadiationCapabilityMixin {
    @Inject(method = "tickRadiation", at = @At("HEAD"), cancellable = true)
    private static void nr$suppressRadiationTick(LivingEntity entity, CallbackInfo ci) {
        if (!entity.level().isClientSide && entity.hasEffect(CNEffects.RADIATION)) {
            entity.removeEffect(CNEffects.RADIATION);
        }
        ci.cancel();
    }

    @Inject(method = "applyContagion", at = @At("HEAD"), cancellable = true)
    private static void nr$suppressContagion(LivingEntity entity, double dose, int ticks, CallbackInfo ci) {
        ci.cancel();
    }
}
