package igentuman.nr.block;

import igentuman.nr.radiation.source.CoriumManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Meltdown corium fluid block. Hot (fire source, full light, ignites entities on contact) and
 * registers each placed position with {@link CoriumManager}, which drives area burning,
 * hardness-based melting of the block below, water quenching, and random solidification.
 */
public class CoriumFluidBlock extends LiquidBlock {

    public CoriumFluidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 15;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
        if (!level.isClientSide()) {
            entity.igniteForSeconds(1.0F);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level instanceof ServerLevel sl) {
            CoriumManager.track(sl, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!level.getBlockState(pos).is(this)) {
            CoriumManager.untrack(level, pos);
        }
    }
}
