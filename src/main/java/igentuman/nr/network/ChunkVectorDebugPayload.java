package igentuman.nr.network;

import igentuman.nr.NuclearRadiation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChunkVectorDebugPayload(
        int chunkX,
        int chunkZ,
        double gradXRayX, double gradXRayZ,
        double gradNeutronX, double gradNeutronZ,
        double scalarXRay,
        double scalarNeutron,
        double maxBq,
        double centerY
) implements CustomPacketPayload {

    public static final Type<ChunkVectorDebugPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "chunk_vector_debug"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChunkVectorDebugPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeVarInt(p.chunkX);
                        buf.writeVarInt(p.chunkZ);
                        buf.writeDouble(p.gradXRayX);
                        buf.writeDouble(p.gradXRayZ);
                        buf.writeDouble(p.gradNeutronX);
                        buf.writeDouble(p.gradNeutronZ);
                        buf.writeDouble(p.scalarXRay);
                        buf.writeDouble(p.scalarNeutron);
                        buf.writeDouble(p.maxBq);
                        buf.writeDouble(p.centerY);
                    },
                    buf -> new ChunkVectorDebugPayload(
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
