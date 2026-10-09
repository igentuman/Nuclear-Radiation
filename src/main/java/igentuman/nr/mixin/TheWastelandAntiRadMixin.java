package igentuman.nr.mixin;

import igentuman.nr.integration.thewasteland.TheWastelandMedicine;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.mcreator.thewastelandreworked.procedures.AntiRadEffectProcedure", remap = false)
public abstract class TheWastelandAntiRadMixin {
    @Inject(method = "execute", at = @At("HEAD"), remap = false)
    private static void nr$removeDose(Entity entity, CallbackInfo ci) {
        TheWastelandMedicine.removeDose(entity, 1.0);
    }
}
