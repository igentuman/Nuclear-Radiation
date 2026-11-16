package igentuman.nr.core.mixin;

import igentuman.nr.radiation.source.BlockSourceChangeProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Feeds every committed server block change into {@link BlockSourceChangeProcessor}, so placed/broken
 * radioactive blocks, fluids and containers (NR bindings and imported NTM hazard blocks alike) register
 * without waiting for a chunk reload. A null return means nothing changed (or a nested write superseded
 * this one and is reported by its own call).
 */
@Mixin(LevelChunk.class)
public abstract class LevelChunkBlockChangeMixin {

    @Inject(method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"))
    private void nuclear_radiation$recordBlockChange(BlockPos pos, BlockState state, int flags,
                                                      CallbackInfoReturnable<BlockState> cir) {
        if (cir.getReturnValue() == null) return;
        if (((LevelChunk) (Object) this).getLevel() instanceof ServerLevel server) {
            BlockSourceChangeProcessor.get().markChanged(server, pos);
        }
    }
}
