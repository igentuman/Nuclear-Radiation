package igentuman.nr.network;

import igentuman.nr.NuclearRadiation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChunkContaminationDebugPayload(
        int[] chunkX,
        int[] chunkZ,
        double[] airBq,
        double[] waterBq,
        double[] soilBq
) implements CustomPacketPayload {

    public static final Type<ChunkContaminationDebugPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "chunk_contamination_debug"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChunkContaminationDebugPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        int n = p.chunkX.length;
                        buf.writeVarInt(n);
                        for (int i = 0; i < n; i++) {
                            buf.writeVarInt(p.chunkX[i]);
                            buf.writeVarInt(p.chunkZ[i]);
                            buf.writeDouble(p.airBq[i]);
                            buf.writeDouble(p.waterBq[i]);
                            buf.writeDouble(p.soilBq[i]);
                        }
                    },
                    buf -> {
                        int n = buf.readVarInt();
                        int[] cx = new int[n];
                        int[] cz = new int[n];
                        double[] air = new double[n];
                        double[] water = new double[n];
                        double[] soil = new double[n];
                        for (int i = 0; i < n; i++) {
                            cx[i] = buf.readVarInt();
                            cz[i] = buf.readVarInt();
                            air[i] = buf.readDouble();
                            water[i] = buf.readDouble();
                            soil[i] = buf.readDouble();
                        }
                        return new ChunkContaminationDebugPayload(cx, cz, air, water, soil);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
