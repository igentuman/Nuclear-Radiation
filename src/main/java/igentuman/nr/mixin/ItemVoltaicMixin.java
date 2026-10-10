package igentuman.nr.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import voltaic.common.item.ItemVoltaic;

@Mixin(value = ItemVoltaic.class, remap = false)
public abstract class ItemVoltaicMixin {

    @Inject(method = "onEntityItemUpdate", at = @At("HEAD"), cancellable = true, remap = false)
    public void nr$skipDropRadiation(ItemStack stack, ItemEntity entity, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
