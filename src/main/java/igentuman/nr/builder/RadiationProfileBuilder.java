package igentuman.nr.builder;

import igentuman.nr.api.Isotope;
import igentuman.nr.api.IsotopeStack;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.registry.IsotopeRegistry;

import java.util.LinkedHashMap;
import java.util.Map;

public class RadiationProfileBuilder {
    private final Map<String, Double> atomsByIsotope = new LinkedHashMap<>();
    private long timestamp = 0L;

    public static RadiationProfileBuilder create() {
        return new RadiationProfileBuilder();
    }

    public RadiationProfileBuilder timestamp(long tick) {
        this.timestamp = tick;
        return this;
    }

    public RadiationProfileBuilder isotope(String id, double atoms) {
        atomsByIsotope.merge(id, atoms, Double::sum);
        return this;
    }

    public RadiationProfileBuilder isotope(Isotope iso, double atoms) {
        return isotope(iso.id(), atoms);
    }

    public RadiationProfile build() {
        RadiationProfile profile = new RadiationProfile();
        for (Map.Entry<String, Double> e : atomsByIsotope.entrySet()) {
            Isotope iso = IsotopeRegistry.get(e.getKey());
            if (iso == null) continue;
            profile.put(new IsotopeStack(iso, e.getValue(), timestamp));
        }
        return profile;
    }
}
