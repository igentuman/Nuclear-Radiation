package igentuman.nr.integration.projectexplosive;

import igentuman.nr.api.RadiationProfile;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.integration.createnucleartech.CreateNTHelper;
import igentuman.nr.radiation.source.LeftOverRadSource;
import igentuman.nr.radiation.source.WorldSourceRegistry;
import igentuman.nr.radiation.storage.ChunkRadiationData;
import igentuman.nr.radiation.storage.NRAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ProjectExplosiveIntegration {
    private static final String CONFIG_CLASS = "lil1llilol11.lo1i1iloioil.liooillio0101i10iiio010l";

    private ProjectExplosiveIntegration() {}

    public static void addExplosionFallout(ServerLevel level, BlockPos center, double intensity, int duration) {
        if (!RadiationConfig.masterEnabled() || !originalRadiationEnabled()
                || !Double.isFinite(intensity) || intensity <= 0 || duration <= 0 || durationFactor() <= 0) return;

        long now = level.getGameTime();
        RadiationProfile fallout = CreateNTHelper.buildFalloutProfile(450.0, 4, now);
        WorldSourceRegistry registry = WorldSourceRegistry.get(level);
        registry.register(new LeftOverRadSource(level, center.immutable(), fallout.copy(now), now, true));

        // PE derives its point-field radius as sqrt(intensity) * 5 (150 blocks for a nuke).
        depositLoadedChunks(level, center.getX(), center.getZ(),
                Math.min(Math.sqrt(intensity) * 5.0, 150.0), fallout, 0.02, now);
    }

    public static void addExplosionZone(ServerLevel level, double x, double z, double radius) {
        if (!RadiationConfig.masterEnabled() || !originalRadiationEnabled() || durationFactor() <= 0
                || !Double.isFinite(x) || !Double.isFinite(z) || !Double.isFinite(radius) || radius <= 0) return;
        ProjectExplosiveFalloutZones.register(level, x, z, Math.min(radius, 600.0));
    }

    private static void depositLoadedChunks(ServerLevel level, double xCenter, double zCenter,
                                            double radius, RadiationProfile fallout, double amount, long now) {
        // NR point fields normally stop at 64 blocks, so persist a diffuse dose
        // in already loaded chunks across PE's horizontal footprint.
        if (!RadiationConfig.chunkContaminationEnabled()) return;
        int chunkRadius = (int) Math.ceil(radius / 16.0);
        ChunkPos origin = new ChunkPos(BlockPos.containing(xCenter, level.getSeaLevel(), zCenter));
        double radiusSquared = radius * radius;
        WorldSourceRegistry registry = WorldSourceRegistry.get(level);
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                double x = origin.getMinBlockX() + dx * 16.0 + 8.0 - xCenter;
                double z = origin.getMinBlockZ() + dz * 16.0 + 8.0 - zCenter;
                double distanceSquared = x * x + z * z;
                if (distanceSquared >= radiusSquared) continue;
                ChunkPos pos = new ChunkPos(origin.x + dx, origin.z + dz);
                LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
                if (chunk == null) continue;
                double distance = Math.sqrt(distanceSquared) / radius;
                double strength = amount * (1.0 - distance) * (1.0 - distance);
                ChunkRadiationData data = chunk.getData(NRAttachments.CHUNK_RADIATION.get());
                data.air().mergeAtoms(fallout, strength, now);
                data.soil().mergeAtoms(fallout, strength * 0.5, now);
                data.setLastDecayTick(now);
                data.markExpiryDirty();
                registry.markChunkContaminated(pos);
                chunk.setUnsaved(true);
            }
        }
    }

    private static boolean originalRadiationEnabled() {
        try {
            Class<?> config = Class.forName(CONFIG_CLASS);
            ModConfigSpec.BooleanValue value = (ModConfigSpec.BooleanValue)
                    config.getField("l0o001o0ol0ilooioooil11i").get(null);
            return value.get();
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static int durationFactor() {
        try {
            Class<?> config = Class.forName(CONFIG_CLASS);
            ModConfigSpec.IntValue value = (ModConfigSpec.IntValue)
                    config.getField("ll1ol00o111oioool0l011lo").get(null);
            return value.get();
        } catch (ReflectiveOperationException e) {
            return 0;
        }
    }
}
