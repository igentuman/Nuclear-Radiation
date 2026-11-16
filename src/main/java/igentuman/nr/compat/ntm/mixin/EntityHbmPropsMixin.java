package igentuman.nr.compat.ntm.mixin;

import igentuman.nr.compat.ntm.NtmCompatibility;
import igentuman.nr.compat.ntm.NtmEntityOwner;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.hbm.entity.EntityHbmProps", remap = false)
public abstract class EntityHbmPropsMixin implements NtmEntityOwner {
    @Shadow private double rads;
    @Shadow private double envRads;
    @Shadow private double radBuf;
    @Unique private LivingEntity nr$owner;

    @Override
    public void nr$bind(LivingEntity entity) {
        nr$owner = entity;
        if (!entity.level().isClientSide()) {
            NtmCompatibility.migrate(entity, rads);
            rads = NtmCompatibility.dose(entity);
        }
    }

    @Override public void nr$mirrorDose(double dose) { rads = dose; }
    @Override public void nr$mirrorRate(double rate) { envRads = rate; radBuf = rate; }

    @Inject(method = "getRads", at = @At("HEAD"), cancellable = true)
    private void nr$readRawDose(CallbackInfoReturnable<Double> cir) {
        if (nr$owner != null && !nr$owner.level().isClientSide() && NtmCompatibility.enabled()) {
            cir.setReturnValue(NtmCompatibility.dose(nr$owner));
        }
    }

    @Inject(method = "setRads", at = @At("HEAD"), cancellable = true)
    private void nr$writeRawDose(double dose, CallbackInfo ci) {
        if (nr$owner != null && !nr$owner.level().isClientSide() && NtmCompatibility.enabled()) {
            NtmCompatibility.setDose(nr$owner, dose);
            rads = NtmCompatibility.dose(nr$owner);
            ci.cancel();
        }
    }

    @Inject(method = {"save", "serialize"}, at = @At("HEAD"))
    private void nr$refreshStoredMirror(CallbackInfo ci) {
        if (nr$owner != null && !nr$owner.level().isClientSide() && NtmCompatibility.enabled()) {
            rads = NtmCompatibility.dose(nr$owner);
            if (nr$owner instanceof net.minecraft.server.level.ServerPlayer) {
                nr$mirrorRate(NtmCompatibility.exposureRate(nr$owner));
            }
        }
    }
}
