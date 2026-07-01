package igentuman.nr.mixin;

import igentuman.nr.api.RadiationProfile;
import igentuman.nr.integration.mekanism.MekanismHelper;
import igentuman.nr.util.tracking.LeftOverRadSource;
import igentuman.nr.util.tracking.WorldSourceRegistry;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = RadiationManager.class, remap = false)
public abstract class MekRadiationManagerMixin {

    public boolean isMekRadiationEnabled()
    {
        return MekanismConfig.general.radiationEnabled.getOrDefault();
    }

    @Inject(method = "radiate(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;D)V", at = @At("HEAD"), remap=false, cancellable = true)
    public void radiate(Level level, BlockPos pos, double magnitude, CallbackInfo callback) {
        callback.cancel();
        if (magnitude < 0) return;
        if(level instanceof ServerLevel serverLevel) {
            long gameTime = level.getGameTime();
            RadiationProfile profile = MekanismHelper.buildProfile(magnitude, gameTime);
            WorldSourceRegistry.get(serverLevel).register(new LeftOverRadSource(level, pos, profile, gameTime, true));
        }
    }
}
