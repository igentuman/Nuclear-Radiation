package igentuman.nr.mixin;

import com.nred.nuclearcraft.block_entity.fission.PebbleFissionChamberEntity;
import com.nred.nuclearcraft.block_entity.fission.SaltFissionVesselEntity;
import com.nred.nuclearcraft.block_entity.fission.SolidFissionCellEntity;
import com.nred.nuclearcraft.capability.radiation.source.IRadiationSource;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.integration.nuclearcraft.NCNHelper;
import igentuman.nr.util.tracking.LeftOverRadSource;
import igentuman.nr.util.tracking.WorldSourceRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {SolidFissionCellEntity.class, PebbleFissionChamberEntity.class, SaltFissionVesselEntity.class}, remap = false)
public abstract class NCMeltdownMixin {

    @Redirect(
            method = "onClusterMeltdown",
            at = @At(value = "INVOKE",
                    target = "Lcom/nred/nuclearcraft/radiation/RadiationHelper;addToSourceRadiation(Lcom/nred/nuclearcraft/capability/radiation/source/IRadiationSource;D)V"),
            remap = false)
    private void nr$captureMeltdown(IRadiationSource source, double magnitude) {
        // Not delegating to addToSourceRadiation: NCN chunk-buffer write is suppressed here.
        BlockEntity self = (BlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            long gameTime = serverLevel.getGameTime();
            RadiationProfile profile = NCNHelper.buildMeltdownProfile(magnitude, gameTime);
            WorldSourceRegistry.get(serverLevel).register(
                    new LeftOverRadSource(serverLevel, self.getBlockPos(), profile, gameTime, true));
        }
    }
}
