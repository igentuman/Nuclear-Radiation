package igentuman.nr.compat.ntm;

import net.minecraft.world.entity.LivingEntity;

/** Implemented on NTM properties by a guarded mixin; contains no optional API types. */
public interface NtmEntityOwner {
    void nr$bind(LivingEntity entity);
    void nr$mirrorDose(double dose);
    void nr$mirrorRate(double rate);
}
