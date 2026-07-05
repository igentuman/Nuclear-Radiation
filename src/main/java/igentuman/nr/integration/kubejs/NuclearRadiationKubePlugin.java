package igentuman.nr.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptType;
import igentuman.nr.api.NREvents;
import igentuman.nr.integration.kubejs.event.ArmorKubeEvent;
import igentuman.nr.integration.kubejs.event.BindingKubeEvent;
import igentuman.nr.integration.kubejs.event.DosePhaseKubeEvent;
import igentuman.nr.integration.kubejs.event.IsotopeKubeEvent;
import igentuman.nr.integration.kubejs.event.ShieldingKubeEvent;

/**
 * KubeJS plugin entry point for Nuclear Radiation. Registered via {@code kubejs.plugins.txt}.
 *
 * <ul>
 *   <li>Exposes {@code NRStartupEvents} (isotopes / bindings / shielding / armor) and
 *   {@code NRServerEvents} (dosePhase).</li>
 *   <li>Registers recipe schemas for {@code mutation} and {@code block_irradiation}.</li>
 *   <li>Bridges the runtime dose-phase rise hook to the {@code NRServerEvents.dosePhase} script event.</li>
 * </ul>
 */
public class NuclearRadiationKubePlugin implements KubeJSPlugin {

    @Override
    public void init() {
        NuclearRadiationKubeData.registerHooks();
        NREvents.addDosePhaseListener((entity, stage, svPerHour, totalDose) -> {
            EventResult result = NRServerEvents.DOSE_PHASE.post(ScriptType.SERVER,
                    new DosePhaseKubeEvent(entity, stage, svPerHour, totalDose));
            return result.interruptFalse();
        });
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(NRStartupEvents.GROUP);
        registry.register(NRServerEvents.GROUP);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("NRServerUtils", NRServerUtils.class);
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        NRRecipeSchemas.register(registry);
    }

    @Override
    public void initStartup() {
        // Startup scripts have been loaded; posting now runs their listeners and fills the overlay store.
        NuclearRadiationKubeData.clearAll();
        NRStartupEvents.ISOTOPES.post(new IsotopeKubeEvent());
        NRStartupEvents.BINDINGS.post(new BindingKubeEvent());
        NRStartupEvents.SHIELDING.post(new ShieldingKubeEvent());
        NRStartupEvents.ARMOR.post(new ArmorKubeEvent());
    }
}
