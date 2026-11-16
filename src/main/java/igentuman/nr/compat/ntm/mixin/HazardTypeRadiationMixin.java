package igentuman.nr.compat.ntm.mixin;

import igentuman.nr.compat.ntm.NtmCompatibility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Item radiation is resolved by NR's inventory, container and dropped-item sources. Only the RADIATION
 * type is retired: other hazard types and the native-retained contact/armor-mod paths still run.
 */
@Pseudo
@Mixin(targets = "com.hbm.hazard.type.HazardTypeRadiation", remap = false)
public abstract class HazardTypeRadiationMixin {
    @Inject(method = "onUpdate(Lnet/minecraft/world/entity/LivingEntity;DLnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"), cancellable = true)
    private void nr$retireCarried(LivingEntity entity, double level, ItemStack stack, CallbackInfo ci) {
        if (NtmCompatibility.enabled() && !NtmCompatibility.nativeItemRadiationRetained()) ci.cancel();
    }

    @Inject(method = "addHazardInformation(Lnet/minecraft/world/entity/player/Player;Ljava/util/List;DLnet/minecraft/world/item/ItemStack;Ljava/util/List;)V",
            at = @At("HEAD"), cancellable = true)
    private void nr$retireTooltip(Player player, List<?> lines, double level, ItemStack stack, List<?> modifiers,
                                  CallbackInfo ci) {
        if (NtmCompatibility.enabled()) ci.cancel();
    }
}
