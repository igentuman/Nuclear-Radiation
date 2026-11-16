package igentuman.nr.compat.ntm.mixin;

import com.hbm.capability.HbmLivingProps;
import igentuman.nr.compat.ntm.NtmCompatibility;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.hbm.main.ModEventHandlerMobs", remap = false)
public abstract class ModEventHandlerMobsMixin {
    @Redirect(method = "onEntityDeath", at = @At(value = "INVOKE",
            target = "Lcom/hbm/capability/HbmLivingProps;setRadiation(Lnet/minecraft/world/entity/LivingEntity;D)V"))
    private static void nr$keepRespawnDose(LivingEntity entity, double value) {
        // NR copies dose on death and halves it on respawn. Preserve that rule;
        // retain every other native death handler and revive side effect.
        if (!NtmCompatibility.enabled()) HbmLivingProps.setRadiation(entity, value);
    }
}
