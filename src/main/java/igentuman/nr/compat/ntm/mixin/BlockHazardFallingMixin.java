package igentuman.nr.compat.ntm.mixin;

import com.hbm.handler.radiation.ChunkRadiationManager;
import igentuman.nr.compat.ntm.NtmCompatibility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** NR owns the emission as for BlockHazard; the falling logic in this tick must keep running. */
@Pseudo
@Mixin(targets = "com.hbm.blocks.generic.BlockHazardFalling", remap = false)
public abstract class BlockHazardFallingMixin {
    @Redirect(method = "tick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V",
            at = @At(value = "INVOKE", target =
                    "Lcom/hbm/handler/radiation/ChunkRadiationManager$ProxyClass;incrementRad(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;D)V"))
    private void nr$explicitBindingWins(ChunkRadiationManager.ProxyClass proxy, Level level, BlockPos pos, double amount) {
        if (NtmCompatibility.enabled() && NtmCompatibility.nrOwnsBlockEmission(level.getBlockState(pos))) return;
        proxy.incrementRad(level, pos, amount);
    }
}
