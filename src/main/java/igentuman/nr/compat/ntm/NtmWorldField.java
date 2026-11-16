package igentuman.nr.compat.ntm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.isotope.IsotopeStack;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * NR-owned environmental release densities for one chunk. This is deliberately not
 * registered with the simulation until native-field migration/intake handoff exists.
 * Chunk/dimension identity comes from the attachment holder; section/pocket IDs are local.
 * All access belongs to the server thread; worker snapshots must use immutable section records.
 */
public final class NtmWorldField {
    public static final int FORMAT_VERSION = 1;
    public static final int MIGRATION_VERSION = 1;
    public static final double EPSILON = 1.0e-5;
    public static final double MAX_RADIATION = Double.MAX_VALUE / 2.0;

    // Keep isotope IDs/atoms even if an isotope is temporarily unavailable after a data-pack change.
    // The general NR profile codec silently skips unknown IDs, which is unsuitable for migration.
    public record Atom(String isotope, double atoms, long tick) {
        private static final Codec<Atom> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("isotope").forGetter(Atom::isotope),
                Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("atoms").forGetter(Atom::atoms),
                Codec.LONG.fieldOf("tick").forGetter(Atom::tick)
        ).apply(instance, Atom::new));
    }

    public record Pocket(List<Atom> atoms) {
        private static final Codec<Pocket> CODEC = Atom.CODEC.listOf().xmap(Pocket::new, Pocket::atoms);

        public Pocket { atoms = List.copyOf(atoms); }

        public static Pocket of(RadiationProfile profile) {
            validate(profile);
            return new Pocket(profile.stacks().stream()
                    .map(s -> new Atom(s.isotope().id(), s.atoms(), s.timestamp())).toList());
        }

        private static void validate(RadiationProfile profile) {
            for (IsotopeStack stack : profile.stacks()) {
                if (!Double.isFinite(stack.atoms()) || stack.atoms() < 0.0
                        || !Double.isFinite(stack.currentActivityBq())) {
                    throw new IllegalArgumentException("Invalid NTM pocket isotope inventory");
                }
            }
            if (!Double.isFinite(profile.totalActivityBq())) throw new IllegalArgumentException("NTM pocket activity overflow");
        }

        public RadiationProfile resolve() {
            RadiationProfile result = RadiationProfile.empty();
            for (Atom atom : atoms) {
                result.put(new IsotopeStack(NtmEmissionProfiles.requireIsotope(atom.isotope()), atom.atoms(), atom.tick()));
            }
            // Validate the resolved activity too: a finite atom count may overflow with a short half-life.
            validate(result);
            return result;
        }
    }

    /** Native index order is (localY << 8) | (localZ << 4) | localX; -1 denotes a wall. */
    public record Section(int sectionY, List<Integer> pocketOf, List<Pocket> pockets) {
        private static final Codec<Section> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("section_y").forGetter(Section::sectionY),
                Codec.INT.listOf().fieldOf("pocket_of").forGetter(Section::pocketOf),
                Pocket.CODEC.listOf().fieldOf("pockets").forGetter(Section::pockets)
        ).apply(instance, Section::new));

        public Section {
            pocketOf = List.copyOf(pocketOf);
            pockets = List.copyOf(pockets);
        }

        private String error() {
            if (pocketOf.size() != 4096 || pockets.size() > 4096) return "Invalid NTM section dimensions";
            for (int id : pocketOf) {
                if (id < -1 || id >= pockets.size()) return "Invalid NTM pocket index";
            }
            for (Pocket pocket : pockets) {
                HashSet<String> ids = new HashSet<>();
                for (Atom atom : pocket.atoms()) {
                    if (atom.isotope().isBlank() || !ids.add(atom.isotope())
                            || !Double.isFinite(atom.atoms()) || atom.atoms() < 0.0) {
                        return "Invalid NTM pocket isotope inventory";
                    }
                }
            }
            return null;
        }
    }

    public static final Codec<NtmWorldField> CODEC = RecordCodecBuilder.<NtmWorldField>create(instance -> instance.group(
            Codec.INT.optionalFieldOf("format_version", FORMAT_VERSION).forGetter(f -> f.formatVersion),
            Codec.INT.optionalFieldOf("migration_version", 0).forGetter(f -> f.migrationVersion),
            Codec.LONG.optionalFieldOf("legacy_fingerprint", 0L).forGetter(f -> f.legacyFingerprint),
            Codec.LONG.optionalFieldOf("import_tick", 0L).forGetter(f -> f.importTick),
            NtmWorldState.GENERATION_CODEC.optionalFieldOf("clear_generation", 0L).forGetter(f -> f.clearGeneration),
            Section.CODEC.listOf().optionalFieldOf("sections", List.of()).forGetter(f -> List.copyOf(f.sections))
    ).apply(instance, NtmWorldField::new)).validate(field -> {
        String error = field.error();
        return error == null ? DataResult.success(field) : DataResult.error(() -> error);
    });

    private final int formatVersion;
    private int migrationVersion;
    private long legacyFingerprint;
    private long importTick;
    private long clearGeneration;
    private final List<Section> sections;
    private long lastDecayTick = Long.MIN_VALUE;

    public NtmWorldField() { this(FORMAT_VERSION, 0, 0L, 0L, 0L, List.of()); }

    private NtmWorldField(int formatVersion, int migrationVersion, long fingerprint, long importTick,
                          long clearGeneration, List<Section> sections) {
        this.formatVersion = formatVersion;
        this.migrationVersion = migrationVersion;
        this.legacyFingerprint = fingerprint;
        this.importTick = importTick;
        this.clearGeneration = clearGeneration;
        this.sections = new ArrayList<>(sections);
    }

    private String error() {
        if (formatVersion != FORMAT_VERSION || migrationVersion < 0 || migrationVersion > MIGRATION_VERSION) {
            return "Unsupported NTM world-field version";
        }
        if (migrationVersion == 0 && !sections.isEmpty()) return "Uncommitted NTM world field";
        if (sections.size() > 1024) return "Too many NTM sections";
        HashSet<Integer> heights = new HashSet<>();
        for (Section section : sections) {
            if (!heights.add(section.sectionY())) return "Duplicate NTM section";
            String error = section.error();
            if (error != null) return error;
        }
        return null;
    }

    public boolean migrated() { return migrationVersion == MIGRATION_VERSION; }
    public long legacyFingerprint() { return legacyFingerprint; }
    public long importTick() { return importTick; }
    public long clearGeneration() { return clearGeneration; }
    public List<Section> sections() { return List.copyOf(sections); }

    /** Stable volume-source identity, independent of mutable activity, configuration and load order. */
    public static UUID sourceId(String dimension, int chunkX, int chunkZ, int sectionY, int pocket) {
        if (dimension == null || !dimension.matches("[a-z0-9_.-]+:[a-z0-9/._-]+") || pocket < 0 || pocket >= 4096) {
            throw new IllegalArgumentException("Invalid NTM source ownership key");
        }
        String key = "nuclear_radiation:ntm/world/v1/" + dimension + "/" + chunkX + "/" + chunkZ
                + "/" + sectionY + "/" + pocket;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    /** Commit a fully decoded/built snapshot and its marker together. Does not touch legacy storage. */
    public boolean importOnce(List<Section> snapshot, long fingerprint, long tick) {
        if (migrated()) return false;
        NtmWorldField candidate = new NtmWorldField(FORMAT_VERSION, MIGRATION_VERSION, fingerprint, tick, clearGeneration, snapshot);
        String error = candidate.error();
        if (error != null) throw new IllegalArgumentException(error);
        for (Section section : candidate.sections) {
            for (Pocket pocket : section.pockets()) pocket.resolve();
        }
        sections.addAll(candidate.sections);
        legacyFingerprint = fingerprint;
        importTick = tick;
        migrationVersion = MIGRATION_VERSION;
        return true;
    }

    public RadiationProfile profileAt(int x, int y, int z, boolean loaded) {
        if (!loaded) return RadiationProfile.empty();
        Section section = sectionAt(y >> 4);
        if (section == null) return RadiationProfile.empty();
        int pocket = pocketAt(section, x, y, z);
        return pocket < 0 ? RadiationProfile.empty() : section.pockets().get(pocket).resolve();
    }

    /** Install geometry for a previously empty section without changing any existing release. */
    public void addEmptySection(int sectionY, List<Integer> geometry, int pocketCount) {
        if (!migrated()) throw new IllegalStateException("Import native state before adding geometry");
        if (sectionAt(sectionY) != null) return;
        Section section = new Section(sectionY, geometry,
                java.util.Collections.nCopies(pocketCount, new Pocket(List.of())));
        String error = section.error();
        if (error != null) throw new IllegalArgumentException(error);
        sections.add(section);
    }

    public double ownedAt(int x, int y, int z, boolean loaded, NtmEmissionProfiles.Calibration calibration) {
        return calibration.nativeUnits(profileAt(x, y, z, loaded));
    }

    /** Set/decrement/clear operate only on this attachment, never on the total NR field. */
    public void set(int x, int y, int z, double value, boolean loaded,
                    NtmEmissionProfiles.Calibration calibration, long tick) {
        if (!loaded || !migrated()) return;
        Section section = sectionAt(y >> 4);
        if (section == null) return; // The adapter must supply built geometry before accepting writes.
        int pocket = pocketAt(section, x, y, z);
        if (pocket < 0) return;
        List<Pocket> updated = new ArrayList<>(section.pockets());
        updated.set(pocket, Pocket.of(calibration.profile(sanitize(value), tick)));
        sections.set(sections.indexOf(section), new Section(section.sectionY(), section.pocketOf(), updated));
    }

    public void increment(int x, int y, int z, double amount, double cap, boolean loaded,
                          NtmEmissionProfiles.Calibration calibration, long tick) {
        if (!loaded || !migrated() || Double.isNaN(amount) || Double.isNaN(cap) || Math.abs(amount) < EPSILON) return;
        Section section = sectionAt(y >> 4);
        if (section == null) return;
        int pocket = pocketAt(section, x, y, z);
        if (pocket < 0) return;
        RadiationProfile profile = section.pockets().get(pocket).resolve();
        profile.advanceDecay(tick);
        double current = calibration.nativeUnits(profile);
        if (amount > 0.0 && current >= cap) return;
        double target = sanitize(Math.min(current + amount, cap));
        if (target == 0.0) {
            profile = RadiationProfile.empty();
        } else if (target > current) {
            profile.mergeAtoms(calibration.profile(target - current, tick), 1.0, tick);
        } else if (current > 0.0) {
            double fraction = target / current;
            for (IsotopeStack stack : profile.stacks()) stack.setAtoms(stack.atoms() * fraction);
        }
        List<Pocket> updated = new ArrayList<>(section.pockets());
        updated.set(pocket, Pocket.of(profile));
        sections.set(sections.indexOf(section), new Section(section.sectionY(), section.pocketOf(), updated));
    }

    public void decrement(int x, int y, int z, double amount, boolean loaded,
                          NtmEmissionProfiles.Calibration calibration, long tick) {
        increment(x, y, z, -amount, Double.MAX_VALUE, loaded, calibration, tick);
    }

    /** Keep topology and migration tombstones so a clear cannot resurrect archived legacy radiation. */
    public void clear() {
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            sections.set(i, new Section(section.sectionY(), section.pocketOf(),
                    section.pockets().stream().map(p -> new Pocket(List.of())).toList()));
        }
    }

    boolean applyClearGeneration(long generation) {
        if (generation <= clearGeneration) return false;
        clear();
        clearGeneration = generation;
        return true;
    }

    /** Uses the ordinary NR profile decay policy; a read never refreshes a release's lifetime. */
    public boolean advanceDecay(long tick) {
        if (lastDecayTick == tick) return false;
        boolean changed = false;
        List<Section> updated = new ArrayList<>();
        for (Section section : sections) {
            List<Pocket> pockets = new ArrayList<>();
            for (Pocket pocket : section.pockets()) {
                if (pocket.atoms().isEmpty()) {
                    pockets.add(pocket);
                    continue;
                }
                RadiationProfile profile = pocket.resolve();
                profile.advanceDecay(tick);
                pockets.add(Pocket.of(profile));
                changed = true;
            }
            updated.add(new Section(section.sectionY(), section.pocketOf(), pockets));
        }
        sections.clear();
        sections.addAll(updated);
        lastDecayTick = tick;
        return changed;
    }

    private Section sectionAt(int sectionY) {
        for (Section section : sections) if (section.sectionY() == sectionY) return section;
        return null;
    }

    private static int pocketAt(Section section, int x, int y, int z) {
        return section.pocketOf().get(((y & 15) << 8) | ((z & 15) << 4) | (x & 15));
    }

    /** Match NTM's epsilon/clamp, including saturation of positive infinity. */
    public static double sanitize(double value) {
        if (Double.isNaN(value) || Math.abs(value) < EPSILON) return 0.0;
        return Math.max(0.0, Math.min(value, MAX_RADIATION));
    }
}
