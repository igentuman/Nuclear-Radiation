package igentuman.nr.compat.megaton.mixin;

import igentuman.nr.compat.megaton.MegatonRadiationBridge;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Prevents MEGATON's second sickness system while retaining its blast effects. */
@Mixin(targets = "com.nuclearcraft.system.RadiationEvents", remap = false)
public abstract class MegatonRadiationMixin {
    @Inject(method = "onLivingTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$useNuclearRadiationTick(LivingEntity entity, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "dose", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$bridgeDirectDose(LivingEntity entity, float amount, CallbackInfo ci) {
        MegatonRadiationBridge.directDose(entity, amount);
        ci.cancel();
    }

    @Inject(method = "rads", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$showNuclearRadiationDose(LivingEntity entity, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(MegatonRadiationBridge.displayedRads(entity));
    }
}
