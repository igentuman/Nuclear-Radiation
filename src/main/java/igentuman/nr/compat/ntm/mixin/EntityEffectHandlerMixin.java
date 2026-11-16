package igentuman.nr.compat.ntm.mixin;

import com.hbm.capability.HbmLivingProps;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.util.ContaminationUtil;
import igentuman.nr.compat.ntm.NtmCompatibility;
import igentuman.nr.compat.ntm.NtmEntityOwner;
import igentuman.nr.config.NtmCompatConfig;
import igentuman.nr.radiation.simulation.RadiationFieldSampler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.hbm.handler.EntityEffectHandler", remap = false)
public abstract class EntityEffectHandlerMixin {
    // NR samples the migrated volume once. Native environment floors remain until
    // their providers are ported, contributing only the excess above that volume.
    @Redirect(method = "handleRadiation", at = @At(value = "INVOKE", target =
            "Lcom/hbm/handler/radiation/ChunkRadiationManager$ProxyClass;getRadiation(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)D"))
    private static double nr$retireWorldIntake(ChunkRadiationManager.ProxyClass proxy, Level level, BlockPos pos) {
        return NtmCompatibility.enabled() ? 0.0 : proxy.getRadiation(level, pos);
    }

    @Redirect(method = "handleRadiation", at = @At(value = "INVOKE", target =
            "Lcom/hbm/util/ContaminationUtil;contaminate(Lnet/minecraft/world/entity/LivingEntity;Lcom/hbm/util/ContaminationUtil$HazardType;Lcom/hbm/util/ContaminationUtil$ContaminationType;D)Z"))
    private static boolean nr$remainingEnvironmentFloor(LivingEntity entity, ContaminationUtil.HazardType hazard,
                                                       ContaminationUtil.ContaminationType type, double amount) {
        if (NtmCompatibility.enabled() && entity.level() instanceof ServerLevel level) {
            double volumeSv = RadiationFieldSampler.profile(NtmCompatibility.worldProfile(level, entity.blockPosition())).total();
            amount = Math.max(0.0, amount - volumeSv / (20.0 * NtmCompatConfig.SV_PER_EXPOSURE_UNIT.get()));
        }
        return amount > 0.0 && ContaminationUtil.contaminate(entity, hazard, type, amount);
    }

    // Keep the native floor calculation above, and stop only NTM symptoms here.
    @Inject(method = "handleRadiation", at = @At(value = "NEW", target = "java/util/Random"), cancellable = true)
    private static void nr$stopNativeSymptoms(LivingEntity entity, CallbackInfo ci) {
        if (NtmCompatibility.enabled()) ci.cancel();
    }

    // This call occurs AFTER the complete data-map transformation branch and its
    // fallback damage. Never cancel onUpdate or the transformation branch itself.
    @Inject(method = "handleRadiationEffect", at = @At(value = "INVOKE",
            target = "Lcom/hbm/util/ContaminationUtil;isRadImmune(Lnet/minecraft/world/entity/Entity;)Z"), cancellable = true)
    private static void nr$stopNativeSickness(LivingEntity entity, CallbackInfo ci) {
        if (NtmCompatibility.enabled()) ci.cancel();
    }

    @Inject(method = "syncToPlayer", at = @At("HEAD"))
    private static void nr$syncMirror(ServerPlayer player, CallbackInfo ci) {
        if (!NtmCompatibility.enabled()) return;
        NtmEntityOwner properties = (NtmEntityOwner) HbmLivingProps.getData(player);
        properties.nr$mirrorDose(NtmCompatibility.dose(player));
        properties.nr$mirrorRate(NtmCompatibility.exposureRate(player));
    }
}
