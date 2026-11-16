package igentuman.nr.compat.ntm.mixin;

import com.hbm.util.ContaminationUtil;
import igentuman.nr.compat.ntm.NtmCompatibility;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The drum is a WorldlyContainer, so NR's container sources already emit its stored item profiles.
 * Only the item-derived area exposure is retired; overflow releases, waste decay and activation hooks remain.
 */
@Pseudo
@Mixin(targets = "com.hbm.tileentity.machine.TileEntityStorageDrum", remap = false)
public abstract class StorageDrumMixin {
    @Redirect(method = "serverTick", at = @At(value = "INVOKE",
            target = "Lcom/hbm/util/ContaminationUtil;radiate(Lnet/minecraft/world/level/Level;DDDDF)V"))
    private static void nr$retireStoredItemExposure(Level level, double x, double y, double z, double range, float amount) {
        if (!NtmCompatibility.enabled()) ContaminationUtil.radiate(level, x, y, z, range, amount);
    }
}
