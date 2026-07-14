package igentuman.nr.mixin;

import com.rae.crowns.content.nuclear.NuclearExplosion;
import com.rae.crowns.content.nuclear.fuel_assembly.AssemblyBlockEntity;
import igentuman.nr.corium.Corium;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AssemblyBlockEntity.class)
public class AssemblyBlockEntityMixin {

    @Inject(method = "standardExplosion", at = @At("HEAD"), cancellable = true, remap = false)
    public void standardExplosionMixin(BlockPos pos, float power, CallbackInfo ci) {
        ci.cancel();
        Level level = ((BlockEntity) (Object) this).getLevel();
        if (level != null) {
            NuclearExplosion.nuclearExplosion(level, pos, power);
            level.setBlockAndUpdate(pos, Corium.MOLTEN_CORIUM_BLOCK.get().defaultBlockState());
        }
    }

    @Inject(method = "meltdown", at = @At("HEAD"), cancellable = true, remap = false)
    private void meltdownMixin(BlockPos pos, CallbackInfo ci) {
        ci.cancel();
        Level level = ((BlockEntity) (Object) this).getLevel();
        if (level != null) {
            level.setBlockAndUpdate(pos, Corium.MOLTEN_CORIUM_BLOCK.get().defaultBlockState());
        }
    }
}
