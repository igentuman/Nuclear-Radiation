package igentuman.nr.entity;

import igentuman.nr.medicine.NREffects;
import igentuman.nr.medicine.effects.IsotopeSpecificProtectionEffect;
import igentuman.nr.util.persistence.EntityRadiationData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public final class MedicineEffectsApplier {

    private MedicineEffectsApplier() {}

    public static void apply(LivingEntity entity, EntityRadiationData data, int intervalTicks) {
        double protection = 0.0;
        double decay = 1.0;

        MobEffectInstance prot = entity.getEffect(NREffects.PROTECTION);
        if (prot != null) protection = Math.min(0.95, 0.25 + 0.15 * prot.getAmplifier());

        MobEffectInstance purge = entity.getEffect(NREffects.PURGE);
        if (purge != null) decay = 1.0 + 0.5 * (purge.getAmplifier() + 1);

        data.setProtectionFactor(protection);
        data.setDecayMultiplier(decay);

        applyIsotopeSpecific(entity, data, NREffects.IODINE_PROTECTION, intervalTicks);
        applyIsotopeSpecific(entity, data, NREffects.CESIUM_PURGE, intervalTicks);
    }

    private static void applyIsotopeSpecific(LivingEntity entity, EntityRadiationData data,
                                             net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.effect.MobEffect, IsotopeSpecificProtectionEffect> holder,
                                             int intervalTicks) {
        if (!holder.isBound()) return;
        MobEffectInstance inst = entity.getEffect(holder);
        if (inst == null) return;
        String targetId = holder.get().targetIsotopeId();
        Map<String, Double> internal = data.internalContamination();
        Double atoms = internal.get(targetId);
        if (atoms == null) return;
        double drainPerTick = 0.02 * (inst.getAmplifier() + 1);
        double remaining = atoms * (1.0 - Math.min(0.9, drainPerTick * intervalTicks));
        if (remaining < 1.0) internal.remove(targetId);
        else internal.put(targetId, remaining);
    }
}
