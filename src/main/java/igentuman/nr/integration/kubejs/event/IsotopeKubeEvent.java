package igentuman.nr.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.KubeStartupEvent;
import igentuman.nr.integration.kubejs.NuclearRadiationKubeData;
import igentuman.nr.registry.IsotopeDefinition;

/**
 * {@code NRStartupEvents.isotopes} — register new isotopes or remove existing ones.
 * <p>Half-life helpers convert real time units to game ticks (20 ticks/second).
 */
public class IsotopeKubeEvent implements KubeStartupEvent {

    /** Registers (or overrides) an isotope. Returns a fluent builder; chain the decay properties. */
    public Builder add(String id) {
        IsotopeDefinition.Builder b = IsotopeDefinition.create(id);
        NuclearRadiationKubeData.ISOTOPE_ADDS.add(b);
        return new Builder(b);
    }

    /** Removes an isotope and its decay edges (applies after defaults + datapack each reload). */
    public void remove(String id) {
        NuclearRadiationKubeData.ISOTOPE_REMOVES.add(id);
    }

    public static final class Builder {
        private static final long TICKS_PER_YEAR = 631128000L;
        private static final long TICKS_PER_DAY = 24000L;
        private static final long TICKS_PER_HOUR = 1000L;

        private final IsotopeDefinition.Builder b;

        Builder(IsotopeDefinition.Builder b) {
            this.b = b;
        }

        /** Fraction of decays emitting x-ray/gamma (0..1). */
        public Builder xray(float v) { b.xray(v); return this; }
        /** Fraction of decays emitting alpha (0..1). */
        public Builder alpha(float v) { b.alpha(v); return this; }
        /** Fraction of decays emitting beta (0..1). */
        public Builder beta(float v) { b.beta(v); return this; }
        /** Fraction of decays emitting neutrons (0..1). */
        public Builder neutron(float v) { b.neutron(v); return this; }

        public Builder halfLife(long ticks) { b.halfLife(ticks); return this; }
        public Builder halfLifeSeconds(double seconds) { b.halfLife(saturate(seconds * 20.0)); return this; }
        public Builder halfLifeHours(double hours) { b.halfLife(saturate(hours * TICKS_PER_HOUR)); return this; }
        public Builder halfLifeDays(double days) { b.halfLife(saturate(days * TICKS_PER_DAY)); return this; }
        public Builder halfLifeYears(double years) { b.halfLife(saturate(years * TICKS_PER_YEAR)); return this; }

        /** Radiation quality factors (Sv/Gy) for x-ray, beta, alpha, neutron. Defaults 1/1/20/10. */
        public Builder quality(float qXray, float qBeta, float qAlpha, float qNeutron) {
            b.quality(qXray, qBeta, qAlpha, qNeutron);
            return this;
        }

        /** Adds a decay branch to a daughter isotope with the given probability (0..1]. */
        public Builder branch(String targetIsotopeId, double probability) {
            b.branch(targetIsotopeId, probability);
            return this;
        }

        /** Single 100% decay branch to the given daughter isotope. */
        public Builder decaysTo(String targetIsotopeId) {
            b.branch(targetIsotopeId, 1.0);
            return this;
        }

        private static long saturate(double v) {
            if (v >= (double) Long.MAX_VALUE) return Long.MAX_VALUE;
            if (v <= 1.0) return 1L;
            return (long) v;
        }
    }
}
