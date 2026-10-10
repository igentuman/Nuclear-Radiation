package igentuman.nr.network;

public final class ClientChunkContaminationCache {
    private static volatile ChunkContaminationDebugPayload latest;

    private ClientChunkContaminationCache() {}

    public static void update(ChunkContaminationDebugPayload p) { latest = p; }

    public static ChunkContaminationDebugPayload get() { return latest; }

    public static void clear() { latest = null; }
}
