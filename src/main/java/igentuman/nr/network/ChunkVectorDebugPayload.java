package igentuman.nr.network;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.radiation.simulation.SubChunkRadVector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record ChunkVectorDebugPayload(
        int chunkX,
        int chunkY,
        int chunkZ,
        double apexX,
        double apexY,
        double apexZ,
        double[] xRayBq,
        double[] neutronBq,
        double[] tipX,
        double[] tipY,
        double[] tipZ,
        double maxBq
) implements CustomPacketPayload {

    public static final Type<ChunkVectorDebugPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "chunk_vector_debug"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChunkVectorDebugPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeVarInt(p.chunkX);
                        buf.writeVarInt(p.chunkY);
                        buf.writeVarInt(p.chunkZ);
                        buf.writeDouble(p.apexX);
                        buf.writeDouble(p.apexY);
                        buf.writeDouble(p.apexZ);
                        for (int i = 0; i < SubChunkRadVector.DIR_COUNT; i++) buf.writeDouble(p.xRayBq[i]);
                        for (int i = 0; i < SubChunkRadVector.DIR_COUNT; i++) buf.writeDouble(p.neutronBq[i]);
                        for (int i = 0; i < SubChunkRadVector.DIR_COUNT; i++) buf.writeDouble(p.tipX[i]);
                        for (int i = 0; i < SubChunkRadVector.DIR_COUNT; i++) buf.writeDouble(p.tipY[i]);
                        for (int i = 0; i < SubChunkRadVector.DIR_COUNT; i++) buf.writeDouble(p.tipZ[i]);
                        buf.writeDouble(p.maxBq);
                    },
                    buf -> {
                        int cx = buf.readVarInt();
                        int cy = buf.readVarInt();
                        int cz = buf.readVarInt();
                        double ax = buf.readDouble(), ay = buf.readDouble(), az = buf.readDouble();
                        double[] xr = new double[SubChunkRadVector.DIR_COUNT];
                        double[] nr = new double[SubChunkRadVector.DIR_COUNT];
                        double[] tx = new double[SubChunkRadVector.DIR_COUNT];
                        double[] ty = new double[SubChunkRadVector.DIR_COUNT];
                        double[] tz = new double[SubChunkRadVector.DIR_COUNT];
                        for (int i = 0; i < SubChunkRadVector.DIR_COUNT; i++) xr[i] = buf.readDouble();
                        for (int i = 0; i < SubChunkRadVector.DIR_COUNT; i++) nr[i] = buf.readDouble();
                        for (int i = 0; i < SubChunkRadVector.DIR_COUNT; i++) tx[i] = buf.readDouble();
                        for (int i = 0; i < SubChunkRadVector.DIR_COUNT; i++) ty[i] = buf.readDouble();
                        for (int i = 0; i < SubChunkRadVector.DIR_COUNT; i++) tz[i] = buf.readDouble();
                        double max = buf.readDouble();
                        return new ChunkVectorDebugPayload(cx, cy, cz, ax, ay, az, xr, nr, tx, ty, tz, max);
                    }
            );

    public static ChunkVectorDebugPayload of(int cx, int cy, int cz, SubChunkRadVector v) {
        double[] xr = new double[SubChunkRadVector.DIR_COUNT];
        double[] nr = new double[SubChunkRadVector.DIR_COUNT];
        double[] tx = new double[SubChunkRadVector.DIR_COUNT];
        double[] ty = new double[SubChunkRadVector.DIR_COUNT];
        double[] tz = new double[SubChunkRadVector.DIR_COUNT];
        for (int i = 0; i < SubChunkRadVector.DIR_COUNT; i++) {
            xr[i] = v.xRayBq[i];
            nr[i] = v.neutronBq[i];
            Vec3 t = v.tip[i];
            tx[i] = t.x; ty[i] = t.y; tz[i] = t.z;
        }
        return new ChunkVectorDebugPayload(cx, cy, cz, v.apexX, v.apexY, v.apexZ,
                xr, nr, tx, ty, tz, v.maxBq);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
