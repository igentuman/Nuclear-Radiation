package igentuman.nr.mixin;

import cattodream.createnucleartech.radiation.RadiationData;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.integration.createnucleartech.CreateNTHelper;
import igentuman.nr.radiation.source.LeftOverRadSource;
import igentuman.nr.radiation.source.WorldSourceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RadiationData.class, remap = false)
public class CreateNTRadiationDataMixin {

    // RadiationData is a per-dimension SavedData; its mutators receive no Level.
    // Every write is preceded by RadiationData.get(level) on the same thread,
    // so stash the level here and read it back in the mutators below.
    @Unique
    private static final ThreadLocal<ServerLevel> nr$currentLevel = new ThreadLocal<>();

    @Inject(method = "get", at = @At("HEAD"), remap = false)
    private static void nr$captureLevel(ServerLevel level, CallbackInfoReturnable<RadiationData> cir) {
        nr$currentLevel.set(level);
    }

    @Inject(method = "addRadialFallout", at = @At("HEAD"), cancellable = true, remap = false)
    public void nr$captureFallout(BlockPos center, int radiusChunks, double centralStrength, CallbackInfo ci) {
        ci.cancel();
        ServerLevel level = nr$currentLevel.get();
        if (level == null || centralStrength <= 0) return;
        long gameTime = level.getGameTime();
        RadiationProfile profile = CreateNTHelper.buildFalloutProfile(centralStrength, radiusChunks, gameTime);
        WorldSourceRegistry.get(level).register(new LeftOverRadSource(level, center, profile, gameTime, true));
    }

    @Inject(method = "addRadiation(Lnet/minecraft/core/BlockPos;D)V", at = @At("HEAD"), cancellable = true, remap = false)
    public void nr$captureBlockRadiation(BlockPos pos, double strength, CallbackInfo ci) {
        ci.cancel();
        ServerLevel level = nr$currentLevel.get();
        if (level == null || strength <= 0) return;
        long gameTime = level.getGameTime();
        RadiationProfile profile = CreateNTHelper.buildLeakProfile(strength, gameTime);
        WorldSourceRegistry.get(level).register(new LeftOverRadSource(level, pos, profile, gameTime, true));
    }

    @Inject(method = "addRadiation(Lnet/minecraft/world/level/ChunkPos;D)V", at = @At("HEAD"), cancellable = true, remap = false)
    public void nr$captureChunkRadiation(ChunkPos pos, double strength, CallbackInfo ci) {
        ci.cancel();
        ServerLevel level = nr$currentLevel.get();
        if (level == null || strength <= 0) return;
        long gameTime = level.getGameTime();
        BlockPos center = new BlockPos(pos.getMinBlockX() + 8, level.getSeaLevel(), pos.getMinBlockZ() + 8);
        RadiationProfile profile = CreateNTHelper.buildLeakProfile(strength, gameTime);
        WorldSourceRegistry.get(level).register(new LeftOverRadSource(level, center, profile, gameTime, true));
    }
}
