package igentuman.nr.compat.ntm.mixin;

import com.hbm.capability.HbmLivingProps;
import igentuman.nr.compat.ntm.NtmCompatibility;
import igentuman.nr.compat.ntm.NtmEntityOwner;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.hbm.capability.HbmLivingProps", remap = false)
public abstract class HbmLivingPropsMixin {
    @Inject(method = "getData", at = @At("RETURN"))
    private static void nr$bindOwner(LivingEntity entity, CallbackInfoReturnable<Object> cir) {
        if (NtmCompatibility.enabled()) ((NtmEntityOwner) cir.getReturnValue()).nr$bind(entity);
    }

    @Inject(method = "getRadiation", at = @At("RETURN"), cancellable = true)
    private static void nr$readDose(LivingEntity entity, CallbackInfoReturnable<Double> cir) {
        if (!NtmCompatibility.enabled()) return;
        var properties = HbmLivingProps.getData(entity); // Also migrates saves with contamination disabled in NTM.
        cir.setReturnValue(entity.level().isClientSide() ? properties.getRads() : NtmCompatibility.dose(entity));
    }

    @Inject(method = "setRadiation", at = @At("HEAD"), cancellable = true)
    private static void nr$writeDose(LivingEntity entity, double amount, CallbackInfo ci) {
        if (!NtmCompatibility.enabled()) return;
        NtmEntityOwner properties = (NtmEntityOwner) HbmLivingProps.getData(entity);
        NtmCompatibility.setDose(entity, amount);
        if (!entity.level().isClientSide()) properties.nr$mirrorDose(NtmCompatibility.dose(entity));
        ci.cancel();
    }

    @Inject(method = "incrementRadiation", at = @At("HEAD"), cancellable = true)
    private static void nr$addDose(LivingEntity entity, double amount, CallbackInfo ci) {
        if (!NtmCompatibility.enabled()) return;
        NtmEntityOwner properties = (NtmEntityOwner) HbmLivingProps.getData(entity);
        NtmCompatibility.incrementDose(entity, amount);
        if (!entity.level().isClientSide()) properties.nr$mirrorDose(NtmCompatibility.dose(entity));
        ci.cancel();
    }

    @Inject(method = "getRadBuf", at = @At("RETURN"), cancellable = true)
    private static void nr$readRate(LivingEntity entity, CallbackInfoReturnable<Double> cir) {
        if (NtmCompatibility.enabled() && !entity.level().isClientSide()) {
            cir.setReturnValue(NtmCompatibility.exposureRate(entity));
        }
    }
}
