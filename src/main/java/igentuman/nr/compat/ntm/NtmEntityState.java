package igentuman.nr.compat.ntm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nr.radiation.storage.EntityRadiationData;

/** Persisted with the NR entity attachment; native exposure buffers are deliberately transient. */
public final class NtmEntityState {
    public static final int MIGRATION_VERSION = 1;
    public static final Codec<NtmEntityState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("migration_version", 0).forGetter(state -> state.migrationVersion),
            Codec.DOUBLE.optionalFieldOf("imported_dose", 0.0).forGetter(state -> state.importedDose)
    ).apply(instance, NtmEntityState::new));

    private int migrationVersion;
    private double importedDose;
    private double exposureSv;
    private double ambientExposureSv;

    public NtmEntityState() { this(0, 0.0); }

    public NtmEntityState(int version, double importedDose) {
        this.migrationVersion = version;
        this.importedDose = importedDose;
    }

    public boolean migrated() { return migrationVersion >= MIGRATION_VERSION; }
    public double importedDose() { return importedDose; }

    /** Returns the legacy dose to import, exactly once, including entities first loaded much later. */
    public double migrate(double legacyDose) {
        if (migrated()) return 0.0;
        importedDose = Double.isFinite(legacyDose) ? Math.max(0.0, legacyDose) : 0.0;
        migrationVersion = MIGRATION_VERSION;
        return importedDose;
    }

    public void recordExposure(double sv) {
        if (Double.isFinite(sv) && sv > 0.0) {
            exposureSv = Math.min(EntityRadiationData.MAX_CAREER_SV, exposureSv + sv);
            recordAmbientCorrection(sv);
        }
    }

    public void recordAmbientCorrection(double sv) {
        if (Double.isFinite(sv) && sv > 0.0) {
            ambientExposureSv = Math.min(EntityRadiationData.MAX_CAREER_SV, ambientExposureSv + sv);
        }
    }

    public double drainAmbientExposure() {
        double result = ambientExposureSv;
        ambientExposureSv = 0.0;
        return result;
    }

    public double drainExposure() {
        double result = exposureSv;
        exposureSv = 0.0;
        return result;
    }
}
