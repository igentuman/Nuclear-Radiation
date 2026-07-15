package igentuman.nr.entity;

import igentuman.nr.NRDamageTypes;
import igentuman.nr.NRSounds;
import igentuman.nr.binding.RadiationTags;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.shielding.ArmorProtectionRegistry;
import igentuman.nr.util.persistence.LungPollutionData;
import igentuman.nr.util.persistence.NRAttachments;
import igentuman.nr.util.tracking.WorldSourceRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class LungProcessor {

    private LungProcessor() {}

    public static void tick(ServerLevel level, LivingEntity entity, double purgeMultiplier,
                            long now, int intervalTicks) {
        LungPollutionData lung = entity.getData(NRAttachments.LUNG_POLLUTION.get());

        boolean gasProtection = ArmorProtectionRegistry.summed(entity).protectsFromGas();

        double add = 0.0;
        if (!gasProtection) {
            if (WorldSourceRegistry.get(level).inGasCloud(entity.position())
                    && level.random.nextDouble() < RadiationConfig.GAS_INHALE_CHANCE.get()) {
                add += RadiationConfig.GAS_POLLUTION_PER_INHALE.get();
            }
            if (carriesAirborneContaminant(entity)) {
                add += RadiationConfig.LUNG_DUST_POLLUTION_PER_INTERVAL.get();
            }
        }

        double recovery = RadiationConfig.LUNG_RECOVERY_PER_INTERVAL.get() * Math.max(1.0, purgeMultiplier);
        lung.add(add - recovery);

        applyStageEffects(level, entity, lung.pollution(), now, intervalTicks);
    }

    private static final int COUGH_INTERVAL_MID = 800;
    private static final int COUGH_INTERVAL_HIGH = 400;

    private static void applyStageEffects(ServerLevel level, LivingEntity entity, double pollution, long now, int intervalTicks) {
        if (EntityIgnoreFilter.shouldSkipHarm(entity)) return;
        double mid = RadiationConfig.LUNG_MID_THRESHOLD.get();
        double high = RadiationConfig.LUNG_HIGH_THRESHOLD.get();
        if (pollution >= high) {
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2000, 1, true, false, false));
            int dmgInterval = RadiationConfig.LUNG_CANCER_DAMAGE_INTERVAL_TICKS.get();
            if (now % dmgInterval < intervalTicks) {
                entity.hurt(NRDamageTypes.source(level, NRDamageTypes.LUNG_CANCER),
                        RadiationConfig.LUNG_CANCER_DAMAGE.get().floatValue());
            }
            playCough(level, entity, now, intervalTicks, COUGH_INTERVAL_HIGH);
        } else if (pollution >= mid) {
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2000, 0, true, false, false));
            playCough(level, entity, now, intervalTicks, COUGH_INTERVAL_MID);
        }
    }

    private static void playCough(ServerLevel level, LivingEntity entity, long now, int intervalTicks, int coughInterval) {
        if (now % coughInterval >= intervalTicks) return;
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                NRSounds.COUGH.get(), entity.getSoundSource(),
                1.5f, 0.9f + entity.getRandom().nextFloat() * 0.2f);
    }

    private static boolean carriesAirborneContaminant(LivingEntity entity) {
        if (!(entity instanceof Player player)) return false;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(RadiationTags.ITEM_AIRBORNE_CONTAMINANT)) return true;
        }
        return player.getInventory().offhand.get(0).is(RadiationTags.ITEM_AIRBORNE_CONTAMINANT);
    }
}
