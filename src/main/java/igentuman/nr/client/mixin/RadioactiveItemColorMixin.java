package igentuman.nr.client.mixin;

import igentuman.nr.client.IonizationGlowRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Tags radioactive item silhouettes for the ionization glow post effect. */
@Mixin(Entity.class)
public abstract class RadioactiveItemColorMixin {
    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void nr$glowMaskColor(CallbackInfoReturnable<Integer> cir) {
        if ((Object)this instanceof ItemEntity item && IonizationGlowRenderer.shouldGlow(item)) {
            cir.setReturnValue(IonizationGlowRenderer.MASK_COLOR);
        }
    }
}
