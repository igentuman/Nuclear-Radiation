package igentuman.nr.block;

import com.mojang.serialization.MapCodec;
import igentuman.nr.network.CreativeRadSourceOpenPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class CreativeRadSourceBlock extends BaseEntityBlock {

    public static final MapCodec<CreativeRadSourceBlock> CODEC = simpleCodec(CreativeRadSourceBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    public CreativeRadSourceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeRadSourceBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel server)) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof CreativeRadSourceBlockEntity be)) return InteractionResult.PASS;

        PacketDistributor.sendToPlayer(sp, new CreativeRadSourceOpenPayload(
                pos,
                be.getAlphaBq(),
                be.getBetaBq(),
                be.getXRayBq(),
                be.getNeutronBq()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof CreativeRadSourceBlockEntity be) {
                be.removeSource(server);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
