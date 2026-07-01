package igentuman.nr.api;

import igentuman.nr.registry.IsotopeRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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

    public double alphaActivityBq() {
        double sum = 0.0;
        for (IsotopeStack s : isotopes.values()) {
            sum += s.currentActivityBq() * s.isotope().alphaStrength();
        }
        return sum;
    }

    public double betaActivityBq() {
        double sum = 0.0;
        for (IsotopeStack s : isotopes.values()) {
            sum += s.currentActivityBq() * s.isotope().betaStrength();
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
        advanceDecay(currentTick, Double.NEGATIVE_INFINITY);
    }

    public long advanceDecay(long currentTick, double floorBq) {
        long maxExpiry = Long.MIN_VALUE;
        boolean computeExpiry = floorBq > Double.NEGATIVE_INFINITY;
        List<IsotopeStack> snapshot = new ArrayList<>(isotopes.values());
        Map<String, Double> ingrowth = null;
        for (IsotopeStack s : snapshot) {
            double decayed = s.advanceDecay(currentTick);
            if (computeExpiry && maxExpiry != Long.MAX_VALUE) {
                long e = s.expiryTick(floorBq);
                if (e == Long.MAX_VALUE) maxExpiry = Long.MAX_VALUE;
                else if (e > maxExpiry) maxExpiry = e;
            }
            if (decayed <= 0.0) continue;
            List<DecayEdge> edges = DecayGraph.outputs(s.isotope().id());
            if (edges.isEmpty()) continue;
            if (ingrowth == null) ingrowth = new HashMap<>();
            for (DecayEdge edge : edges) {
                if (edge.targetIsotopeId() == null) continue;
                ingrowth.merge(edge.targetIsotopeId(), decayed * edge.probability(), Double::sum);
            }
        }
        if (ingrowth != null) {
            for (Map.Entry<String, Double> e : ingrowth.entrySet()) {
                double add = e.getValue();
                if (add <= 0.0) continue;
                IsotopeStack daughter = isotopes.get(e.getKey());
                if (daughter == null) {
                    Isotope iso = IsotopeRegistry.get(e.getKey());
                    if (iso == null) continue;
                    daughter = new IsotopeStack(iso, add, currentTick);
                    isotopes.put(iso.id(), daughter);
                } else {
                    daughter.setAtoms(daughter.atoms() + add);
                }
                if (computeExpiry && maxExpiry != Long.MAX_VALUE) {
                    long de = daughter.expiryTick(floorBq);
                    if (de == Long.MAX_VALUE) maxExpiry = Long.MAX_VALUE;
                    else if (de > maxExpiry) maxExpiry = de;
                }
            }
        }
        return maxExpiry;
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

    public void reduceAtoms(double rate) {
        for (IsotopeStack s : isotopes.values()) {
            s.setAtoms(s.atoms() / rate);
        }
    }
}
