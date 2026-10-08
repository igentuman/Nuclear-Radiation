package igentuman.nr.events;

import igentuman.nr.api.DecayGraph;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.block.CreativeRadSourceBlockEntity;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.client.particle.MeltdownParticles;
import igentuman.nr.radiation.source.*;
import igentuman.nr.integration.projectexplosive.ProjectExplosiveFalloutZones;
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
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
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
        if (!RadiationConfig.itemEntityRadiationEnabled()) return;
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
        if (!RadiationConfig.itemEntityRadiationEnabled()) return;
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
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        long now = server.getGameTime();
        ChunkRadiationData data = chunk.getData(NRAttachments.CHUNK_RADIATION.get());
        if (data.isExpired(now)) {
            data.clearIfExpired(now);
            chunk.setUnsaved(true);
        } else if (!data.isEmpty() && RadiationConfig.chunkContaminationEnabled()) {
            WorldSourceRegistry.get(server).markChunkContaminated(chunk.getPos());
        }
        if (ModList.get().isLoaded("projectexplosive")) {
            ProjectExplosiveFalloutZones.onChunkLoad(server, chunk);
        }
        scanChunkForSources(server, chunk);
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        WorldSourceRegistry reg = WorldSourceRegistry.get(server);
        ChunkPos cp = chunk.getPos();
        reg.unmarkChunkContaminated(cp);
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
        if (!RadiationConfig.masterEnabled()) return;
        long now = server.getGameTime();
        MeltdownParticles.getOrCreate(server).spawnTick(server);
        WorldSourceRegistry.get(server).tickSableSync(server);
        if (now % 3 == 0) WorldSourceRegistry.get(server).emitGasParticles();
        int interval = RadiationConfig.WORLD_SIM_INTERVAL_TICKS.get();
        if (now % interval != 0) return;
        WorldSourceRegistry reg = WorldSourceRegistry.get(server);
        reg.tickDecay(now, server);
        if (RadiationConfig.chunkContaminationEnabled()) {
            reg.tickChunkDecay(now);
            if (now % (interval * 2L) == 0) reg.spreadContamination(now, interval * 2L);
        }
    }

    private void scanChunkForSources(ServerLevel server, LevelChunk chunk) {
        scanBlocksAndFluids(server, chunk, RadiationConfig.blockRadiationEnabled(), RadiationConfig.fluidRadiationEnabled());
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (be instanceof CreativeRadSourceBlockEntity creative) {
                creative.registerSource(server);
            }
        }
    }

    private void scanBlocksAndFluids(ServerLevel server, LevelChunk chunk, boolean blockEnabled, boolean fluidEnabled) {
        if (!blockEnabled && !fluidEnabled) return;
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
                        if (blockEnabled) {
                            RadiationProfile bp = RadiationBindings.of(state);
                            if (!bp.isEmpty()) {
                                cursor.set(cp.getMinBlockX() + lx, baseY + ly, cp.getMinBlockZ() + lz);
                                if (reg.atBlock(cursor) == null) {
                                    reg.register(new BlockRadSource(UUID.randomUUID(), server.dimension(),
                                            cursor.immutable(), bp, spawnTick));
                                }
                                placed = true;
                            }
                        }
                        if (!placed && fluidEnabled) {
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
