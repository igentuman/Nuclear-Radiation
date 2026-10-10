package igentuman.nr.api.isotope;

import igentuman.nr.api.DecayEdge;
import igentuman.nr.api.DecayGraph;
import igentuman.nr.api.RadiationQuality;

import java.util.ArrayList;
import java.util.List;

public class IsotopeBuilder {
    private String id;
    private float xray;
    private float alpha;
    private float beta;
    private float neutron;
    private long halfLife;
    private String decaysTo;
    private RadiationQuality quality = RadiationQuality.DEFAULT;
    private final List<DecayEdge> branches = new ArrayList<>();

    public static IsotopeBuilder create(String id) {
        return new IsotopeBuilder().id(id);
    }

    public IsotopeBuilder id(String id) { this.id = id; return this; }
    public IsotopeBuilder xray(float v) { this.xray = v; return this; }
    public IsotopeBuilder alpha(float v) { this.alpha = v; return this; }
    public IsotopeBuilder beta(float v) { this.beta = v; return this; }
    public IsotopeBuilder neutron(float v) { this.neutron = v; return this; }
    public IsotopeBuilder halfLife(long t) { this.halfLife = t; return this; }
    public IsotopeBuilder decaysTo(String isotopeId) {
        this.decaysTo = isotopeId;
        return this;
    }

    public IsotopeBuilder branch(String targetIsotopeId, double probability) {
        if (targetIsotopeId == null) throw new IllegalArgumentException("branch target null");
        if (probability <= 0.0) throw new IllegalArgumentException("branch probability must be > 0");
        this.branches.add(new DecayEdge(targetIsotopeId, probability));
        if (this.decaysTo == null) this.decaysTo = targetIsotopeId;
        return this;
    }

    public IsotopeBuilder quality(float qXRay, float qBeta, float qAlpha, float qNeutron) {
        this.quality = new RadiationQuality(qXRay, qBeta, qAlpha, qNeutron);
        return this;
    }

    public Isotope build() {
        if (id == null) throw new IllegalStateException("Isotope id required");
        return new IsotopeImpl(id, xray, alpha, beta, neutron, halfLife, decaysTo, quality);
    }

    public Isotope register() {
        Isotope iso = build();
        IsotopeRegistry.register(iso);
        if (branches.isEmpty() && decaysTo != null) {
            DecayGraph.addEdge(id, new DecayEdge(decaysTo, 1.0));
        } else {
            for (DecayEdge e : branches) DecayGraph.addEdge(id, e);
        }
        return iso;
    }
}
