package igentuman.nr.compat.ntm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Content-addressed, write-before-import recovery objects. Never overwrites or deletes native state. */
public final class NtmMigrationArchive {
    private final Path dimensionDirectory;
    private final Path root;

    public NtmMigrationArchive(Path dimensionDirectory) {
        this.dimensionDirectory = dimensionDirectory.toAbsolutePath().normalize();
        root = this.dimensionDirectory.resolve("nuclear_radiation/ntm_migration/v1");
    }

    /** Complete and force the recovery object before a caller is allowed to drain/import anything. */
    public Path retain(byte[] bytes) throws IOException {
        String digest = digest(bytes);
        Files.createDirectories(root);
        Path target = root.resolve(digest + ".bin");
        if (Files.exists(target)) {
            verify(target, digest);
            forceDirectories();
            return target;
        }
        Path temporary = Files.createTempFile(root, ".pending-", ".bin");
        try {
            try (FileChannel file = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) file.write(buffer);
                file.force(true);
            }
            // Do not fall back to a non-atomic copy: failure must keep the old ownership/marker intact.
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            forceDirectories();
        } finally {
            Files.deleteIfExists(temporary);
        }
        return target;
    }

    public byte[] read(String digest) throws IOException {
        if (!digest.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("Invalid NTM archive digest");
        return verify(root.resolve(digest + ".bin"), digest);
    }

    private static byte[] verify(Path path, String expected) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        if (!digest(bytes).equals(expected)) throw new IOException("Corrupt NTM recovery object: " + path);
        return bytes;
    }

    private void forceDirectories() throws IOException {
        // The Windows JDK cannot open directories as FileChannels. File contents are
        // still forced and publication remains atomic there; directory fsync is Unix-only.
        if (System.getProperty("os.name", "").startsWith("Windows")) return;
        // Force each new directory entry too, not just the leaf. Retrying an interrupted
        // retention repeats this barrier even when the content-addressed file already exists.
        for (Path path = root; path != null && path.startsWith(dimensionDirectory); path = path.getParent()) {
            try (FileChannel directory = FileChannel.open(path, StandardOpenOption.READ)) {
                directory.force(true);
            }
        }
    }

    public static String digest(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
