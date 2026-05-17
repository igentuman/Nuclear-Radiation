package igentuman.nr.entity;

import igentuman.nr.config.GeneralConfig;
import igentuman.nr.api.Isotope;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.core.RadiationQuality;
import igentuman.nr.core.Units;
import igentuman.nr.inventory.InventoryRadCache;
import igentuman.nr.network.NRNetwork;
import igentuman.nr.network.RadiationSyncPayload;
import igentuman.nr.persistence.EntityRadiationData;
import igentuman.nr.persistence.NRAttachments;
import igentuman.nr.registry.IsotopeRegistry;
import igentuman.nr.shielding.ArmorProtectionRegistry;
import igentuman.nr.shielding.AttenuationResult;
import igentuman.nr.shielding.RaycastCache;
import igentuman.nr.shielding.ShieldingRaycast;
import igentuman.nr.simulation.ChunkRadVector;
import igentuman.nr.simulation.RadiationSimulator;
import igentuman.nr.tools.GeigerCounterItem;
import igentuman.nr.tracking.WorldRadSource;
import igentuman.nr.tracking.WorldSourceRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public final class EntityDoseProcessor {

    private EntityDoseProcessor() {}

    private static final RadiationQuality DEFAULT_Q = RadiationQuality.DEFAULT;
    private static final double EMA_ALPHA = 0.1;

    public static void tick(ServerLevel level, LivingEntity entity, long now, int intervalTicks) {
        if (EntityIgnoreFilter.shouldSkip(entity)) return;

        EntityRadiationData data = entity.getData(NRAttachments.ENTITY_RADIATION.get());

        MedicineEffectsApplier.apply(entity, data, intervalTicks);

        double gyPerBqSec = RadiationConfig.GY_PER_BQ_SECOND.get();
        double intervalSeconds = intervalTicks * Units.SECONDS_PER_TICK;

        ArmorProtectionRegistry.Protection armor = ArmorProtectionRegistry.summed(entity);

        double svExternal = computeExternal(level, entity, gyPerBqSec, intervalSeconds, armor);
        double svInternal = computeInternal(data, gyPerBqSec, intervalSeconds, now);
        double svInventory = computeInventory(entity, gyPerBqSec, intervalSeconds, now, armor);

        double svThisTick = svExternal + svInternal + svInventory;
        double protection = clamp01(data.protectionFactor());
        svThisTick *= (1.0 - protection);

        data.addSv(svThisTick);

        double svPerHourInstant = (svThisTick / intervalSeconds) * Units.SECONDS_PER_HOUR;
        double prev = data.svPerHour();
        double rolling = prev + EMA_ALPHA * (svPerHourInstant - prev);
        data.setSvPerHour(rolling);

        double recovery = RadiationConfig.BASE_DECAY_SV_PER_HOUR.get()
                * data.decayMultiplier()
                * GeneralConfig.ENTITY_DECAY_MULTIPLIER.get()
                * (intervalSeconds / Units.SECONDS_PER_HOUR);
        if (recovery > 0 && data.svTotalCareer() > 0) {
            data.setSvTotalCareer(Math.max(0.0, data.svTotalCareer() - recovery));
        }

        RadiationEffects.apply(entity, data.svPerHour());

        if (entity instanceof ServerPlayer player) {
            double bq = GeigerCounterItem.readBq(level, entity);
            NRNetwork.sendTo(player, new RadiationSyncPayload(
                    data.svTotalCareer(), data.svPerHour(), bq));
        }
    }

    private static double computeExternal(ServerLevel level, LivingEntity entity,
                                          double gyPerBqSec, double intervalSeconds,
                                          ArmorProtectionRegistry.Protection armor) {
        ChunkPos cp = entity.chunkPosition();
        ChunkRadVector vec = RadiationSimulator.get().getChunkVector(level, cp);
        if (vec == null) return 0.0;

        double dx = entity.getX() - (cp.x * 16.0 + 8.0);
        double dz = entity.getZ() - (cp.z * 16.0 + 8.0);
        double bqXRay = Math.max(0.0, vec.centerScalarXRay + vec.gradientXRay.x * dx + vec.gradientXRay.z * dz);
        double bqNeutron = Math.max(0.0, vec.centerScalarNeutron + vec.gradientNeutron.x * dx + vec.gradientNeutron.z * dz);

        AttenuationResult shielding = computeShielding(level, entity);

        double svXRay = bqXRay * gyPerBqSec * DEFAULT_Q.qXRay * intervalSeconds
                * shielding.xrayPass() * (1.0 - armor.xray());
        double svNeutron = bqNeutron * gyPerBqSec * DEFAULT_Q.qNeutron * intervalSeconds
                * shielding.neutronPass() * (1.0 - armor.neutron());
        return svXRay + svNeutron;
    }

    private static AttenuationResult computeShielding(ServerLevel level, LivingEntity entity) {
        WorldSourceRegistry reg = WorldSourceRegistry.get(level);
        int radius = RadiationConfig.MAX_SOURCE_RADIUS_M.get();
        Vec3 to = entity.getEyePosition();
        List<WorldRadSource> nearby = reg.queryRadius(to, radius);
        if (nearby.isEmpty()) return AttenuationResult.UNATTENUATED;

        RaycastCache cache = reg.raycastCache();
        ChunkPos targetChunk = new ChunkPos((int) Math.floor(to.x) >> 4, (int) Math.floor(to.z) >> 4);
        double weightX = 0.0, sumX = 0.0;
        double weightN = 0.0, sumN = 0.0;
        for (WorldRadSource s : nearby) {
            if (!s.isActive()) continue;
            Vec3 from = s.emissionCenter();
            ChunkPos srcChunk = new ChunkPos((int) Math.floor(from.x) >> 4, (int) Math.floor(from.z) >> 4);
            AttenuationResult r = cache.get(srcChunk, targetChunk);
            if (r == null) {
                r = ShieldingRaycast.cast(level, from, to);
                cache.put(srcChunk, targetChunk, r);
            }
            double wx = Math.max(0.0, s.xRayBq());
            double wn = Math.max(0.0, s.neutronBq());
            sumX += r.xrayPass() * wx;
            weightX += wx;
            sumN += r.neutronPass() * wn;
            weightN += wn;
        }
        double xPass = weightX > 0 ? sumX / weightX : 1.0;
        double nPass = weightN > 0 ? sumN / weightN : 1.0;
        return new AttenuationResult(xPass, nPass);
    }

    private static double computeInternal(EntityRadiationData data, double gyPerBqSec,
                                          double intervalSeconds, long now) {
        if (data.internalContamination().isEmpty()) return 0.0;
        double sv = 0.0;
        for (Map.Entry<String, Double> e : data.internalContamination().entrySet()) {
            Isotope iso = IsotopeRegistry.get(e.getKey());
            if (iso == null) continue;
            double atoms = e.getValue();
            double bq = Units.atomsToBq(atoms, iso.halfLifeTicks());
            RadiationQuality q = iso.quality();
            double bqXRay = bq * iso.xRayStrength();
            double bqAB = bq * iso.alphaBetaStrength();
            double bqN = bq * iso.neutronStrength();
            sv += (bqXRay * q.qXRay + bqAB * q.qAlpha + bqN * q.qNeutron) * gyPerBqSec * intervalSeconds;
        }
        return sv;
    }

    private static double computeInventory(LivingEntity entity, double gyPerBqSec,
                                           double intervalSeconds, long now,
                                           ArmorProtectionRegistry.Protection armor) {
        InventoryRadCache cache = InventoryRadCache.get(entity);
        cache.rescan(entity, now);
        double armorBlocks = RadiationConfig.ARMOR_BLOCKS_INVENTORY.get();
        double xRayMul   = 1.0 - armor.xray()    * armorBlocks;
        double alphaMul  = 1.0 - armor.alpha()   * armorBlocks;
        double neutronMul = 1.0 - armor.neutron() * armorBlocks;
        double svXRay = cache.svXRayPerSecPerGyBq()      * gyPerBqSec * intervalSeconds * xRayMul;
        double svAB   = cache.svAlphaBetaPerSecPerGyBq() * gyPerBqSec * intervalSeconds * alphaMul;
        double svN    = cache.svNeutronPerSecPerGyBq()   * gyPerBqSec * intervalSeconds * neutronMul;
        return svXRay + svAB + svN;
    }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
