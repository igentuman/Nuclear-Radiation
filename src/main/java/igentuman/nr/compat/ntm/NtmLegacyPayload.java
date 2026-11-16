package igentuman.nr.compat.ntm;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Strict, optional-mod-free reader for the pinned NTM RadiationPayload v1 format. */
public final class NtmLegacyPayload {
    public static final int MAX_SECTIONS = 1024;
    public static final int MAX_POCKETS = 4096;
    public static final int MAX_BYTES = 5 + MAX_SECTIONS * (8 + MAX_POCKETS * 8);

    private NtmLegacyPayload() {}

    /** Null means no native payload. A present but malformed payload must never become an empty import. */
    public static Map<Integer, double[]> decode(byte[] payload) {
        if (payload == null) return Map.of();
        if (payload.length < 5 || payload.length > MAX_BYTES) throw invalid();
        ByteBuffer input = ByteBuffer.wrap(payload);
        if (input.get() != 1) throw invalid();
        int count = input.getInt();
        if (count <= 0 || count > MAX_SECTIONS) throw invalid();
        Map<Integer, double[]> sections = new TreeMap<>();
        for (int i = 0; i < count; i++) {
            if (input.remaining() < 8) throw invalid();
            int section = input.getInt();
            int pockets = input.getInt();
            if (section < 0 || section >= MAX_SECTIONS || pockets < 0 || pockets > MAX_POCKETS
                    || sections.containsKey(section) || input.remaining() < pockets * Double.BYTES) throw invalid();
            double[] densities = new double[pockets];
            for (int p = 0; p < pockets; p++) densities[p] = NtmWorldField.sanitize(input.getDouble());
            sections.put(section, densities);
        }
        if (input.hasRemaining()) throw invalid();
        return sections;
    }

    /** Canonical encoding for built snapshots and old SavedData rows; never used to rewrite native files. */
    public static byte[] encode(Map<Integer, double[]> sections) {
        if (sections.isEmpty()) return null;
        if (sections.size() > MAX_SECTIONS) throw invalid();
        int bytes = 5;
        for (var entry : sections.entrySet()) {
            if (entry.getKey() < 0 || entry.getKey() >= MAX_SECTIONS || entry.getValue() == null
                    || entry.getValue().length > MAX_POCKETS) throw invalid();
            bytes += 8 + entry.getValue().length * Double.BYTES;
        }
        ByteBuffer output = ByteBuffer.allocate(bytes).put((byte) 1).putInt(sections.size());
        for (var entry : new TreeMap<>(sections).entrySet()) {
            output.putInt(entry.getKey()).putInt(entry.getValue().length);
            // Preserve exact scalar bits in the archive. Sanitization belongs to conversion, not backup.
            for (double value : entry.getValue()) output.putDouble(value);
        }
        return output.array();
    }

    /** NTM's FNV-1a identity, including its reserved-zero handling. */
    public static long fingerprint(byte[] payload) {
        if (payload == null) return 0L;
        long hash = 0xcbf29ce484222325L;
        for (byte value : payload) hash = (hash ^ (value & 0xffL)) * 0x100000001b3L;
        return hash == 0L ? 1L : hash;
    }

    /** Captured topology and scalar densities, copied while the native server thread owns the field. */
    public record Section(int sectionY, List<Integer> pocketOf, List<Double> densities) {
        public Section {
            pocketOf = List.copyOf(pocketOf);
            densities = List.copyOf(densities);
            if (pocketOf.size() != 4096 || densities.size() > MAX_POCKETS) throw invalid();
            for (int pocket : pocketOf) if (pocket < -1 || pocket >= densities.size()) throw invalid();
        }

        public NtmWorldField.Section convert(NtmEmissionProfiles.Calibration calibration, long tick) {
            return new NtmWorldField.Section(sectionY, pocketOf, densities.stream()
                    .map(value -> NtmWorldField.Pocket.of(calibration.profile(NtmWorldField.sanitize(value), tick))).toList());
        }
    }

    /** Build a detached candidate. Attaching it would require the still-pending world/intake handoff. */
    public static NtmWorldField candidate(List<Section> sections, long fingerprint, long tick,
                                           NtmEmissionProfiles.Calibration calibration) {
        HashSet<Integer> heights = new HashSet<>();
        for (Section section : sections) if (!heights.add(section.sectionY())) throw invalid();
        NtmWorldField result = new NtmWorldField();
        result.importOnce(sections.stream().map(section -> section.convert(calibration, tick)).toList(), fingerprint, tick);
        return result;
    }

    private static IllegalArgumentException invalid() {
        return new IllegalArgumentException("Malformed or unsupported NTM radiation payload; legacy data retained");
    }
}
