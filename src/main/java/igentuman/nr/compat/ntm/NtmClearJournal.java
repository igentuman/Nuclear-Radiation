package igentuman.nr.compat.ntm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/** Write-ahead clear watermark: dimension/chunk save ordering must never resurrect a cleared release. */
public final class NtmClearJournal {
    private final Path dimensionDirectory;
    private final Path directory;

    public NtmClearJournal(Path dimensionDirectory) {
        this.dimensionDirectory = dimensionDirectory.toAbsolutePath().normalize();
        directory = this.dimensionDirectory.resolve("nuclear_radiation/ntm_migration/v1/clears");
    }

    public long latest() throws IOException {
        if (!Files.exists(directory)) return 0L;
        long generation = 0L;
        try (var entries = Files.newDirectoryStream(directory, "*.clear")) {
            for (Path entry : entries) {
                String name = entry.getFileName().toString();
                long value;
                try {
                    value = Long.parseLong(name.substring(0, name.length() - 6));
                } catch (NumberFormatException malformed) {
                    throw new IOException("Invalid NTM clear journal entry: " + entry, malformed);
                }
                byte[] bytes = Files.readAllBytes(entry);
                if (value <= 0L || bytes.length != Long.BYTES || ByteBuffer.wrap(bytes).getLong() != value) {
                    throw new IOException("Corrupt NTM clear journal entry: " + entry);
                }
                generation = Math.max(generation, value);
            }
        }
        return generation;
    }

    public void commit(long generation) throws IOException {
        if (generation <= 0L) throw new IllegalArgumentException("Invalid clear generation");
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, ".pending-", ".bin");
        try {
            try (FileChannel file = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES).putLong(generation).flip();
                while (buffer.hasRemaining()) file.write(buffer);
                file.force(true);
            }
            Files.move(temporary, directory.resolve(generation + ".clear"), StandardCopyOption.ATOMIC_MOVE);
            if (!System.getProperty("os.name", "").startsWith("Windows")) {
                // Force newly created parent entries, as well as the published watermark.
                for (Path path = directory; path != null && path.startsWith(dimensionDirectory);
                     path = path.getParent()) {
                    try (FileChannel folder = FileChannel.open(path, StandardOpenOption.READ)) {
                        folder.force(true);
                    }
                }
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
