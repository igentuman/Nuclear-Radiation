package igentuman.nr.entity;

import igentuman.nr.config.GeneralConfig;
import igentuman.nr.api.Isotope;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.api.RadiationQuality;
import igentuman.nr.api.Units;
import igentuman.nr.inventory.InventoryRadCache;
import igentuman.nr.network.ChunkVectorDebugPayload;
import igentuman.nr.network.NRNetwork;
import igentuman.nr.network.RadiationSyncPayload;
import igentuman.nr.persistence.EntityRadiationData;
import igentuman.nr.persistence.NRAttachments;
import igentuman.nr.registry.IsotopeRegistry;
import igentuman.nr.network.ShieldingRaysDebugPayload;
import igentuman.nr.shielding.ArmorProtectionRegistry;
import igentuman.nr.shielding.AttenuationResult;
import igentuman.nr.shielding.ShieldingRaycast;
import igentuman.nr.simulation.ChunkRadVector;
import igentuman.nr.simulation.RadiationSimulator;
import igentuman.nr.tools.GeigerCounterItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
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

            if (RadiationConfig.DEBUG_RADIATION_VECTORS.get()) {
                ChunkPos cp = entity.chunkPosition();
                ChunkRadVector vec = RadiationSimulator.get().getChunkVector(level, cp);
                if (vec != null) {
                    NRNetwork.sendTo(player, new ChunkVectorDebugPayload(
                            cp.x, cp.z,
                            vec.gradientXRay.x, vec.gradientXRay.z,
                            vec.gradientNeutron.x, vec.gradientNeutron.z,
                            vec.centerScalarXRay, vec.centerScalarNeutron,
                            vec.maxBq, vec.centerY));
                }
            }
        }
    }

    private static double computeExternal(ServerLevel level, LivingEntity entity,
                                          double gyPerBqSec, double intervalSeconds,
                                          ArmorProtectionRegistry.Protection armor) {
        ChunkPos cp = entity.chunkPosition();
        ChunkRadVector vec = RadiationSimulator.get().getChunkVector(level, cp);
        if (vec == null || vec.contribs.isEmpty()) return 0.0;

        double ex = entity.getX();
        double ey = entity.getY();
        double ez = entity.getZ();
        double bqXRay = 0.0;
        double bqNeutron = 0.0;
        for (ChunkRadVector.Contrib c : vec.contribs) {
            double dx = ex - c.x();
            double dy = ey - c.y();
            double dz = ez - c.z();
            double inv = 1.0 / (dx * dx + dy * dy + dz * dz + 1.0);
            bqXRay += c.xRayBq() * inv;
            bqNeutron += c.neutronBq() * inv;
        }

        AttenuationResult shielding = computeShielding(level, entity);

        double svXRay = bqXRay * gyPerBqSec * DEFAULT_Q.qXRay * intervalSeconds
                * shielding.xrayPass() * (1.0 - armor.xray());
        double svNeutron = bqNeutron * gyPerBqSec * DEFAULT_Q.qNeutron * intervalSeconds
                * shielding.neutronPass() * (1.0 - armor.neutron());
        return svXRay + svNeutron;
    }

    private static final int SAMPLE_RAYS = 4;
    private static final double SAMPLE_ANGLE_RAD = Math.toRadians(12.0);
    private static final byte CHANNEL_XRAY = 0;
    private static final byte CHANNEL_NEUTRON = 1;

    private record RayHit(Vec3 endpoint, double xrayPass, double neutronPass) {}

    private static AttenuationResult computeShielding(ServerLevel level, LivingEntity entity) {
        ChunkPos cp = entity.chunkPosition();
        ChunkRadVector vec = RadiationSimulator.get().getChunkVector(level, cp);
        if (vec == null) return AttenuationResult.UNATTENUATED;

        Vec3 eye = entity.getEyePosition();
        double radius = RadiationConfig.MAX_SOURCE_RADIUS_M.get();
        double chunkCenterX = cp.x * 16.0 + 8.0;
        double chunkCenterZ = cp.z * 16.0 + 8.0;

        double gx = vec.gradientXRay.x + vec.gradientNeutron.x;
        double gz = vec.gradientXRay.z + vec.gradientNeutron.z;
        if (gx * gx + gz * gz < 1.0e-6) return AttenuationResult.UNATTENUATED;

        boolean debug = RadiationConfig.DEBUG_RADIATION_VECTORS.get()
                && entity instanceof ServerPlayer;
        List<RayHit> hits = debug ? new ArrayList<>(SAMPLE_RAYS + 1) : null;

        double targetX = chunkCenterX + gx;
        double targetZ = chunkCenterZ + gz;
        double dirX = targetX - eye.x;
        double dirY = vec.centerY - eye.y;
        double dirZ = targetZ - eye.z;
        double dirLen = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (dirLen < 1.0e-6) return AttenuationResult.UNATTENUATED;
        dirX /= dirLen; dirY /= dirLen; dirZ /= dirLen;

        double rayLen = Math.min(dirLen, radius);

        double upX = 0, upY = 1, upZ = 0;
        double uX, uY, uZ;
        if (Math.abs(dirY) > 0.95) {
            uX = 1; uY = 0; uZ = 0;
        } else {
            uX = dirY * upZ - dirZ * upY;
            uY = dirZ * upX - dirX * upZ;
            uZ = dirX * upY - dirY * upX;
        }
        double uLen = Math.sqrt(uX * uX + uY * uY + uZ * uZ);
        if (uLen < 1.0e-6) { uX = 1; uY = 0; uZ = 0; }
        else { uX /= uLen; uY /= uLen; uZ /= uLen; }
        double vX = dirY * uZ - dirZ * uY;
        double vY = dirZ * uX - dirX * uZ;
        double vZ = dirX * uY - dirY * uX;

        double sumX = 0.0;
        double sumN = 0.0;
        int count = 0;
        double sin = Math.sin(SAMPLE_ANGLE_RAD);
        double cos = Math.cos(SAMPLE_ANGLE_RAD);

        AttenuationResult r0 = castBoth(level, eye, dirX, dirY, dirZ, rayLen, hits);
        sumX += r0.xrayPass();
        sumN += r0.neutronPass();
        count++;

        double[][] offsets = {
                { uX,  uY,  uZ},
                {-uX, -uY, -uZ},
                { vX,  vY,  vZ},
                {-vX, -vY, -vZ},
        };
        for (int i = 0; i < SAMPLE_RAYS; i++) {
            double[] o = offsets[i];
            double ox = dirX * cos + o[0] * sin;
            double oy = dirY * cos + o[1] * sin;
            double oz = dirZ * cos + o[2] * sin;
            double ol = Math.sqrt(ox * ox + oy * oy + oz * oz);
            ox /= ol; oy /= ol; oz /= ol;
            AttenuationResult r = castBoth(level, eye, ox, oy, oz, rayLen, hits);
            sumX += r.xrayPass();
            sumN += r.neutronPass();
            count++;
        }

        if (debug && hits != null && !hits.isEmpty()) {
            sendRaysDebug((ServerPlayer) entity, eye, hits);
        }

        return new AttenuationResult(sumX / count, sumN / count);
    }

    private static AttenuationResult castBoth(ServerLevel level, Vec3 eye,
                                              double dx, double dy, double dz, double len,
                                              List<RayHit> hits) {
        Vec3 endpoint = new Vec3(eye.x + dx * len, eye.y + dy * len, eye.z + dz * len);
        AttenuationResult r = ShieldingRaycast.cast(level, eye, endpoint);
        if (hits != null) hits.add(new RayHit(endpoint, r.xrayPass(), r.neutronPass()));
        return r;
    }

    private static void sendRaysDebug(ServerPlayer player, Vec3 origin, List<RayHit> hits) {
        int n = hits.size() * 2;
        double[] ex = new double[n];
        double[] ey = new double[n];
        double[] ez = new double[n];
        float[] pv = new float[n];
        byte[] ch = new byte[n];
        int idx = 0;
        for (RayHit h : hits) {
            ex[idx] = h.endpoint.x; ey[idx] = h.endpoint.y; ez[idx] = h.endpoint.z;
            pv[idx] = (float) h.xrayPass;   ch[idx] = CHANNEL_XRAY;    idx++;
            ex[idx] = h.endpoint.x; ey[idx] = h.endpoint.y; ez[idx] = h.endpoint.z;
            pv[idx] = (float) h.neutronPass; ch[idx] = CHANNEL_NEUTRON; idx++;
        }
        igentuman.nr.network.NRNetwork.sendTo(player,
                new ShieldingRaysDebugPayload(origin.x, origin.y, origin.z, ex, ey, ez, pv, ch));
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
            double bqAlpha = bq * iso.alphaStrength();
            double bqBeta = bq * iso.betaStrength();
            double bqN = bq * iso.neutronStrength();
            sv += (bqXRay * q.qXRay + bqAlpha * q.qAlpha + bqBeta * q.qBeta + bqN * q.qNeutron)
                    * gyPerBqSec * intervalSeconds;
        }
        return sv;
    }

    private static double computeInventory(LivingEntity entity, double gyPerBqSec,
                                           double intervalSeconds, long now,
                                           ArmorProtectionRegistry.Protection armor) {
        InventoryRadCache cache = InventoryRadCache.get(entity);
        cache.rescan(entity, now);
        double armorBlocks = RadiationConfig.ARMOR_BLOCKS_INVENTORY.get();
        double inventoryAlphaPass = RadiationConfig.INVENTORY_ALPHA_PASS.get();
        double inventoryBetaPass = RadiationConfig.INVENTORY_BETA_PASS.get();
        double xRayMul   = 1.0 - armor.xray()    * armorBlocks;
        double alphaMul  = (1.0 - armor.alpha() * armorBlocks) * inventoryAlphaPass;
        double betaMul   = (1.0 - armor.beta()  * armorBlocks) * inventoryBetaPass;
        double neutronMul = 1.0 - armor.neutron() * armorBlocks;
        double svXRay = cache.svXRayPerSecPerGyBq()    * gyPerBqSec * intervalSeconds * xRayMul;
        double svAlpha = cache.svAlphaPerSecPerGyBq()  * gyPerBqSec * intervalSeconds * alphaMul;
        double svBeta = cache.svBetaPerSecPerGyBq()    * gyPerBqSec * intervalSeconds * betaMul;
        double svN    = cache.svNeutronPerSecPerGyBq() * gyPerBqSec * intervalSeconds * neutronMul;
        return svXRay + svAlpha + svBeta + svN;
    }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
