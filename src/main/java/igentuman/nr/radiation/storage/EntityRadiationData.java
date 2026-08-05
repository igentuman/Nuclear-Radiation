package igentuman.nr.radiation.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityRadiationData {

    public static final Codec<EntityRadiationData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.optionalFieldOf("sv_total", 0.0).forGetter(d -> d.svTotalCareer),
            Codec.DOUBLE.optionalFieldOf("sv_per_hour", 0.0).forGetter(d -> d.svPerHour),
            Codec.DOUBLE.optionalFieldOf("protection", 0.0).forGetter(d -> d.protectionFactor),
            Codec.DOUBLE.optionalFieldOf("decay_mult", 1.0).forGetter(d -> d.decayMultiplier),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("internal", new LinkedHashMap<>())
                    .forGetter(d -> d.internalContamination),
            Codec.STRING.listOf().optionalFieldOf("attempted_mutations", List.of())
                    .forGetter(d -> new ArrayList<>(d.attemptedMutations)),
            Codec.INT.optionalFieldOf("last_dose_stage", 0)
                    .forGetter(d -> d.lastDoseStage),
            Codec.DOUBLE.optionalFieldOf("sv_per_hour_ambient", 0.0)
                    .forGetter(d -> d.svPerHourAmbient)
    ).apply(inst, EntityRadiationData::new));

    private double svTotalCareer;
    private double svPerHour;
    private double svPerHourAmbient;
    private double protectionFactor;
    private double decayMultiplier;
    private Map<String, Double> internalContamination;
    private Set<String> attemptedMutations;
    private int lastDoseStage;

    public EntityRadiationData() {
        this(0.0, 0.0, 0.0, 1.0, new LinkedHashMap<>(), List.of(), 0, 0.0);
    }

    public EntityRadiationData(double svTotalCareer,
                               double svPerHour,
                               double protectionFactor,
                               double decayMultiplier,
                               Map<String, Double> internalContamination,
                               List<String> attemptedMutations,
                               int lastDoseStage,
                               double svPerHourAmbient) {
        this.svTotalCareer = sanitizeCareer(svTotalCareer);
        this.svPerHour = svPerHour;
        this.svPerHourAmbient = svPerHourAmbient;
        this.protectionFactor = protectionFactor;
        this.decayMultiplier = decayMultiplier;
        this.internalContamination = new LinkedHashMap<>(internalContamination);
        this.attemptedMutations = new LinkedHashSet<>(attemptedMutations);
        this.lastDoseStage = lastDoseStage;
    }

    public double svTotalCareer() { return svTotalCareer; }
    public double svPerHour() { return svPerHour; }
    public double svPerHourAmbient() { return svPerHourAmbient; }
    public double protectionFactor() { return protectionFactor; }
    public double decayMultiplier() { return decayMultiplier; }
    public Map<String, Double> internalContamination() { return internalContamination; }
    public int lastDoseStage() { return lastDoseStage; }

    public void setLastDoseStage(int v) { this.lastDoseStage = v; }
    public void setSvTotalCareer(double v) { this.svTotalCareer = sanitizeCareer(v); }
    public void setSvPerHour(double v) { this.svPerHour = v; }
    public void setSvPerHourAmbient(double v) { this.svPerHourAmbient = v; }
    public void setProtectionFactor(double v) { this.protectionFactor = v; }
    public void setDecayMultiplier(double v) { this.decayMultiplier = v; }

    /** Hard ceiling on career dose. Nothing realistic exceeds this; guards against runaway/overflow
     *  (a corrupt save could otherwise store ~1e21 Sv and pin the entity at lethal forever). */
    public static final double MAX_CAREER_SV = 1.0e4;

    private static double sanitizeCareer(double v) {
        if (!Double.isFinite(v) || v < 0.0) return v > 0.0 ? MAX_CAREER_SV : 0.0;
        return Math.min(MAX_CAREER_SV, v);
    }

    public void addSv(double sv) {
        if (!Double.isFinite(sv)) return;
        this.svTotalCareer = sanitizeCareer(this.svTotalCareer + sv);
    }

    public void addInternal(String isotopeId, double atoms) {
        internalContamination.merge(isotopeId, atoms, Double::sum);
    }

    public boolean hasAttemptedMutation(String recipeId) {
        return attemptedMutations.contains(recipeId);
    }

    public void markAttemptedMutation(String recipeId) {
        attemptedMutations.add(recipeId);
    }
}
