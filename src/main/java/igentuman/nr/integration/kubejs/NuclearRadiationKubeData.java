package igentuman.nr.integration.kubejs;

import igentuman.nr.api.DecayGraph;
import igentuman.nr.api.IChunkRadiation;
import igentuman.nr.events.NREvents;
import igentuman.nr.registry.DefaultIsotopes;
import igentuman.nr.api.isotope.IsotopeRegistry;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Overlay store for KubeJS-contributed content. Startup scripts populate these lists once (during
 * the KubeJS startup phase); the {@code apply*} methods are registered as {@link NREvents} reload
 * hooks so the contributions are re-installed after every datapack reload — otherwise the
 * clear-and-rebuild reload cycle in the mod's reload listeners would wipe them.
 *
 * <p>Precedence per reload: built-in defaults → datapack JSON → KubeJS additions → KubeJS removals.
 */
public final class NuclearRadiationKubeData {

    private NuclearRadiationKubeData() {}

    public static final List<IChunkRadiation.IsotopeDefinition.Builder> ISOTOPE_ADDS = new ArrayList<>();
    public static final Set<String> ISOTOPE_REMOVES = new LinkedHashSet<>();

    // Deferred registry mutations captured in script call order; replayed on each reload.
    public static final List<Runnable> BINDING_OPS = new ArrayList<>();
    public static final List<Runnable> SHIELDING_OPS = new ArrayList<>();
    public static final List<Runnable> ARMOR_OPS = new ArrayList<>();

    private static boolean hooksRegistered = false;

    public static void registerHooks() {
        if (hooksRegistered) return;
        hooksRegistered = true;
        NREvents.AFTER_ISOTOPES_RELOAD.add(NuclearRadiationKubeData::applyIsotopes);
        NREvents.AFTER_BINDINGS_RELOAD.add(NuclearRadiationKubeData::applyBindings);
        NREvents.AFTER_SHIELDING_RELOAD.add(NuclearRadiationKubeData::applyShielding);
        NREvents.AFTER_ARMOR_RELOAD.add(NuclearRadiationKubeData::applyArmor);
    }

    public static void clearAll() {
        ISOTOPE_ADDS.clear();
        ISOTOPE_REMOVES.clear();
        BINDING_OPS.clear();
        SHIELDING_OPS.clear();
        ARMOR_OPS.clear();
    }

    static void applyIsotopes() {
        for (IChunkRadiation.IsotopeDefinition.Builder b : ISOTOPE_ADDS) {
            DefaultIsotopes.apply(b.build());
        }
        for (String id : ISOTOPE_REMOVES) {
            IsotopeRegistry.remove(id);
            DecayGraph.remove(id);
        }
    }

    static void applyBindings() {
        for (Runnable r : BINDING_OPS) r.run();
    }

    static void applyShielding() {
        for (Runnable r : SHIELDING_OPS) r.run();
    }

    static void applyArmor() {
        for (Runnable r : ARMOR_OPS) r.run();
    }
}
