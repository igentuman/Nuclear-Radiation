package igentuman.nr.block;

import igentuman.nr.tracking.CreativeRadSource;
import igentuman.nr.tracking.WorldSourceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

import static igentuman.nr.NuclearRadiation.CREATIVE_RAD_SOURCE_BE;

public class CreativeRadSourceBlockEntity extends BlockEntity {

    private double alphaBq = 0.0;
    private double betaBq = 0.0;
    private double xRayBq = 0.0;
    private double neutronBq = 0.0;
    private UUID sourceId = null;

    public CreativeRadSourceBlockEntity(BlockPos pos, BlockState state) {
        super(CREATIVE_RAD_SOURCE_BE.get(), pos, state);
    }

    public double getAlphaBq()   { return alphaBq; }
    public double getBetaBq()    { return betaBq; }
    public double getXRayBq()    { return xRayBq; }
    public double getNeutronBq() { return neutronBq; }

    public void applyConfig(double alpha, double beta, double xray, double neutron) {
        this.alphaBq = alpha;
        this.betaBq = beta;
        this.xRayBq = xray;
        this.neutronBq = neutron;
        setChanged();
        if (level instanceof ServerLevel server) {
            refreshSource(server);
        }
    }

    public void registerSource(ServerLevel server) {
        WorldSourceRegistry reg = WorldSourceRegistry.get(server);
        if (sourceId != null) {
            reg.remove(sourceId);
            sourceId = null;
        }
        double total = alphaBq + betaBq + xRayBq + neutronBq;
        if (total <= 0.0) return;
        sourceId = UUID.randomUUID();
        reg.register(new CreativeRadSource(
                sourceId, server.dimension(), getBlockPos().immutable(),
                alphaBq, betaBq, xRayBq, neutronBq, server.getGameTime()));
    }

    private void refreshSource(ServerLevel server) {
        registerSource(server);
    }

    public void removeSource(ServerLevel server) {
        if (sourceId != null) {
            WorldSourceRegistry.get(server).remove(sourceId);
            sourceId = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("alpha_bq", alphaBq);
        tag.putDouble("beta_bq", betaBq);
        tag.putDouble("xray_bq", xRayBq);
        tag.putDouble("neutron_bq", neutronBq);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        alphaBq   = tag.getDouble("alpha_bq");
        betaBq    = tag.getDouble("beta_bq");
        xRayBq    = tag.getDouble("xray_bq");
        neutronBq = tag.getDouble("neutron_bq");
    }

    public Component getDisplayName() {
        return Component.translatable("block.nuclear_radiation.creative_rad_source");
    }
}
