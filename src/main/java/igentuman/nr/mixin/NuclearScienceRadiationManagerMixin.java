package igentuman.nr.mixin;

import igentuman.nr.integration.nuclear_science.NuclearScienceHelper;
import igentuman.nr.radiation.source.LeftOverRadSource;
import igentuman.nr.radiation.source.WorldSourceRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import voltaic.api.radiation.RadiationManager;
import voltaic.api.radiation.SimpleRadiationSource;

@Mixin(value = RadiationManager.class, remap = false)
public abstract class NuclearScienceRadiationManagerMixin {

    @Inject(method = "addRadiationSource", at = @At("HEAD"), remap = false, cancellable = true)
    public void addRadiationSourceMixin(SimpleRadiationSource source, Level world, CallbackInfo callback) {
        callback.cancel();
        if(source.amount() < 1 || source.strength() < 1) return;
        WorldSourceRegistry.get((ServerLevel) world).register(new LeftOverRadSource(world, source.location(), NuclearScienceHelper.buildProfile(source, world.getGameTime()), world.getGameTime(), true));
    }
}
