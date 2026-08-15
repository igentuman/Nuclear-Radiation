package igentuman.nr.radiation.simulation.entity;

import igentuman.nr.registry.NRDamageTypes;
import igentuman.nr.registry.NRSounds;
import igentuman.nr.config.GeneralConfig;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.network.VomitPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public final class RadiationEffects {

    private RadiationEffects() {}

    /**
     * Dose stage (radiation "phase") for the given rates, 0 = below the mild threshold up to
     * 4 = lethal. Factors in accumulated career dose the same way {@link #apply} does, so callers
     * (e.g. the KubeJS dose-phase event) see the exact stage that drives harm effects.
     */
    public static int computeStage(double svPerHour, double svTotalCareer) {
        // Acute: current dose rate (Sv/h) vs rate thresholds.
        int acute = band(svPerHour,
                RadiationConfig.THRESHOLD_MILD.get(),
                RadiationConfig.THRESHOLD_MODERATE.get(),
                RadiationConfig.THRESHOLD_SEVERE.get(),
                RadiationConfig.THRESHOLD_LETHAL.get());
        // Chronic: accumulated career dose (real Sv) vs LD50-anchored bands.
        int chronic = band(svTotalCareer,
                RadiationConfig.CAREER_MILD.get(),
                RadiationConfig.CAREER_MODERATE.get(),
                RadiationConfig.CAREER_SEVERE.get(),
                RadiationConfig.CAREER_LETHAL.get());
        return Math.max(acute, chronic);
    }

    /** 0..4 band for {@code v} against ascending thresholds (mild, moderate, severe, lethal). */
    private static int band(double v, double mild, double mod, double sev, double leth) {
        if (v >= leth) return 4;
        if (v >= sev) return 3;
        if (v >= mod) return 2;
        if (v >= mild) return 1;
        return 0;
    }

    public static void apply(LivingEntity entity, double svPerHour, double svTotalCareer) {
        if (EntityIgnoreFilter.shouldSkipHarm(entity)) return;
        boolean isPlayer = entity instanceof Player;
        if (isPlayer ? !GeneralConfig.RADIATION_HARM_EFFECTS_PLAYERS.get()
                     : !GeneralConfig.RADIATION_HARM_EFFECTS_MOBS.get()) return;

        int stage = computeStage(svPerHour, svTotalCareer);
        if (stage <= 0) return;
        if (!(entity.level() instanceof ServerLevel server)) return;
        DamageSource radiation = NRDamageTypes.source(server, NRDamageTypes.RADIATION);

        addEffect(entity, MobEffects.WEAKNESS, 2000, 0);
        addEffect(entity, MobEffects.UNLUCK, 2000, 0);
        addEffect(entity, MobEffects.DIG_SLOWDOWN, 2000, 0);

        if (stage >= 2) {
            addEffect(entity, MobEffects.WEAKNESS, 2000, 1);
            addEffect(entity, MobEffects.CONFUSION, 2000, 0);
            addEffect(entity, MobEffects.DIG_SLOWDOWN, 2000, 1);
            addEffect(entity, MobEffects.MOVEMENT_SLOWDOWN, 2000, 1);
        }
        if (stage >= 3) {
            addEffect(entity, MobEffects.BLINDNESS, 2000, 0);
            entity.hurt(radiation, 1.5f);
        }
        if (stage >= 4) {
            addEffect(entity, MobEffects.WITHER, 10, 0);
            entity.hurt(radiation, 20.0f);
        }

        triggerVomit(entity, stage);
    }

    private static final String VOMIT_COOLDOWN_KEY = "nr_next_vomit_tick";
    private static final int VOMIT_INTERVAL_STAGE_1 = 6000;
    private static final int VOMIT_INTERVAL_STAGE_2 = 3000;
    private static final int VOMIT_INTERVAL_STAGE_3_4 = 1000;

    private static void addEffect(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
                                  int duration, int amplifier) {
        entity.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, false));
    }

    private static void triggerVomit(LivingEntity entity, int stage) {
        Level level = entity.level();
        if (!(level instanceof ServerLevel server)) return;

        long now = server.getGameTime();
        CompoundTag data = entity.getPersistentData();

        long next = data.getLong(VOMIT_COOLDOWN_KEY);
        if (now < next) return;

        int interval = switch (stage) {
            case 1 -> VOMIT_INTERVAL_STAGE_1;
            case 2 -> VOMIT_INTERVAL_STAGE_2;
            default -> VOMIT_INTERVAL_STAGE_3_4;
        };
        data.putLong(VOMIT_COOLDOWN_KEY, now + interval);

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new VomitPayload(entity.getId(), stage));
        server.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                NRSounds.VOMIT.get(), entity.getSoundSource(),
                1.0f, 0.9f + entity.getRandom().nextFloat() * 0.2f);
    }


    @SuppressWarnings("unused")
    private static DamageSource source(LivingEntity entity) {
        return entity.damageSources().magic();
    }
}
