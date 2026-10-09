package igentuman.nr.mixin;

import igentuman.nr.config.RadiationConfig;
import igentuman.nr.integration.thewasteland.TheWastelandGeiger;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.mcreator.thewastelandreworked.procedures.GeigerCounterSoundsProcedure", remap = false)
public abstract class TheWastelandGeigerSoundMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$clickForDoseRate(LevelAccessor level, double x, double y, double z,
                                            Entity entity, ItemStack stack, CallbackInfo ci) {
        if (!RadiationConfig.masterEnabled()) return;
        ci.cancel();
        TheWastelandGeiger.click(entity, stack);
    }
}
