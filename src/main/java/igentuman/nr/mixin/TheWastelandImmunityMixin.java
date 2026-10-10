package igentuman.nr.mixin;

import igentuman.nr.integration.thewasteland.TheWastelandMedicine;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.mcreator.thewastelandreworked.procedures.RadiationImmunityApplyProcedure", remap = false)
public abstract class TheWastelandImmunityMixin {
    @Inject(method = "execute", at = @At("HEAD"), remap = false)
    private static void nr$protect(Entity entity, CallbackInfo ci) {
        TheWastelandMedicine.protect(entity, 5);
    }
}
