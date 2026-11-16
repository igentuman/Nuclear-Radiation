package igentuman.nr.compat.ntm.mixin;

import com.hbm.capability.HbmLivingProps;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import dev.golem.core.world.Protection;
import igentuman.nr.api.shielding.ArmorProtectionRegistry;
import igentuman.nr.compat.ntm.NtmCompatibility;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.hbm.util.ContaminationUtil", remap = false)
public abstract class ContaminationUtilMixin {
    // Preserve the complete contamination method: Golem protection, immunity,
    // suit checks, bypass, events, Digamma and Space's neutron callback all stay.
    @Inject(method = "calculateRadiationMod", at = @At("RETURN"), cancellable = true)
    private static void nr$combinedProtection(LivingEntity entity, CallbackInfoReturnable<Double> cir) {
        if (NtmCompatibility.enabled() && cir.getReturnValue() > 0.0) {
            double armor = ArmorProtectionRegistry.summed(entity).xray();
            double medicine = Math.clamp(NtmCompatibility.data(entity).protectionFactor(), 0.0, 1.0);
            cir.setReturnValue((1.0 - armor) * (1.0 - medicine));
        }
    }

    @Redirect(method = "contaminate(Lnet/minecraft/world/entity/LivingEntity;Lcom/hbm/util/ContaminationUtil$HazardType;Lcom/hbm/util/ContaminationUtil$ContaminationType;DLdev/golem/core/world/Protection$Source;)Z",
            at = @At(value = "INVOKE", target = "Lcom/hbm/capability/HbmLivingProps;incrementRadiation(Lnet/minecraft/world/entity/LivingEntity;D)V"))
    private static void nr$recordAmbient(LivingEntity receiver, double absorbed,
                                         LivingEntity entity, HazardType hazard, ContaminationType route,
                                         double raw, Protection.Source source) {
        HbmLivingProps.incrementRadiation(receiver, absorbed);
        if (NtmCompatibility.enabled()) NtmCompatibility.recordAmbientExposure(entity, raw, absorbed);
    }
}
