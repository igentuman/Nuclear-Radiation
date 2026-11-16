package igentuman.nr.radiation.simulation;

import igentuman.nr.api.IPointRadiationSource;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.RadiationQuality;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.api.shielding.ArmorProtectionRegistry.Protection;
import igentuman.nr.compat.megaton.MegatonRadiationBridge;
import igentuman.nr.compat.ntm.NtmCompatibility;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.radiation.shielding.world.ShieldingRaycast;
import igentuman.nr.radiation.simulation.entity.EntityIgnoreFilter;
import igentuman.nr.radiation.simulation.inventory.InventoryRadCache;
import igentuman.nr.radiation.storage.NRAttachments;
import igentuman.nr.util.PerlinNoise;
import igentuman.nr.util.Units;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Instantaneous physical Sv/s. No armor, entity treatment, display gain or smoothing is implicit. */
public final class RadiationFieldSampler {
    private RadiationFieldSampler() {}

    public record Rate(double alpha, double beta, double xray, double neutron) {
        public static final Rate ZERO = new Rate(0, 0, 0, 0);
        public double total() { return alpha + beta + xray + neutron; }
        public double protectedRate(Protection armor) {
            return alpha * (1 - armor.alpha()) + beta * (1 - armor.beta())
                    + xray * (1 - armor.xray()) + neutron * (1 - armor.neutron());
        }
        public Rate plus(Rate other) {
            return new Rate(alpha + other.alpha, beta + other.beta, xray + other.xray, neutron + other.neutron);
        }
        public Rate scale(double factor) {
            return new Rate(alpha * factor, beta * factor, xray * factor, neutron * factor);
        }
    }

    public static Rate profile(RadiationProfile profile) {
        double a = 0, b = 0, x = 0, n = 0;
        double gy = RadiationConfig.GY_PER_BQ_SECOND.get();
        for (var stack : profile.stacks()) {
            var isotope = stack.isotope();
            var q = isotope.quality();
            double activity = stack.currentActivityBq() * gy;
            a += activity * isotope.alphaStrength() * q.qAlpha;
            b += activity * isotope.betaStrength() * q.qBeta;
            x += activity * isotope.xRayStrength() * q.qXRay;
            n += activity * isotope.neutronStrength() * q.qNeutron;
        }
        return new Rate(a, b, x, n);
    }

    public static Rate sample(ServerLevel level, Vec3 position) {
        BlockPos pos = BlockPos.containing(position);
        if (!RadiationConfig.masterEnabled() || loaded(level, pos) == null) return Rate.ZERO;
        Rate result = profile(NtmCompatibility.worldProfile(level, pos));
        double gy = RadiationConfig.GY_PER_BQ_SECOND.get();
        var q = RadiationQuality.DEFAULT;
        double radius = RadiationConfig.MAX_SOURCE_RADIUS_M.get();
        for (var source : RadiationSimulator.get().indexFor(level).queryRadius(position, radius)) {
            if (!(source instanceof IPointRadiationSource point) || !source.isActive()) continue;
            Vec3 center = point.emissionCenter();
            double distance = center.distanceToSqr(position);
            var pass = ShieldingRaycast.cast(level, position, center);
            double coupling = gy / (distance + 1.0);
            result = result.plus(new Rate(0, 0, point.xRayBq() * coupling * q.qXRay * pass.xrayPass(),
                    point.neutronBq() * coupling * q.qNeutron * pass.neutronPass()));
        }
        if (RadiationConfig.blockRadiationEnabled()) {
            result = result.plus(defaultQualityProfile(RadiationBindings.of(level.getBlockState(pos))));
        }
        result = result.plus(contamination(level, pos, level.getFluidState(pos).is(FluidTags.WATER)));
        result = result.plus(nearbyInventories(level, position, null));
        double fallout = MegatonRadiationBridge.zoneRate(level, position) * MegatonRadiationBridge.falloutSvPerGameRad();
        return result.plus(new Rate(0, 0, background(level, pos) + fallout, 0));
    }

    public static Rate nearbyInventories(ServerLevel level, Vec3 position, LivingEntity excluded) {
        if (!RadiationConfig.inventoryRadiationEnabled()) return Rate.ZERO;
        double radius = RadiationConfig.MAX_SOURCE_RADIUS_M.get();
        var box = new AABB(position.x - radius, position.y - radius, position.z - radius,
                position.x + radius, position.y + radius, position.z + radius);
        double x = 0, n = 0;
        double gy = RadiationConfig.GY_PER_BQ_SECOND.get();
        var q = RadiationQuality.DEFAULT;
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (other == excluded || EntityIgnoreFilter.shouldSkip(other)) continue;
            Vec3 center = other.getEyePosition();
            double distance = center.distanceToSqr(position);
            if (distance > radius * radius) continue;
            InventoryRadCache cache = InventoryRadCache.get(other);
            cache.rescan(other, level.getGameTime());
            if (cache.bqXRay() == 0 && cache.bqNeutron() == 0) continue;
            var pass = ShieldingRaycast.cast(level, position, center);
            double coupling = gy / (distance + 1.0);
            x += cache.bqXRay() * coupling * q.qXRay * pass.xrayPass();
            n += cache.bqNeutron() * coupling * q.qNeutron * pass.neutronPass();
        }
        return new Rate(0, 0, x, n);
    }

    public static Rate contamination(ServerLevel level, BlockPos pos, boolean underwater) {
        LevelChunk chunk = loaded(level, pos);
        if (!RadiationConfig.chunkContaminationEnabled() || chunk == null) return Rate.ZERO;
        var data = chunk.getData(NRAttachments.CHUNK_RADIATION.get());
        Rate airHere = defaultQualityProfile(data.air());
        Rate result = airHere.plus(defaultQualityProfile(data.soil()));
        // Soil contributes penetrating radiation only; air alpha/beta are local contact exposure.
        result = new Rate(airHere.alpha(), airHere.beta(), result.xray(), result.neutron());
        if (underwater) {
            Rate water = defaultQualityProfile(data.water());
            result = new Rate(result.alpha(), result.beta(), result.xray() * 0.5 + water.xray(),
                    result.neutron() * 0.5 + water.neutron());
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                var neighbor = level.getChunkSource().getChunkNow(chunk.getPos().x() + dx, chunk.getPos().z() + dz);
                if (neighbor == null) continue;
                var other = neighbor.getData(NRAttachments.CHUNK_RADIATION.get());
                if (other.air().isEmpty()) continue;
                Rate air = defaultQualityProfile(other.air());
                Rate soil = defaultQualityProfile(other.soil());
                result = result.plus(new Rate(air.alpha(), air.beta(), air.xray() + soil.xray(),
                        air.neutron() + soil.neutron()).scale(0.5));
            }
        }
        return result.scale(10.0);
    }

    // These existing NR providers use DEFAULT_Q in EntityDoseProcessor. Match that
    // policy for instruments while volume releases retain their isotope qualities.
    private static Rate defaultQualityProfile(RadiationProfile profile) {
        var q = RadiationQuality.DEFAULT;
        double gy = RadiationConfig.GY_PER_BQ_SECOND.get();
        return new Rate(profile.alphaActivityBq() * gy * q.qAlpha, profile.betaActivityBq() * gy * q.qBeta,
                profile.xRayActivityBq() * gy * q.qXRay, profile.neutronActivityBq() * gy * q.qNeutron);
    }

    public static double background(ServerLevel level, BlockPos pos) {
        var biome = level.getBiome(pos).unwrapKey().map(key -> key.identifier()).orElse(null);
        Double value = RadiationConfig.biomeBackgroundUSvPerHour(biome);
        if (value == null) value = RadiationConfig.levelBackgroundUSvPerHour(level.dimension().identifier());
        if (value == null) value = RadiationConfig.DEFAULT_BACKGROUND_USV_PER_HOUR.get();
        if (value <= 0) return 0;
        double spatial = PerlinNoise.noise(pos.getX() / 64.0, pos.getY() / 64.0, pos.getZ() / 64.0);
        double temporal = PerlinNoise.noise(level.getGameTime() * Units.SECONDS_PER_TICK * 0.3, 0, 0);
        return value * 1.0e-6 * (1.0 + spatial * 0.10 + temporal * 0.05) / Units.SECONDS_PER_HOUR;
    }

    private static LevelChunk loaded(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
    }
}
