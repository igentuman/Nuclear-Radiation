package igentuman.nr.events;

import igentuman.nr.api.DecayGraph;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.block.CreativeRadSourceBlockEntity;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.client.particle.MeltdownParticles;
import igentuman.nr.radiation.source.*;
import igentuman.nr.radiation.storage.ChunkRadiationData;
import igentuman.nr.radiation.storage.NRAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.UUID;

public class RadSourceEvents {

    @SubscribeEvent
    public void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        WorldSourceRegistry.get(server).loadFromLevel();
    }

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
            if (src != null) {
                WorldSourceRegistry.get(server).remove(src.getId());
                long now = server.getGameTime();
                RadiationProfile leftOver = src.getProfile().copy(now);
                leftOver.reduceAtoms(2.0);
                WorldSourceRegistry.get(server).register(
                        new LeftOverRadSource(server, item.blockPosition(), leftOver, now, true));
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        BlockState state = event.getPlacedBlock();
        BlockPos pos = event.getPos();
        WorldSourceRegistry reg = WorldSourceRegistry.get(server);

        RadiationProfile blockProfile = RadiationBindings.of(state);
        if (!blockProfile.isEmpty()) {
            reg.register(new BlockRadSource(UUID.randomUUID(), server.dimension(), pos.immutable(),
                    blockProfile, server.getGameTime()));
            return;
        }
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            registerFluidSource(server, reg, pos, fluidState);
            return;
        }
        BlockEntity be = server.getBlockEntity(pos);
        if (be instanceof CreativeRadSourceBlockEntity creative) {
            creative.registerSource(server);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        WorldSourceRegistry reg = WorldSourceRegistry.get(server);
        DecayGraph.WorldRadSource src = reg.atBlock(event.getPos());
        if (src != null) reg.remove(src.getId());
        if (server.getBlockEntity(event.getPos()) instanceof CreativeRadSourceBlockEntity creative) {
            creative.removeSource(server);
        }
    }

    @SubscribeEvent
    public void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        BlockPos pos = event.getPos();
        WorldSourceRegistry reg = WorldSourceRegistry.get(server);
        FluidState fluidState = server.getFluidState(pos);
        DecayGraph.WorldRadSource existing = reg.atBlock(pos);
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
        }
    }

    private void registerFluidSource(ServerLevel server, WorldSourceRegistry reg,
                                     BlockPos pos, FluidState fluidState) {
        RadiationProfile profile = RadiationBindings.of(fluidState);
        if (profile.isEmpty()) return;
        if (reg.atBlock(pos) != null) return;
        reg.register(new FluidRadSource(UUID.randomUUID(), server.dimension(), pos.immutable(),
                profile, server.getGameTime()));
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
        } else if (!data.isEmpty()) {
            WorldSourceRegistry.get(server).markChunkContaminated(chunk.getPos());
        }
        scanChunkForSources(server, chunk);
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        WorldSourceRegistry reg = WorldSourceRegistry.get(server);
        ChunkPos cp = chunk.getPos();
        for (DecayGraph.WorldRadSource s : reg.all()) {
            BlockPos p = s.getPosition();
            if ((p.getX() >> 4) == cp.x && (p.getZ() >> 4) == cp.z) {
                if (s instanceof BlockRadSource || s instanceof FluidRadSource || s instanceof CreativeRadSource) {
                    reg.remove(s.getId());
                }
            }
        }
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (be instanceof CreativeRadSourceBlockEntity creative) {
                creative.removeSource(server);
            }
        }
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        long now = server.getGameTime();
        MeltdownParticles.getOrCreate(server).spawnTick(server);
        if (now % 3 == 0) WorldSourceRegistry.get(server).emitGasParticles();
        int interval = RadiationConfig.WORLD_SIM_INTERVAL_TICKS.get();
        if (now % interval != 0) return;
        WorldSourceRegistry reg = WorldSourceRegistry.get(server);
        reg.tickDecay(now, server);
        reg.tickChunkDecay(now);
        if (now % interval*2 != 0) return;
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
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (be instanceof CreativeRadSourceBlockEntity creative) {
                creative.registerSource(server);
            }
        }
    }
}
