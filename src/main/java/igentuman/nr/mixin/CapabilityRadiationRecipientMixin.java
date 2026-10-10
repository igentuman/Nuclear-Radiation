package igentuman.nr.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import voltaic.api.radiation.CapabilityRadiationRecipient;

@Mixin(value = CapabilityRadiationRecipient.class, remap = false)
public abstract class CapabilityRadiationRecipientMixin {

    @Inject(method = "recieveRadiation", at = @At("HEAD"), cancellable = true, remap = false)
    public void recieveRadiationMixin(LivingEntity entity, double rads, double strength, CallbackInfo ci)
    {
        ci.cancel();
    }
}
