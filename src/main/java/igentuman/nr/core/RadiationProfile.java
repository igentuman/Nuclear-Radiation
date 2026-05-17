package igentuman.nr.core;

import igentuman.nr.api.Isotope;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class RadiationProfile {
    private final Map<String, IsotopeStack> isotopes;

    public RadiationProfile() {
        this(new LinkedHashMap<>());
    }

    public RadiationProfile(Map<String, IsotopeStack> isotopes) {
        this.isotopes = isotopes;
    }

    public static RadiationProfile empty() {
        return new RadiationProfile();
    }

    public Map<String, IsotopeStack> isotopes() {
        return Collections.unmodifiableMap(isotopes);
    }

    public void put(IsotopeStack stack) {
        isotopes.put(stack.isotope().id(), stack);
    }

    public IsotopeStack get(String id) {
        return isotopes.get(id);
    }

    public IsotopeStack get(Isotope isotope) {
        return isotopes.get(isotope.id());
    }

    public boolean isEmpty() {
        return isotopes.isEmpty();
    }

    public Collection<IsotopeStack> stacks() {
        return isotopes.values();
    }

    public double totalActivityBq() {
        double sum = 0.0;
        for (IsotopeStack s : isotopes.values()) sum += s.currentActivityBq();
        return sum;
    }

    public double xRayActivityBq() {
        double sum = 0.0;
        for (IsotopeStack s : isotopes.values()) {
            sum += s.currentActivityBq() * s.isotope().xRayStrength();
        }
        return sum;
    }

    public double alphaBetaActivityBq() {
        double sum = 0.0;
        for (IsotopeStack s : isotopes.values()) {
            sum += s.currentActivityBq() * s.isotope().alphaBetaStrength();
        }
        return sum;
    }

    public double neutronActivityBq() {
        double sum = 0.0;
        for (IsotopeStack s : isotopes.values()) {
            sum += s.currentActivityBq() * s.isotope().neutronStrength();
        }
        return sum;
    }

    public void advanceDecay(long currentTick) {
        for (IsotopeStack s : isotopes.values()) s.advanceDecay(currentTick);
    }

    public long expiryTick(double floorBq) {
        if (isotopes.isEmpty()) return Long.MIN_VALUE;
        long max = Long.MIN_VALUE;
        for (IsotopeStack s : isotopes.values()) {
            long e = s.expiryTick(floorBq);
            if (e == Long.MAX_VALUE) return Long.MAX_VALUE;
            if (e > max) max = e;
        }
        return max;
    }

    public void mergeAtoms(RadiationProfile other, double scale, long timestamp) {
        for (IsotopeStack s : other.isotopes.values()) {
            IsotopeStack existing = isotopes.get(s.isotope().id());
            if (existing == null) {
                isotopes.put(s.isotope().id(),
                        new IsotopeStack(s.isotope(), s.atoms() * scale, timestamp));
            } else {
                existing.advanceDecay(timestamp);
                existing.setAtoms(existing.atoms() + s.atoms() * scale);
            }
        }
    }

    public RadiationProfile copy(long timestamp) {
        Map<String, IsotopeStack> copy = new LinkedHashMap<>();
        for (IsotopeStack s : isotopes.values()) {
            copy.put(s.isotope().id(), new IsotopeStack(s.isotope(), s.atoms(), timestamp));
        }
        return new RadiationProfile(copy);
    }
}
