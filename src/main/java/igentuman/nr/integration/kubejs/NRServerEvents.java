package igentuman.nr.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import igentuman.nr.integration.kubejs.event.DosePhaseKubeEvent;

/**
 * KubeJS server event group {@code NRServerEvents}. Runtime events fired on the server.
 */
public interface NRServerEvents {

    EventGroup GROUP = EventGroup.of("NRServerEvents");

    EventHandler DOSE_PHASE = GROUP.server("dosePhase", () -> DosePhaseKubeEvent.class).hasResult();
}
