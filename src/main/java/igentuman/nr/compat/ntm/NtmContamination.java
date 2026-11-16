package igentuman.nr.compat.ntm;

import com.hbm.capability.HbmLivingProps;
import net.minecraft.world.entity.LivingEntity;

/**
 * Native timed contamination (EntityHbmProps.ContaminationEffect, e.g. from fallout) stays in NTM storage
 * and keeps ticking natively; each tick already enters NR dose once through the contaminate bridge.
 * NR treatments/clears additionally end it, as the native decontaminator does. Only reached when enabled.
 */
final class NtmContamination {
    private NtmContamination() {}

    static int clear(LivingEntity entity) {
        var effects = HbmLivingProps.getCont(entity);
        int removed = effects.size();
        effects.clear();
        return removed;
    }
}
