package igentuman.nr.compat.ntm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

/** Dimension-scoped clear tombstone. A world clear must also affect subsequently loaded chunks. */
public final class NtmWorldState {
    static final Codec<Long> GENERATION_CODEC = Codec.LONG.validate(value -> value >= 0L
            ? DataResult.success(value) : DataResult.error(() -> "Negative NTM clear generation"));
    public static final Codec<NtmWorldState> CODEC = GENERATION_CODEC
            .optionalFieldOf("clear_generation", 0L).codec()
            .xmap(NtmWorldState::new, state -> state.clearGeneration);

    private long clearGeneration;

    public NtmWorldState() { this(0L); }

    private NtmWorldState(long clearGeneration) { this.clearGeneration = clearGeneration; }

    public long clearGeneration() { return clearGeneration; }

    /** The runtime adapter must commit its write-ahead journal before advancing a live world clear. */
    public void clearWorld() { clearGeneration = Math.incrementExact(clearGeneration); }

    /** Recover a durable clear that may be newer than Minecraft's last dimension save. */
    public void recoverClear(long generation) {
        if (generation < 0L) throw new IllegalArgumentException("Negative clear generation");
        clearGeneration = Math.max(clearGeneration, generation);
    }

    /** Call after import and before exposing a chunk to readers/writers or the NR sampler. */
    public boolean synchronize(NtmWorldField field) {
        if (!field.migrated()) throw new IllegalStateException("Import native chunk before applying world clear state");
        return field.applyClearGeneration(clearGeneration);
    }
}
