package igentuman.nr.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import igentuman.nr.integration.kubejs.event.ArmorKubeEvent;
import igentuman.nr.integration.kubejs.event.BindingKubeEvent;
import igentuman.nr.integration.kubejs.event.IsotopeKubeEvent;
import igentuman.nr.integration.kubejs.event.ShieldingKubeEvent;

/**
 * KubeJS startup event group {@code NRStartupEvents}. Handlers run once during the startup script
 * phase and define content (isotopes, bindings, shielding, armor) that persists across reloads.
 */
public interface NRStartupEvents {

    EventGroup GROUP = EventGroup.of("NRStartupEvents");

    EventHandler ISOTOPES = GROUP.startup("isotopes", () -> IsotopeKubeEvent.class);
    EventHandler BINDINGS = GROUP.startup("bindings", () -> BindingKubeEvent.class);
    EventHandler SHIELDING = GROUP.startup("shielding", () -> ShieldingKubeEvent.class);
    EventHandler ARMOR = GROUP.startup("armor", () -> ArmorKubeEvent.class);
}
