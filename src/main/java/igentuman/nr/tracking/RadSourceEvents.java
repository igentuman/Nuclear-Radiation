package igentuman.nr.tracking;

import igentuman.nr.binding.RadiationBindings;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.core.RadiationProfile;
import igentuman.nr.persistence.ChunkRadiationData;
import igentuman.nr.persistence.NRAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.UUID;

public class RadSourceEvents {

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity item) {
            RadiationProfile profile = RadiationBindings.of(item.getItem());
            if (profile.isEmpty()) return;
            WorldSourceRegistry.get(server).register(
                    new ItemEntityRadSource(item, profile, server.getGameTime()));
        }
    }

    @SubscribeEvent
    public void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity item) {
            ItemEntityRadSource src = WorldSourceRegistry.get(server).forItemEntity(item.getUUID());
            if (src != null) WorldSourceRegistry.get(server).remove(src.getId());
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        BlockState state = event.getPlacedBlock();
        BlockPos pos = event.getPos();
        WorldSourceRegistry reg = WorldSourceRegistry.get(server);
        reg.raycastCache().invalidateChunk(new ChunkPos(pos));

        RadiationProfile blockProfile = RadiationBindings.of(state);
        if (!blockProfile.isEmpty()) {
            reg.register(new BlockRadSource(UUID.randomUUID(), server.dimension(), pos.immutable(),
                    blockProfile, server.getGameTime()));
            return;
        }
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            registerFluidSource(server, reg, pos, fluidState);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        WorldSourceRegistry reg = WorldSourceRegistry.get(server);
        reg.raycastCache().invalidateChunk(new ChunkPos(event.getPos()));
        WorldRadSource src = reg.atBlock(event.getPos());
        if (src != null) reg.remove(src.getId());
    }

    @SubscribeEvent
    public void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        BlockPos pos = event.getPos();
        WorldSourceRegistry reg = WorldSourceRegistry.get(server);
        FluidState fluidState = server.getFluidState(pos);
        WorldRadSource existing = reg.atBlock(pos);
        if (!fluidState.isEmpty()) {
            RadiationProfile p = RadiationBindings.of(fluidState);
            if (!p.isEmpty()) {
                if (existing == null) {
                    registerFluidSource(server, reg, pos, fluidState);
                }
                return;
            }
        }
        if (existing instanceof FluidRadSource) {
            reg.remove(existing.getId());
            reg.raycastCache().invalidateChunk(new ChunkPos(pos));
        }
    }

    private void registerFluidSource(ServerLevel server, WorldSourceRegistry reg,
                                     BlockPos pos, FluidState fluidState) {
        RadiationProfile profile = RadiationBindings.of(fluidState);
        if (profile.isEmpty()) return;
        if (reg.atBlock(pos) != null) return;
        reg.register(new FluidRadSource(UUID.randomUUID(), server.dimension(), pos.immutable(),
                profile, server.getGameTime()));
        reg.raycastCache().invalidateChunk(new ChunkPos(pos));
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        long now = server.getGameTime();
        ChunkRadiationData data = chunk.getData(NRAttachments.CHUNK_RADIATION.get());
        if (data.isExpired(now)) {
            data.clearIfExpired(now);
            chunk.setUnsaved(true);
        }
        scanChunkForSources(server, chunk);
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        WorldSourceRegistry reg = WorldSourceRegistry.get(server);
        ChunkPos cp = chunk.getPos();
        reg.raycastCache().invalidateChunk(cp);
        for (WorldRadSource s : reg.all()) {
            BlockPos p = s.getPosition();
            if ((p.getX() >> 4) == cp.x && (p.getZ() >> 4) == cp.z) {
                if (s instanceof BlockRadSource || s instanceof FluidRadSource) {
                    reg.remove(s.getId());
                }
            }
        }
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        long now = server.getGameTime();
        int interval = RadiationConfig.WORLD_SIM_INTERVAL_TICKS.get();
        if (now % interval != 0) return;
        WorldSourceRegistry reg = WorldSourceRegistry.get(server);
        reg.tickDecay(now);
        reg.spreadContamination(now, interval);
    }

    private void scanChunkForSources(ServerLevel server, LevelChunk chunk) {
        ChunkPos cp = chunk.getPos();
        WorldSourceRegistry reg = WorldSourceRegistry.get(server);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        long spawnTick = server.getGameTime();
        int minSection = chunk.getMinSection();
        LevelChunkSection[] sections = chunk.getSections();
        for (int si = 0; si < sections.length; si++) {
            LevelChunkSection section = sections[si];
            if (section == null || section.hasOnlyAir()) continue;
            int baseY = (minSection + si) << 4;
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    for (int ly = 0; ly < 16; ly++) {
                        BlockState state = section.getBlockState(lx, ly, lz);
                        if (state.isAir()) continue;
                        boolean placed = false;
                        RadiationProfile bp = RadiationBindings.of(state);
                        if (!bp.isEmpty()) {
                            cursor.set(cp.getMinBlockX() + lx, baseY + ly, cp.getMinBlockZ() + lz);
                            if (reg.atBlock(cursor) == null) {
                                reg.register(new BlockRadSource(UUID.randomUUID(), server.dimension(),
                                        cursor.immutable(), bp, spawnTick));
                            }
                            placed = true;
                        }
                        if (!placed) {
                            FluidState fs = state.getFluidState();
                            if (fs.isEmpty()) continue;
                            RadiationProfile fp = RadiationBindings.of(fs);
                            if (fp.isEmpty()) continue;
                            cursor.set(cp.getMinBlockX() + lx, baseY + ly, cp.getMinBlockZ() + lz);
                            if (reg.atBlock(cursor) == null) {
                                reg.register(new FluidRadSource(UUID.randomUUID(), server.dimension(),
                                        cursor.immutable(), fp, spawnTick));
                            }
                        }
                    }
                }
            }
        }
    }
}
