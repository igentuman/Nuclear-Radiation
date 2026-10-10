package igentuman.nr.block;

import igentuman.nr.radiation.source.CreativeRadSource;
import igentuman.nr.radiation.source.WorldSourceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    public void setRemoved() {
        if (level instanceof ServerLevel server) removeSource(server);
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putDouble("alpha_bq", alphaBq);
        output.putDouble("beta_bq", betaBq);
        output.putDouble("xray_bq", xRayBq);
        output.putDouble("neutron_bq", neutronBq);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        alphaBq   = input.getDoubleOr("alpha_bq", 0.0);
        betaBq    = input.getDoubleOr("beta_bq", 0.0);
        xRayBq    = input.getDoubleOr("xray_bq", 0.0);
        neutronBq = input.getDoubleOr("neutron_bq", 0.0);
    }

    public Component getDisplayName() {
        return Component.translatable("block.nuclear_radiation.creative_rad_source");
    }
}
