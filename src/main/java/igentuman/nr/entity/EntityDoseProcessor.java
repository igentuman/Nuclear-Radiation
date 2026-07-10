package igentuman.nr.entity;

import igentuman.nr.api.Isotope;
import igentuman.nr.api.NREvents;
import igentuman.nr.api.RadiationQuality;
import igentuman.nr.api.Units;
import igentuman.nr.config.GeneralConfig;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.inventory.InventoryRadCache;
import igentuman.nr.network.ChunkContaminationDebugPayload;
import igentuman.nr.network.ChunkVectorDebugPayload;
import igentuman.nr.network.NRNetwork;
import igentuman.nr.network.RadiationSyncPayload;
import igentuman.nr.network.ShieldingRaysDebugPayload;
import igentuman.nr.util.persistence.ChunkRadiationData;
import igentuman.nr.util.persistence.EntityRadiationData;
import igentuman.nr.util.persistence.NRAttachments;
import igentuman.nr.registry.IsotopeRegistry;
import igentuman.nr.shielding.ArmorProtectionRegistry;
import igentuman.nr.shielding.AttenuationResult;
import igentuman.nr.shielding.ShieldingRaycast;
import igentuman.nr.simulation.SubChunkRadVector;
import igentuman.nr.simulation.RadiationSimulator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EntityDoseProcessor {

    private EntityDoseProcessor() {}

    private static final RadiationQuality DEFAULT_Q = RadiationQuality.DEFAULT;
    private static final double EMA_ALPHA = 0.2;

    public static void tick(ServerLevel level, LivingEntity entity, long now, int intervalTicks) {
        if (EntityIgnoreFilter.shouldSkip(entity)) return;

        EntityRadiationData data = entity.getData(NRAttachments.ENTITY_RADIATION.get());

        MedicineEffectsApplier.apply(entity, data, intervalTicks);

        double gyPerBqSec = RadiationConfig.GY_PER_BQ_SECOND.get();
        double intervalSeconds = intervalTicks * Units.SECONDS_PER_TICK;

        ArmorProtectionRegistry.Protection armor = ArmorProtectionRegistry.summed(entity);

        ExternalResult ext = computeExternal(level, entity, gyPerBqSec, intervalSeconds, armor);
        double svInternal = computeInternal(data, gyPerBqSec, intervalSeconds, now);
        Dose inventory = computeInventory(entity, gyPerBqSec, intervalSeconds, now, armor);
        Dose nearby = computeNearbyEntities(level, entity, gyPerBqSec, intervalSeconds, armor, now);
        Dose contamination = computeContamination(level, entity, gyPerBqSec, intervalSeconds, armor);
        Dose background = backgroundRadiation(level, entity, intervalSeconds, armor);

        double protection = clamp01(data.protectionFactor());
        double doseScale = (1.0 - protection) * 72;

        double svThisTick = (ext.sv() + svInternal + inventory.sv() + nearby.sv()
                + contamination.sv() + background.sv()) * doseScale;
        // Geiger field reading: same dose ignoring worn armor.
        double svThisTickAmbient = (ext.svAmbient() + svInternal + inventory.svAmbient() + nearby.svAmbient()
                + contamination.svAmbient() + background.svAmbient()) * doseScale;

        data.addSv(svThisTick);

        double svPerHourInstant = (svThisTick / intervalSeconds) * Units.SECONDS_PER_HOUR;
        double prev = data.svPerHour();
        double rolling = prev + EMA_ALPHA * (svPerHourInstant - prev);
        data.setSvPerHour(rolling);

        double svPerHourInstantAmbient = (svThisTickAmbient / intervalSeconds) * Units.SECONDS_PER_HOUR;
        double prevAmbient = data.svPerHourAmbient();
        data.setSvPerHourAmbient(prevAmbient + EMA_ALPHA * (svPerHourInstantAmbient - prevAmbient));
        double mult = entity instanceof Player ? 0.01 : 0.001;
        double recovery = RadiationConfig.BASE_DECAY_SV_PER_HOUR.get()
                * data.decayMultiplier()
                * GeneralConfig.ENTITY_DECAY_MULTIPLIER.get()
                * 72D
                * mult
                * (intervalSeconds / Units.SECONDS_PER_HOUR);
        if (recovery > 0 && data.svTotalCareer() > 0) {
            double recoveryFactor = recovery / (recovery + svThisTick * 100000D);
            data.setSvTotalCareer(Math.max(0.0, data.svTotalCareer() - recovery * recoveryFactor));
        }

        int stage = RadiationEffects.computeStage(data.svPerHour(), data.svTotalCareer());
        boolean cancelled = false;
        if (stage > data.lastDoseStage()) {
            cancelled = NREvents.fireDosePhaseRise(entity, stage, data.svPerHour(), data.svTotalCareer());
        }
        data.setLastDoseStage(stage);
        if (!cancelled) {
            RadiationEffects.apply(entity, data.svPerHour(), data.svTotalCareer());
        }

        LungProcessor.tick(level, entity, data.decayMultiplier(), now, intervalTicks);

        MutationProcessor.tryMutate(level, entity, data);

        if (entity instanceof ServerPlayer player) {
            NRNetwork.sendTo(player, new RadiationSyncPayload(
                    data.svTotalCareer(), data.svPerHour(), data.svPerHourAmbient()));

            if (RadiationConfig.DEBUG_RADIATION_VECTORS.get()) {
                ChunkPos cp = entity.chunkPosition();
                int cy = entity.blockPosition().getY() >> 4;
                SubChunkRadVector vec = RadiationSimulator.get().getChunkVector(level, cp, cy);
                if (vec != null) {
                    NRNetwork.sendTo(player, ChunkVectorDebugPayload.of(cp.x, cy, cp.z, vec));
                }
                sendChunkContamination(level, player, cp);
            }
        }
    }

    private static final int CONTAMINATION_RADIUS = 2;

    private static void sendChunkContamination(ServerLevel level, ServerPlayer player, ChunkPos center) {
        int side = CONTAMINATION_RADIUS * 2 + 1;
        int max = side * side;
        int[] cxs = new int[max];
        int[] czs = new int[max];
        double[] air = new double[max];
        double[] water = new double[max];
        double[] soil = new double[max];
        int n = 0;
        for (int dx = -CONTAMINATION_RADIUS; dx <= CONTAMINATION_RADIUS; dx++) {
            for (int dz = -CONTAMINATION_RADIUS; dz <= CONTAMINATION_RADIUS; dz++) {
                int x = center.x + dx;
                int z = center.z + dz;
                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) continue;
                ChunkRadiationData data = chunk.getData(NRAttachments.CHUNK_RADIATION.get());
                double a = data.air().totalActivityBq();
                double w = data.water().totalActivityBq();
                double s = data.soil().totalActivityBq();
                if (a <= 0.0 && w <= 0.0 && s <= 0.0) continue;
                cxs[n] = x;
                czs[n] = z;
                air[n] = a;
                water[n] = w;
                soil[n] = s;
                n++;
            }
        }
        if (n == 0) return;
        int[] cxOut = new int[n];
        int[] czOut = new int[n];
        double[] airOut = new double[n];
        double[] waterOut = new double[n];
        double[] soilOut = new double[n];
        System.arraycopy(cxs, 0, cxOut, 0, n);
        System.arraycopy(czs, 0, czOut, 0, n);
        System.arraycopy(air, 0, airOut, 0, n);
        System.arraycopy(water, 0, waterOut, 0, n);
        System.arraycopy(soil, 0, soilOut, 0, n);
        NRNetwork.sendTo(player, new ChunkContaminationDebugPayload(cxOut, czOut, airOut, waterOut, soilOut));
    }

    private static final byte CHANNEL_XRAY = 0;
    private static final byte CHANNEL_NEUTRON = 1;

    private record RayHit(Vec3 endpoint, double xrayPass, double neutronPass) {}

    private record ExternalResult(double sv, double svAmbient, double xrayPass, double neutronPass) {}

    /** sv = armor-attenuated dose; svAmbient = same dose ignoring worn armor (geiger field reading). */
    private record Dose(double sv, double svAmbient) {}

    private static ExternalResult computeExternal(ServerLevel level, LivingEntity entity,
                                                  double gyPerBqSec, double intervalSeconds,
                                                  ArmorProtectionRegistry.Protection armor) {
        ChunkPos cp = entity.chunkPosition();
        int cy = entity.blockPosition().getY() >> 4;
        SubChunkRadVector vec = RadiationSimulator.get().getChunkVector(level, cp, cy);
        if (vec == null || vec.isEmpty()) return new ExternalResult(0.0, 0.0, 1.0, 1.0);

        Vec3 eye = entity.getEyePosition();
        boolean debug = RadiationConfig.DEBUG_RADIATION_VECTORS.get()
                && entity instanceof ServerPlayer;
        List<RayHit> hits = debug ? new ArrayList<>(SubChunkRadVector.DIR_COUNT) : null;

        double sv = 0.0;
        double svAmbient = 0.0;
        double sumXPass = 0.0, sumXBq = 0.0;
        double sumNPass = 0.0, sumNBq = 0.0;

        for (int d = 0; d < SubChunkRadVector.DIR_COUNT; d++) {
            double apexX = vec.xRayBq[d];
            double apexN = vec.neutronBq[d];
            if (apexX <= 0 && apexN <= 0) continue;

            Vec3 tip = vec.tip[d];
            double dxe = tip.x - eye.x;
            double dye = tip.y - eye.y;
            double dze = tip.z - eye.z;
            double tipEyeDist2 = dxe * dxe + dye * dye + dze * dze;
            double scale = (vec.tipApexDist2[d] + 1.0) / (tipEyeDist2 + 1.0);
            double bqX = apexX * scale;
            double bqN = apexN * scale;

            AttenuationResult att = ShieldingRaycast.cast(level, eye, tip);
            double xp = att.xrayPass();
            double np = att.neutronPass();

            double svX = bqX * gyPerBqSec * DEFAULT_Q.qXRay * intervalSeconds * xp;
            double svN = bqN * gyPerBqSec * DEFAULT_Q.qNeutron * intervalSeconds * np;
            svAmbient += svX + svN;
            sv += svX * (1.0 - armor.xray()) + svN * (1.0 - armor.neutron());

            sumXPass += xp * bqX; sumXBq += bqX;
            sumNPass += np * bqN; sumNBq += bqN;

            if (debug) hits.add(new RayHit(tip, xp, np));
        }

        if (debug && hits != null && !hits.isEmpty()) {
            sendRaysDebug((ServerPlayer) entity, eye, hits);
        }

        double xrayPass = sumXBq > 0 ? sumXPass / sumXBq : 1.0;
        double neutronPass = sumNBq > 0 ? sumNPass / sumNBq : 1.0;
        return new ExternalResult(sv, svAmbient, xrayPass, neutronPass);
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
        NRNetwork.sendTo(player,
                new ShieldingRaysDebugPayload(origin.x, origin.y, origin.z, ex, ey, ez, pv, ch));
    }

    private static Dose computeContamination(ServerLevel level, LivingEntity entity,
                                               double gyPerBqSec, double intervalSeconds,
                                               ArmorProtectionRegistry.Protection armor) {
        ChunkPos cp = entity.chunkPosition();
        LevelChunk chunk = level.getChunkSource().getChunkNow(cp.x, cp.z);
        if (chunk == null) return new Dose(0.0, 0.0);
        ChunkRadiationData data = chunk.getData(NRAttachments.CHUNK_RADIATION.get());

        double bqX = data.air().xRayActivityBq() + data.soil().xRayActivityBq();
        double bqN = data.air().neutronActivityBq() + data.soil().neutronActivityBq();
        double bA = data.air().alphaActivityBq();
        double bB = data.air().betaActivityBq();
        if(entity.isInWater()) {
            bqX *= 0.5d;
            bqN *= 0.5d;
            bqX += data.water().xRayActivityBq();
            bqN += data.water().neutronActivityBq();
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                LevelChunk neighbor = level.getChunkSource().getChunkNow(cp.x + dx, cp.z + dz);
                if (neighbor == null) continue;
                ChunkRadiationData nd = neighbor.getData(NRAttachments.CHUNK_RADIATION.get());
                if (nd.air().isEmpty()) continue;
                bqX += (nd.air().xRayActivityBq() + nd.soil().xRayActivityBq()) * 0.5;
                bqN += (nd.air().neutronActivityBq() + nd.soil().neutronActivityBq()) * 0.5;
                bA += nd.air().alphaActivityBq() * 0.5;
                bB += nd.air().betaActivityBq() * 0.5;
            }
        }
        double svX = bqX * gyPerBqSec * DEFAULT_Q.qXRay * intervalSeconds;
        double svN = bqN * gyPerBqSec * DEFAULT_Q.qNeutron * intervalSeconds;
        double svA = bA * gyPerBqSec * DEFAULT_Q.qAlpha * intervalSeconds;
        double svB = bB * gyPerBqSec * DEFAULT_Q.qBeta * intervalSeconds;
        double svAmbient = (svX + svN + svA + svB) * 10;
        double sv = (svX * (1.0 - armor.xray())
                + svN * (1.0 - armor.neutron())
                + svA * (1.0 - armor.alpha())
                + svB * (1.0 - armor.beta())) * 10;
        return new Dose(sv, svAmbient);
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

    private static Dose computeInventory(LivingEntity entity, double gyPerBqSec,
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
        double baseXRay = cache.svXRayPerSecPerGyBq()    * gyPerBqSec * intervalSeconds;
        double baseAlpha = cache.svAlphaPerSecPerGyBq()  * gyPerBqSec * intervalSeconds;
        double baseBeta = cache.svBetaPerSecPerGyBq()    * gyPerBqSec * intervalSeconds;
        double baseN    = cache.svNeutronPerSecPerGyBq() * gyPerBqSec * intervalSeconds;
        double sv = baseXRay * xRayMul + baseAlpha * alphaMul + baseBeta * betaMul + baseN * neutronMul;
        double svAmbient = baseXRay + baseAlpha * inventoryAlphaPass + baseBeta * inventoryBetaPass + baseN;
        return new Dose(sv, svAmbient);
    }

    private static Dose computeNearbyEntities(ServerLevel level, LivingEntity self,
                                                double gyPerBqSec, double intervalSeconds,
                                                ArmorProtectionRegistry.Protection armor,
                                                long now) {
        double radius = RadiationConfig.MAX_SOURCE_RADIUS_M.get();
        AABB box = self.getBoundingBox().inflate(radius);
        Vec3 selfEye = self.getEyePosition();
        double xrayMul = 1.0 - armor.xray();
        double neutronMul = 1.0 - armor.neutron();
        double r2 = radius * radius;
        double sv = 0.0;
        double svAmbient = 0.0;
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (other == self) continue;
            if (EntityIgnoreFilter.shouldSkip(other)) continue;
            InventoryRadCache cache = InventoryRadCache.get(other);
            cache.rescan(other, now);
            double bqX = cache.bqXRay();
            double bqN = cache.bqNeutron();
            if (bqX <= 0.0 && bqN <= 0.0) continue;
            Vec3 otherPos = other.getEyePosition();
            double dx = otherPos.x - selfEye.x;
            double dy = otherPos.y - selfEye.y;
            double dz = otherPos.z - selfEye.z;
            double d2 = dx * dx + dy * dy + dz * dz;
            if (d2 > r2) continue;
            double falloff = 1.0 / (d2 + 1.0);
            AttenuationResult att = ShieldingRaycast.cast(level, selfEye, otherPos);
            double svX = bqX * falloff * gyPerBqSec * DEFAULT_Q.qXRay * intervalSeconds * att.xrayPass();
            double svN = bqN * falloff * gyPerBqSec * DEFAULT_Q.qNeutron * intervalSeconds * att.neutronPass();
            svAmbient += svX + svN;
            sv += svX * xrayMul + svN * neutronMul;
        }
        return new Dose(sv, svAmbient);
    }

    public static Dose backgroundRadiation(ServerLevel level, LivingEntity entity, double intervalSeconds, ArmorProtectionRegistry.Protection armor) {
        ResourceLocation biomeKey = level.getBiome(entity.blockPosition())
                .unwrapKey()
                .map(ResourceKey::location)
                .orElse(null);
        Double uSvPerHour = RadiationConfig.biomeBackgroundUSvPerHour(biomeKey);
        if (uSvPerHour == null) {
            uSvPerHour = RadiationConfig.levelBackgroundUSvPerHour(level.dimension().location());
        }
        if (uSvPerHour == null) {
            uSvPerHour = RadiationConfig.DEFAULT_BACKGROUND_USV_PER_HOUR.get();
        }
        if (uSvPerHour <= 0.0) return new Dose(0.0, 0.0);
        double svPerHour = uSvPerHour * 1.0e-6;
        double svAmbient = svPerHour * (intervalSeconds / Units.SECONDS_PER_HOUR);
        return new Dose(svAmbient * (1.0 - armor.xray()), svAmbient);
    }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
