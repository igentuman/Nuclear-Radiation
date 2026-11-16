package igentuman.nr.compat.ntm.mixin;

import com.hbm.hazard.HazardSystem;
import igentuman.nr.compat.ntm.NtmCompatibility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Block contact has no NR equivalent: keep its native item-hazard radiation (bridged into NR dose once). */
@Pseudo
@Mixin(targets = "com.hbm.blocks.generic.BlockHazard", remap = false)
public abstract class NativeItemContactMixin {
    @Redirect(method = {"stepOn", "onEntityInside"}, at = @At(value = "INVOKE", target =
            "Lcom/hbm/hazard/HazardSystem;applyHazards(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void nr$retainContact(Block block, LivingEntity entity) {
        NtmCompatibility.retainNativeItemRadiation(() -> HazardSystem.applyHazards(block, entity));
    }
}
