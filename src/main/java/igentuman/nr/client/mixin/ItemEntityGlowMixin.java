package igentuman.nr.client.mixin;

import igentuman.nr.client.IonizationGlowRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds client-only radioactive item outlines without changing synced entity flags. */
@Mixin(Minecraft.class)
public abstract class ItemEntityGlowMixin {
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void nr$glowRadioactiveItem(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ItemEntity item && IonizationGlowRenderer.shouldGlow(item)) {
            cir.setReturnValue(true);
        }
    }
}
