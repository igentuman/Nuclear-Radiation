package igentuman.nr.registry;

import igentuman.nr.api.DecayEdge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class IsotopeDefinition {

    public final String id;
    public final float xray;
    public final float alpha;
    public final float beta;
    public final float neutron;
    public final long halfLifeTicks;
    public final float qXray;
    public final float qBeta;
    public final float qAlpha;
    public final float qNeutron;
    public final List<DecayEdge> branches;

    private IsotopeDefinition(Builder b) {
        this.id = b.id;
        this.xray = b.xray;
        this.alpha = b.alpha;
        this.beta = b.beta;
        this.neutron = b.neutron;
        this.halfLifeTicks = b.halfLifeTicks;
        this.qXray = b.qXray;
        this.qBeta = b.qBeta;
        this.qAlpha = b.qAlpha;
        this.qNeutron = b.qNeutron;
        this.branches = Collections.unmodifiableList(new ArrayList<>(b.branches));
    }

    public String namespace() {
        int i = id.indexOf(':');
        return i < 0 ? "minecraft" : id.substring(0, i);
    }

    public String path() {
        int i = id.indexOf(':');
        return i < 0 ? id : id.substring(i + 1);
    }

    public static Builder create(String id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final String id;
        private float xray;
        private float alpha;
        private float beta;
        private float neutron;
        private long halfLifeTicks;
        private float qXray = 1.0f;
        private float qBeta = 1.0f;
        private float qAlpha = 20.0f;
        private float qNeutron = 10.0f;
        private final List<DecayEdge> branches = new ArrayList<>();

        private Builder(String id) {
            if (id == null) throw new IllegalArgumentException("isotope id null");
            this.id = id;
        }

        public Builder xray(float v) { this.xray = v; return this; }
        public Builder alpha(float v) { this.alpha = v; return this; }
        public Builder beta(float v) { this.beta = v; return this; }
        public Builder neutron(float v) { this.neutron = v; return this; }
        public Builder halfLife(long ticks) { this.halfLifeTicks = ticks; return this; }

        public Builder quality(float qXray, float qBeta, float qAlpha, float qNeutron) {
            this.qXray = qXray;
            this.qBeta = qBeta;
            this.qAlpha = qAlpha;
            this.qNeutron = qNeutron;
            return this;
        }

        public Builder branch(String targetIsotopeId, double probability) {
            if (targetIsotopeId == null) throw new IllegalArgumentException("branch target null");
            if (probability <= 0.0) throw new IllegalArgumentException("branch probability must be > 0");
            this.branches.add(new DecayEdge(targetIsotopeId, probability));
            return this;
        }

        public IsotopeDefinition build() {
            return new IsotopeDefinition(this);
        }
    }
}
