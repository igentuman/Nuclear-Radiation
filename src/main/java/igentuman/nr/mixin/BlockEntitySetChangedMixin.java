package igentuman.nr.mixin;

import igentuman.nr.containers.ContainerRadiationTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntitySetChangedMixin {

    @Inject(method = "setChanged()V", at = @At("TAIL"))
    private void nuclear_radiation$markRadiationDirty(CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (!(self instanceof BaseContainerBlockEntity)) return;
        Level level = self.getLevel();
        if (!(level instanceof ServerLevel server)) return;
        BlockPos pos = self.getBlockPos();
        if (pos == null) return;
        ContainerRadiationTicker.markDirty(server, pos);
    }
}
