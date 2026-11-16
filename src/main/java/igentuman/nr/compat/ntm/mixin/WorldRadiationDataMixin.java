package igentuman.nr.compat.ntm.mixin;

import igentuman.nr.compat.ntm.NtmCompatibility;
import igentuman.nr.compat.ntm.NtmLegacyWorldReader;
import igentuman.nr.compat.ntm.NtmNativeWorldOwner;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.hbm.handler.radiation.RadiationSystemNT$WorldRadiationData", remap = false)
public abstract class WorldRadiationDataMixin implements NtmNativeWorldOwner {
    @Shadow @Final ServerLevel level;

    @Override public ServerLevel nr$level() { return level; }

    @Inject(method = "migrateSavedData", at = @At("HEAD"), cancellable = true)
    private void nr$retainBeforeDestructiveDrain(CallbackInfo ci) {
        if (!NtmCompatibility.enabled()) return;
        NtmLegacyWorldReader.beforeSavedDataDrain(level);
        // Keep old rows as a frozen import source. A crash between drainTo and a
        // sidecar/chunk save must not turn a recoverable old release into an empty import.
        ci.cancel();
    }

    @Inject(method = "persist", at = @At("HEAD"), cancellable = true)
    private void nr$freezeNativeStorage(CallbackInfo ci) {
        if (NtmCompatibility.enabled()) ci.cancel();
    }

    @Inject(method = "prepare", at = @At("HEAD"), cancellable = true)
    private void nr$freezeNativePreparation(CallbackInfo ci) {
        if (NtmCompatibility.enabled()) ci.cancel();
    }

    @Inject(method = "runParallelSimulation", at = @At("HEAD"), cancellable = true)
    private void nr$freezeNativeWorkers(long epoch, CallbackInfoReturnable<Boolean> cir) {
        if (NtmCompatibility.enabled()) cir.setReturnValue(true);
    }
}
