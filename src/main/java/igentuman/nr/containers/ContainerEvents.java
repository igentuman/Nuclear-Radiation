package igentuman.nr.containers;

import igentuman.nr.inventory.InventoryRadCache;
import igentuman.nr.util.WorldUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class ContainerEvents {

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        long now = server.getGameTime();
        if (now % ContainerRadiationTicker.RESCAN_INTERVAL_TICKS != 0) return;
        ContainerRadiationTicker.scanLevel(server);
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        BlockEntity be = WorldUtil.getBlockEntity(event.getPos(), server, false);
        if (be == null) return;
        if (isRadiatingCandidate(be)) {
            ContainerRadiationTicker.track(server, event.getPos());
        }
    }

    @SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        InventoryRadCache.get(player).markDirty();
        OpenContainerRegistry.open(player, event.getContainer());
    }

    @SubscribeEvent
    public void onContainerClose(PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        InventoryRadCache.get(player).markDirty();
        OpenContainerRegistry.close(player);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        ContainerRadiationTicker.untrack(server, event.getPos());
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (isRadiatingCandidate(be)) {
                ContainerRadiationTicker.track(server, be.getBlockPos());
            }
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            ContainerRadiationTicker.untrack(server, be.getBlockPos());
        }
    }

    private boolean isRadiatingCandidate(BlockEntity be) {
        return be instanceof IRadiatingContainer || be instanceof Container;
    }
}
