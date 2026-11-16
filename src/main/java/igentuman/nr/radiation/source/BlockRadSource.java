package igentuman.nr.radiation.source;

import igentuman.nr.api.AbstractWorldRadSource;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.api.isotope.IsotopeStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public final class BlockRadSource extends AbstractWorldRadSource {

    public boolean contaminatesArea = false;
    /** Constant emission while the block exists: no decay, daughters or expiry. */
    private final boolean sustained;

    public BlockRadSource(UUID id, ResourceKey<Level> dim, BlockPos pos,
                          RadiationProfile profile, long spawnedTick) {
        this(id, dim, pos, profile, spawnedTick, true);
    }

    public BlockRadSource(UUID id, ResourceKey<Level> dim, BlockPos pos,
                          RadiationProfile profile, long spawnedTick, boolean contamiateArea) {
        this(id, dim, pos, profile, spawnedTick, contamiateArea, false);
    }

    private BlockRadSource(UUID id, ResourceKey<Level> dim, BlockPos pos, RadiationProfile profile,
                           long spawnedTick, boolean contamiateArea, boolean sustained) {
        super(id, dim, pos, profile, spawnedTick);
        this.contaminatesArea = contamiateArea;
        this.sustained = sustained;
        if (sustained) this.expiryGameTime = Long.MAX_VALUE;
    }

    /**
     * Source for a block resolved through {@link RadiationBindings#of(BlockState)}. Imported fallbacks
     * (NTM hazard blocks) emit at their native constant level while placed and never seed chunk
     * contamination or fallout dust; explicit NR bindings keep NR's decaying, contaminating behavior.
     */
    public static BlockRadSource forState(UUID id, ResourceKey<Level> dim, BlockPos pos, BlockState state,
                                          RadiationProfile profile, long spawnedTick) {
        boolean imported = !RadiationBindings.explicit(state);
        return new BlockRadSource(id, dim, pos, profile, spawnedTick, !imported, imported);
    }

    public boolean sustained() { return sustained; }

    @Override
    public boolean contaminatesArea() { return contaminatesArea; }

    @Override
    public void advanceDecay(long currentTick) {
        if (!sustained) {
            super.advanceDecay(currentTick);
            return;
        }
        for (IsotopeStack s : profile.stacks()) s.setTimestamp(currentTick);
        expiryGameTime = Long.MAX_VALUE;
    }

    @Override
    public void recomputeExpiry() {
        if (sustained) expiryGameTime = Long.MAX_VALUE;
        else super.recomputeExpiry();
    }
}
