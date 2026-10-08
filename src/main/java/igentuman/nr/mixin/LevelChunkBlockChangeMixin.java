package igentuman.nr.mixin;

import igentuman.nr.radiation.source.BlockSourceChangeProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkBlockChangeMixin {

    @Inject(method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"))
    private void nuclear_radiation$recordBlockChange(BlockPos pos, BlockState state, boolean isMoving,
                                                      CallbackInfoReturnable<BlockState> cir) {
        if (cir.getReturnValue() == null) return;
        LevelChunk chunk = (LevelChunk) (Object) this;
        if (chunk.getLevel() instanceof ServerLevel server) {
            BlockSourceChangeProcessor.get().markChanged(server, pos);
        }
    }
}
