package igentuman.nr.entity;

import igentuman.nr.config.RadiationConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class RadiationEffects {

    private RadiationEffects() {}

    public static void apply(LivingEntity entity, double svPerHour) {
        double mild = RadiationConfig.THRESHOLD_MILD.get();
        double mod  = RadiationConfig.THRESHOLD_MODERATE.get();
        double sev  = RadiationConfig.THRESHOLD_SEVERE.get();
        double leth = RadiationConfig.THRESHOLD_LETHAL.get();

        if (svPerHour < mild) return;

        if (svPerHour >= mild) {
            addEffect(entity, MobEffects.CONFUSION, 100, 0);
        }
        if (svPerHour >= mod) {
            addEffect(entity, MobEffects.WEAKNESS, 100, 0);
        }
        if (svPerHour >= sev) {
            addEffect(entity, MobEffects.BLINDNESS, 60, 0);
            entity.hurt(entity.damageSources().magic(), 0.5f);
        }
        if (svPerHour >= leth) {
            addEffect(entity, MobEffects.DIG_SLOWDOWN, 200, 1);
            entity.hurt(entity.damageSources().magic(), 2.0f);
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
