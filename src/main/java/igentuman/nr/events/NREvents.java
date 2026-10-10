package igentuman.nr.events;

import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Neutral integration bridge. Holds hooks that fire from core code but are implemented by
 * optional integrations (currently KubeJS). No integration types leak into core: cores call
 * these static methods, integrations register plain-Java callbacks. Safe when KubeJS is absent
 * (the lists simply stay empty).
 *
 * <ul>
 *   <li><b>Reload hooks</b> — run at the end of each datapack reload listener, after defaults +
 *   datapack JSON have been (re)installed, so integrations can re-overlay their contributions
 *   (which would otherwise be wiped by the clear-and-rebuild reload cycle).</li>
 *   <li><b>Dose phase listener</b> — fired on the rising edge of an entity's radiation dose stage
 *   (1..4). Returning {@code true} cancels the mod's own harm effects for that tick.</li>
 * </ul>
 */
public final class NREvents {

    private NREvents() {}

    public static final List<Runnable> AFTER_ISOTOPES_RELOAD = new CopyOnWriteArrayList<>();
    public static final List<Runnable> AFTER_BINDINGS_RELOAD = new CopyOnWriteArrayList<>();
    public static final List<Runnable> AFTER_SHIELDING_RELOAD = new CopyOnWriteArrayList<>();
    public static final List<Runnable> AFTER_ARMOR_RELOAD = new CopyOnWriteArrayList<>();

    public static void runAfterIsotopesReload() { run(AFTER_ISOTOPES_RELOAD); }
    public static void runAfterBindingsReload() { run(AFTER_BINDINGS_RELOAD); }
    public static void runAfterShieldingReload() { run(AFTER_SHIELDING_RELOAD); }
    public static void runAfterArmorReload() { run(AFTER_ARMOR_RELOAD); }

    private static void run(List<Runnable> hooks) {
        for (Runnable r : hooks) r.run();
    }

    @FunctionalInterface
    public interface DosePhaseListener {
        /** @return true to cancel the mod's default harm effects for this tick. */
        boolean onRise(LivingEntity entity, int stage, double svPerHour, double svTotalCareer);
    }

    private static final List<DosePhaseListener> DOSE_PHASE_LISTENERS = new CopyOnWriteArrayList<>();

    public static void addDosePhaseListener(DosePhaseListener listener) {
        DOSE_PHASE_LISTENERS.add(listener);
    }

    /** @return true if any listener cancelled the default harm for this tick. */
    public static boolean fireDosePhaseRise(LivingEntity entity, int stage,
                                            double svPerHour, double svTotalCareer) {
        boolean cancelled = false;
        for (DosePhaseListener l : DOSE_PHASE_LISTENERS) {
            if (l.onRise(entity, stage, svPerHour, svTotalCareer)) cancelled = true;
        }
        return cancelled;
    }
}
