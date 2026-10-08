package igentuman.nr.mixin.client;

import igentuman.nr.config.RadiationConfig;
import igentuman.nr.network.ClientRadiationCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mixin(targets = "lil1llilol11.lo1i1iloioil.llo10oo01ol0i00lil1iilil", remap = false)
public abstract class ProjectExplosiveGeigerMixin {
    @Shadow private static float lllo0i1lil0lioi010lil111;
    @Shadow private static float l1i0o00l00i1iioiiilll000;
    @Shadow private static int lii1l0lo11lloioi1io11lio;

    @Inject(method = "onClientTick", at = @At("HEAD"), remap = false)
    private static void nr$updateGeigerFromNR(ClientTickEvent.Post event, CallbackInfo ci) {
        nr$readNRDose();
    }

    @Inject(method = "ll1ol00o111oioool0l011lo(FF)V", at = @At("TAIL"), remap = false)
    private static void nr$syncGeiger(float rate, float dose, CallbackInfo ci) {
        nr$readNRDose();
    }

    private static void nr$readNRDose() {
        lllo0i1lil0lioi010lil111 = (float) Math.min(Float.MAX_VALUE, ClientRadiationCache.svPerHourAmbient());
        l1i0o00l00i1iioiiilll000 = (float) Math.min(50.0, ClientRadiationCache.svTotal());
        lii1l0lo11lloioi1io11lio = 0;
    }
}
