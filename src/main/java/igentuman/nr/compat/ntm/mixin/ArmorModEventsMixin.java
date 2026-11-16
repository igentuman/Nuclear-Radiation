package igentuman.nr.compat.ntm.mixin;

import com.hbm.hazard.HazardSystem;
import igentuman.nr.compat.ntm.NtmCompatibility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** NR does not see mods inserted into armor: keep their native radiation (bridged into NR dose once). */
@Pseudo
@Mixin(targets = "com.hbm.handler.ArmorModEvents", remap = false)
public abstract class ArmorModEventsMixin {
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target =
            "Lcom/hbm/hazard/HazardSystem;applyHazards(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private static void nr$retainArmorMods(ItemStack mod, LivingEntity entity) {
        NtmCompatibility.retainNativeItemRadiation(() -> HazardSystem.applyHazards(mod, entity));
    }
}
