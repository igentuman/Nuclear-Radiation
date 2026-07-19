package igentuman.nr.api.isotope;

import igentuman.nr.api.isotope.Isotope;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class IsotopeRegistry {
    private static final Map<String, Isotope> ISOTOPES = new LinkedHashMap<>();

    private IsotopeRegistry() {}

    public static synchronized void register(Isotope isotope) {
        if (isotope == null || isotope.id() == null) {
            throw new IllegalArgumentException("Isotope or id null");
        }
        ISOTOPES.put(isotope.id(), isotope);
    }

    public static Isotope get(String id) {
        return ISOTOPES.get(id);
    }

    public static boolean contains(String id) {
        return ISOTOPES.containsKey(id);
    }

    public static synchronized void remove(String id) {
        ISOTOPES.remove(id);
    }

    public static Collection<Isotope> all() {
        return Collections.unmodifiableCollection(ISOTOPES.values());
    }

    public static synchronized void clear() {
        ISOTOPES.clear();
    }
}
