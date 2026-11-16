package igentuman.nr.compat.ntm.mixin;

import com.hbm.handler.HazmatRegistry;
import igentuman.nr.compat.ntm.NtmCompatibility;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.hbm.hazard.HazardSystem", remap = false)
public abstract class HazardSystemMixin {
    // NR's armor tooltip already shows NTM resistance/cladding converted into NR protection.
    // Only the radiation-resistance line is skipped; damage/protection-class lines remain.
    @Redirect(method = "addHazardInfo(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Ljava/util/List;)V",
            at = @At(value = "INVOKE", target = "Lcom/hbm/handler/HazmatRegistry;getResistance(Lnet/minecraft/world/item/ItemStack;)D"))
    private static double nr$retireResistanceLine(ItemStack stack) {
        return NtmCompatibility.enabled() ? 0.0 : HazmatRegistry.getResistance(stack);
    }
}
