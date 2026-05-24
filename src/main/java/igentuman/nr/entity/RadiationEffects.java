package igentuman.nr.entity;

import igentuman.nr.config.GeneralConfig;
import igentuman.nr.config.RadiationConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class RadiationEffects {

    private RadiationEffects() {}

    public static void apply(LivingEntity entity, double svPerHour, double svTotalCareer) {
        boolean isPlayer = entity instanceof Player;
        if (isPlayer ? !GeneralConfig.RADIATION_HARM_EFFECTS_PLAYERS.get()
                     : !GeneralConfig.RADIATION_HARM_EFFECTS_MOBS.get()) return;
        double mild = RadiationConfig.THRESHOLD_MILD.get();
        double mod  = RadiationConfig.THRESHOLD_MODERATE.get();
        double sev  = RadiationConfig.THRESHOLD_SEVERE.get();
        double leth = RadiationConfig.THRESHOLD_LETHAL.get();
        double k = RadiationConfig.TOTAL_SV_SCALE_K.get();

        double ratio = svTotalCareer / k;
        double effectiveSvPerHour = svPerHour * (1.0 + ratio * ratio);

        if (effectiveSvPerHour < mild) return;

        if (effectiveSvPerHour >= mild) {
            addEffect(entity, MobEffects.CONFUSION, 100, 0);
            entity.hurt(entity.damageSources().magic(), 0.1f);
        }
        if (effectiveSvPerHour >= mod) {
            addEffect(entity, MobEffects.WEAKNESS, 100, 0);
            entity.hurt(entity.damageSources().magic(), 0.2f);
        }
        if (effectiveSvPerHour >= sev) {
            addEffect(entity, MobEffects.BLINDNESS, 60, 0);
            entity.hurt(entity.damageSources().magic(), 0.5f);
        }
        if (effectiveSvPerHour >= leth) {
            addEffect(entity, MobEffects.DIG_SLOWDOWN, 200, 1);
            entity.hurt(entity.damageSources().magic(), 20.0f);
        }
    }

    private static void addEffect(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
                                  int duration, int amplifier) {
        entity.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, false));
    }

    @SuppressWarnings("unused")
    private static DamageSource source(LivingEntity entity) {
        return entity.damageSources().magic();
    }
}
