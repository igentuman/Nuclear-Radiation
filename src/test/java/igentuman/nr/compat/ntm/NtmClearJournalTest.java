package igentuman.nr.compat.ntm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class NtmClearJournalTest {
    @TempDir Path directory;

    @Test
    void recoversNewestClearAcrossIndependentDimensionAndChunkSaveOrder() throws IOException {
        var journal = new NtmClearJournal(directory);
        assertEquals(0, journal.latest());
        journal.commit(1);
        journal.commit(3);
        journal.commit(2);
        var state = new NtmWorldState(); // An older Minecraft dimension save.
        state.recoverClear(new NtmClearJournal(directory).latest());
        assertEquals(3, state.clearGeneration());
        var field = new NtmWorldField(); // A later-loaded chunk from before the clear.
        field.importOnce(java.util.List.of(), 123, 0);
        assertTrue(state.synchronize(field));
        assertEquals(3, field.clearGeneration());
        assertFalse(state.synchronize(field));
        assertTrue(field.migrated());
        assertEquals(123, field.legacyFingerprint());
        state.recoverClear(1);
        assertEquals(3, state.clearGeneration());
    }

    @Test
    void interruptedUnpublishedWriteIsIgnoredButPublishedCorruptionFailsClosed() throws IOException {
        var journal = new NtmClearJournal(directory);
        journal.commit(1);
        var root = directory.resolve("nuclear_radiation/ntm_migration/v1/clears");
        Files.write(root.resolve(".pending-interrupted.bin"), new byte[]{0});
        assertEquals(1, new NtmClearJournal(directory).latest());
        Files.write(root.resolve("1.clear"), new byte[]{0});
        assertThrows(IOException.class, () -> new NtmClearJournal(directory).latest());
    }

    @Test
    void writeFailuresAreReportedAndDimensionsAreIsolated() throws IOException {
        var first = new NtmClearJournal(directory.resolve("first"));
        var second = new NtmClearJournal(directory.resolve("second"));
        first.commit(1);
        assertEquals(0, second.latest());
        Path blocked = directory.resolve("blocked");
        Files.createDirectories(blocked);
        Files.write(blocked.resolve("nuclear_radiation"), new byte[]{0});
        assertThrows(IOException.class, () -> new NtmClearJournal(blocked).commit(1));
        assertEquals(1, first.latest());
        assertThrows(IllegalArgumentException.class, () -> first.commit(0));
        assertThrows(IllegalArgumentException.class, () -> new NtmWorldState().recoverClear(-1));
    }
}
