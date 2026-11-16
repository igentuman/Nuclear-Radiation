package igentuman.nr.compat.ntm.mixin;

import com.hbm.handler.radiation.ChunkRadiationManager;
import igentuman.nr.compat.ntm.NtmCompatibility;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Placed hazard blocks become NR block sources (explicit NR binding, else the imported NTM level).
 * The per-20-tick world release then must not also run. Native {@code rad3d} area exposure is not part
 * of the imported profile and keeps running (it enters NR dose once through contaminate). Without area
 * exposure the tick only emits and reschedules itself, so it is cancelled outright.
 */
@Pseudo
@Mixin(targets = "com.hbm.blocks.generic.BlockHazard", remap = false)
public abstract class BlockHazardMixin {
    private static final String TICK =
            "tick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V";

    @Inject(method = TICK, at = @At("HEAD"), cancellable = true)
    private void nr$nrOwnsEmission(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
                                   CallbackInfo ci) {
        if (NtmCompatibility.enabled() && NtmCompatibility.nrOwnsBlockEmission(state)
                && !(NtmCompatibility.nativeBlockAreaRadiation(state) > 0.0F)) {
            ci.cancel();
        }
    }

    @Redirect(method = TICK, at = @At(value = "INVOKE", target =
            "Lcom/hbm/handler/radiation/ChunkRadiationManager$ProxyClass;incrementRad(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;D)V"))
    private void nr$skipWorldRelease(ChunkRadiationManager.ProxyClass proxy, Level level, BlockPos pos, double amount) {
        if (NtmCompatibility.enabled() && NtmCompatibility.nrOwnsBlockEmission(level.getBlockState(pos))) return;
        proxy.incrementRad(level, pos, amount);
    }
}
