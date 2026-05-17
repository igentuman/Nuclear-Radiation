package igentuman.nr.builder;

import igentuman.nr.api.Isotope;
import igentuman.nr.core.IsotopeImpl;
import igentuman.nr.core.RadiationQuality;
import igentuman.nr.registry.IsotopeRegistry;

public class IsotopeBuilder {
    private String id;
    private float xray;
    private float alphaBeta;
    private float neutron;
    private long halfLife;
    private String decaysTo;
    private RadiationQuality quality = RadiationQuality.DEFAULT;

    public static IsotopeBuilder create(String id) {
        return new IsotopeBuilder().id(id);
    }

    public IsotopeBuilder id(String id) { this.id = id; return this; }
    public IsotopeBuilder xray(float v) { this.xray = v; return this; }
    public IsotopeBuilder alphaBeta(float v) { this.alphaBeta = v; return this; }
    public IsotopeBuilder neutron(float v) { this.neutron = v; return this; }
    public IsotopeBuilder halfLife(long t) { this.halfLife = t; return this; }
    public IsotopeBuilder decaysTo(String isotopeId) { this.decaysTo = isotopeId; return this; }

    public IsotopeBuilder quality(float qXRay, float qBeta, float qAlpha, float qNeutron) {
        this.quality = new RadiationQuality(qXRay, qBeta, qAlpha, qNeutron);
        return this;
    }

    public Isotope build() {
        if (id == null) throw new IllegalStateException("Isotope id required");
        return new IsotopeImpl(id, xray, alphaBeta, neutron, halfLife, decaysTo, quality);
    }

    public Isotope register() {
        Isotope iso = build();
        IsotopeRegistry.register(iso);
        return iso;
    }
}
