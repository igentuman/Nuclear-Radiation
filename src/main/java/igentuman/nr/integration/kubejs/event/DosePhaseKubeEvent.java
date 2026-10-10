package igentuman.nr.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.world.entity.LivingEntity;

/**
 * {@code NRServerEvents.dosePhase} — fired once when a living entity's radiation dose stage rises
 * into a new phase (1 = mild, 2 = moderate, 3 = severe, 4 = lethal). Call {@code event.cancel()}
 * to suppress the mod's default harm effects for that tick.
 */
public class DosePhaseKubeEvent implements KubeEvent {

    private final LivingEntity entity;
    private final int phase;
    private final double svPerHour;
    private final double totalDoseSv;

    public DosePhaseKubeEvent(LivingEntity entity, int phase, double svPerHour, double totalDoseSv) {
        this.entity = entity;
        this.phase = phase;
        this.svPerHour = svPerHour;
        this.totalDoseSv = totalDoseSv;
    }

    /** The affected entity. */
    public LivingEntity getEntity() { return entity; }

    /** Dose phase just entered: 1 mild, 2 moderate, 3 severe, 4 lethal. */
    public int getPhase() { return phase; }

    /** Current dose rate in Sieverts per hour. */
    public double getSvPerHour() { return svPerHour; }

    /** Accumulated career dose in Sieverts. */
    public double getTotalDoseSv() { return totalDoseSv; }
}
