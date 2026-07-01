package igentuman.nr.util.persistence;

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
                    .forGetter(d -> new ArrayList<>(d.attemptedMutations))
    ).apply(inst, EntityRadiationData::new));

    private double svTotalCareer;
    private double svPerHour;
    private double protectionFactor;
    private double decayMultiplier;
    private Map<String, Double> internalContamination;
    private Set<String> attemptedMutations;

    public EntityRadiationData() {
        this(0.0, 0.0, 0.0, 1.0, new LinkedHashMap<>(), List.of());
    }

    public EntityRadiationData(double svTotalCareer,
                               double svPerHour,
                               double protectionFactor,
                               double decayMultiplier,
                               Map<String, Double> internalContamination,
                               List<String> attemptedMutations) {
        this.svTotalCareer = svTotalCareer;
        this.svPerHour = svPerHour;
        this.protectionFactor = protectionFactor;
        this.decayMultiplier = decayMultiplier;
        this.internalContamination = new LinkedHashMap<>(internalContamination);
        this.attemptedMutations = new LinkedHashSet<>(attemptedMutations);
    }

    public double svTotalCareer() { return svTotalCareer; }
    public double svPerHour() { return svPerHour; }
    public double protectionFactor() { return protectionFactor; }
    public double decayMultiplier() { return decayMultiplier; }
    public Map<String, Double> internalContamination() { return internalContamination; }

    public void setSvTotalCareer(double v) { this.svTotalCareer = v; }
    public void setSvPerHour(double v) { this.svPerHour = v; }
    public void setProtectionFactor(double v) { this.protectionFactor = v; }
    public void setDecayMultiplier(double v) { this.decayMultiplier = v; }

    public void addSv(double sv) { this.svTotalCareer += sv; }

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
