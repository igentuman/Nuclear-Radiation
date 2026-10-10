package igentuman.nr.mixin;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(targets = "lil1llilol11.lo1i1iloioil.l01lil10iil0oi1i11lo0lol", remap = false)
public abstract class ProjectExplosiveEnvironmentMixin {
    @Inject(method = "onWorldTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$stopContaminationTick(LevelTickEvent.Post event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$stopContaminationExposure(EntityTickEvent.Post event, CallbackInfo ci) {
        ci.cancel();
    }

    @Redirect(method = "ll1ol00o111oioool0l011lo(Lnet/minecraft/core/BlockPos;Lnet/minecraft/server/level/ServerLevel;D)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
            remap = false)
    private static Object nr$skipLegacyContamination(Map<Object, Object> map, Object pos, Object source) {
        return null;
    }
}
