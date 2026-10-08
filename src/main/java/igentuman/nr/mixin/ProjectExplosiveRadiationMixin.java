package igentuman.nr.mixin;

import igentuman.nr.config.RadiationConfig;
import igentuman.nr.integration.projectexplosive.ProjectExplosiveIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "lil1llilol11.lo1i1iloioil.l1i1l11ii11o111oiilolil1", remap = false)
public abstract class ProjectExplosiveRadiationMixin {
    @Inject(method = "onWorldTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$stopWorldRadiationTick(LevelTickEvent.Post event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onServerTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$stopServerRadiationTick(ServerTickEvent.Post event, CallbackInfo ci) {
       ci.cancel();
    }

    @Inject(method = "onPlayerTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$stopPlayerRadiationTick(PlayerTickEvent.Post event, CallbackInfo ci) {
        ci.cancel();
    }

    // Old PE SavedData may exist in a world. Leave it dormant while NR owns radiation.
    @Inject(method = "onLevelLoad", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$skipLegacyRadiationLoad(LevelEvent.Load event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onLevelSave", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$skipLegacyRadiationSave(LevelEvent.Save event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onMobSpawnPositionCheck", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$skipLegacySpawnCheck(MobSpawnEvent.PositionCheck event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onLivingDeath", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$skipLegacyDeathDose(LivingDeathEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onPlayerGameModeChange", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$skipLegacyGameModeDose(PlayerEvent.PlayerChangeGameModeEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onPlayerLogout", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$skipLegacyLogoutDose(PlayerEvent.PlayerLoggedOutEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onPlayerLogin", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$skipLegacyLoginDose(PlayerEvent.PlayerLoggedInEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onPlayerClone", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$skipLegacyCloneDose(PlayerEvent.Clone event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onEntityJoin", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$skipLegacyEntityJoinDose(EntityJoinLevelEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onEntityLeave", at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$skipLegacyEntityLeaveDose(EntityLeaveLevelEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "ll1ol00o111oioool0l011lo(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;DI)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$registerFallout(Level level, BlockPos pos, double intensity, int duration, CallbackInfo ci) {
        ci.cancel();
        if (!RadiationConfig.masterEnabled()) return;
        if (level instanceof ServerLevel serverLevel) {
            ProjectExplosiveIntegration.addExplosionFallout(serverLevel, pos, intensity, duration);
        }
    }

    @Inject(method = "ll1ol00o111oioool0l011lo(Lnet/minecraft/server/level/ServerLevel;DDD)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void nr$registerZone(ServerLevel level, double x, double z, double radius, CallbackInfo ci) {
        ci.cancel();
        if (!RadiationConfig.masterEnabled()) return;
        ProjectExplosiveIntegration.addExplosionZone(level, x, z, radius);
    }
}
