package igentuman.nr.mixin;

import igentuman.nr.integration.createnuclear.CreateNuclearHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.nuclearteam.createnuclear.content.multiblock.controller.service.ReactorMeltdownExecutor;
import net.nuclearteam.createnuclear.infrastructure.worldgen.biome.BiomeIrradiationService;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ReactorMeltdownExecutor.class, remap = false)
public class CreateNuclearMeltdownMixin {
    @Redirect(method = "triggerExplosion", at = @At(value = "INVOKE",
            target = "Lnet/nuclearteam/createnuclear/infrastructure/worldgen/biome/BiomeIrradiationService;circularArea(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/resources/ResourceKey;I)V"))
    private void nr$addFalloutToIrradiatedBiome(ServerLevel level, BlockPos globalPos,
                                            ResourceKey<Biome> biome, int radius,
                                            ServerLevel reactorLevel, BlockPos controllerPos,
                                            @Nullable BoundingBox bounds, int reactorSize,
                                            int countFuelRod, int notifyRadius, boolean notifyWarnAll) {
        // Retain the irradiated biome and its visuals. NR's biome background
        // config supplies the dose because Create Nuclear's radiation tick is suppressed.
        BiomeIrradiationService.circularArea(level, globalPos, biome, radius);
        // Use the reactor's local position for sources in Sable sub-levels.
        CreateNuclearHelper.registerMeltdownFallout(reactorLevel, controllerPos.above(5), radius);
    }
}
