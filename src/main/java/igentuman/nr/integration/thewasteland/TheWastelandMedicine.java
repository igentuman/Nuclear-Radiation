package igentuman.nr.integration.thewasteland;

import igentuman.nr.config.RadiationConfig;
import igentuman.nr.radiation.storage.EntityRadiationData;
import igentuman.nr.radiation.storage.NRAttachments;
import igentuman.nr.registry.NREffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** Keeps The Wasteland Reworked's medicines useful with Nuclear Radiation's dose store. */
public final class TheWastelandMedicine {
    private TheWastelandMedicine() {}

    public static void removeDose(Entity entity, double sv) {
        if (!RadiationConfig.masterEnabled() || entity == null || entity.level().isClientSide) return;
        EntityRadiationData data = entity.getData(NRAttachments.ENTITY_RADIATION.get());
        data.setSvTotalCareer(Math.max(0.0, data.svTotalCareer() - sv));
    }

    public static void protect(Entity entity, int amplifier) {
        if (!RadiationConfig.masterEnabled() || !(entity instanceof LivingEntity living)
                || entity.level().isClientSide) return;
        living.addEffect(new MobEffectInstance(NREffects.PROTECTION, 3600, amplifier, false, true));
    }
}
