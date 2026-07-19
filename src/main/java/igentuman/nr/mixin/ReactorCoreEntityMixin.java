package igentuman.nr.mixin;

import igentuman.nr.api.RadiationProfile;
import igentuman.nr.integration.createnucleartech.CreateNTHelper;
import igentuman.nr.client.particle.MeltdownParticles;
import igentuman.nr.radiation.source.LeftOverRadSource;
import igentuman.nr.radiation.source.WorldSourceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.nuclearteam.createnuclear.content.multiblock.core.ReactorCoreEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ReactorCoreEntity.class, remap = false)
public class ReactorCoreEntityMixin {

    @Inject(method = "explodeReactorCore", at = @At("HEAD"), cancellable = true, remap = false)
    public void explodeReactorCoreMixin(Level world, BlockPos pos, CallbackInfo ci) {
        ci.cancel();
        world.explode((Entity)null, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), 20.0F, Level.ExplosionInteraction.BLOCK);

        if (world instanceof ServerLevel server) {
            MeltdownParticles.emit(server, pos);
            long now = server.getGameTime();
            RadiationProfile profile = CreateNTHelper.buildFalloutProfile(20.0, 2, now);
            WorldSourceRegistry.get(server).register(new LeftOverRadSource(server, pos.immutable(), profile, now, true));
        }
    }
}
