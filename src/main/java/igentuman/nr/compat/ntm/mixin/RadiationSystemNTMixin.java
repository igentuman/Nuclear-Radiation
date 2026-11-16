package igentuman.nr.compat.ntm.mixin;

import com.hbm.handler.radiation.RadiationSystemNT;
import igentuman.nr.compat.ntm.NtmCompatibility;
import igentuman.nr.compat.ntm.NtmWorldBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.hbm.handler.radiation.RadiationSystemNT", remap = false)
public abstract class RadiationSystemNTMixin {
    @Inject(method = "getRadForCoord", at = @At("HEAD"), cancellable = true)
    private static void nr$read(ServerLevel level, BlockPos pos, CallbackInfoReturnable<Double> cir) {
        if (NtmCompatibility.enabled()) cir.setReturnValue(NtmWorldBridge.reading(level, pos));
    }

    @Inject(method = "setRadForCoord", at = @At("HEAD"), cancellable = true)
    private static void nr$set(ServerLevel level, BlockPos pos, double amount, CallbackInfo ci) {
        if (!NtmCompatibility.enabled()) return;
        NtmWorldBridge.set(level, pos, amount);
        ci.cancel();
    }

    @Inject(method = "incrementRad", at = @At("HEAD"), cancellable = true)
    private static void nr$increment(ServerLevel level, BlockPos pos, double amount, double cap, CallbackInfo ci) {
        if (!NtmCompatibility.enabled()) return;
        NtmWorldBridge.increment(level, pos, amount, cap);
        ci.cancel();
    }

    @Inject(method = "decrementRad", at = @At("HEAD"), cancellable = true)
    private static void nr$decrement(ServerLevel level, BlockPos pos, double amount, CallbackInfo ci) {
        if (!NtmCompatibility.enabled()) return;
        NtmWorldBridge.increment(level, pos, -amount, Double.MAX_VALUE);
        ci.cancel();
    }

    @Inject(method = "clearWorld", at = @At("HEAD"), cancellable = true)
    private static void nr$clear(ServerLevel level, CallbackInfo ci) {
        if (!NtmCompatibility.enabled()) return;
        NtmWorldBridge.clear(level);
        ci.cancel();
    }

    @Inject(method = "forEachChunkMax", at = @At("HEAD"), cancellable = true)
    private static void nr$chunkMax(ServerLevel level, RadiationSystemNT.ChunkMaxConsumer consumer, CallbackInfo ci) {
        if (!NtmCompatibility.enabled()) return;
        NtmWorldBridge.forEachChunkMax(level, consumer);
        ci.cancel();
    }

    @Inject(method = "onChunkLoaded", at = @At("RETURN"))
    private static void nr$track(ServerLevel level, LevelChunk chunk, CallbackInfo ci) {
        if (NtmCompatibility.enabled()) NtmWorldBridge.loaded(level, chunk);
    }

    @Inject(method = "onChunkUnloaded", at = @At("HEAD"))
    private static void nr$forget(ServerLevel level, LevelChunk chunk, CallbackInfo ci) {
        if (NtmCompatibility.enabled()) NtmWorldBridge.unloaded(level, chunk);
    }

    // Native ticks/config gates and world-effect cadence remain intact. Never reach native prepare/workers.
    @Inject(method = "tickServer", at = @At(value = "INVOKE", target =
            "Lcom/hbm/handler/radiation/RadiationSystemNT$WorldRadiationData;prepare()V"), cancellable = true)
    private static void nr$handoffSimulation(MinecraftServer server, CallbackInfo ci) {
        if (!NtmCompatibility.enabled()) return;
        NtmWorldBridge.queueWorldEffects(server);
        ci.cancel();
    }
}
