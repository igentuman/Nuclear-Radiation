package igentuman.nr.mixin;

import igentuman.nr.particle.MeltdownParticles;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FissionReactorMultiblockData.class, remap = false)
public abstract class FissionReactorMultiblockDataMixin {

    @Inject(method = "createMeltdown", at = @At("TAIL"), remap = false)
    public void afterCreateMeltdown(Level world, CallbackInfo ci)
    {
        if (world instanceof ServerLevel sl) {
            MultiblockData self = (MultiblockData) (Object) this;
            BlockPos min = self.getMinPos();
            BlockPos max = self.getMaxPos();
            BlockPos center = new BlockPos(
                (min.getX() + max.getX()) / 2,
                (min.getY() + max.getY()) / 2,
                (min.getZ() + max.getZ()) / 2
            );
            MeltdownParticles.emit(sl, center);
        }
    }
}
