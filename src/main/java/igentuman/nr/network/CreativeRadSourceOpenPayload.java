package igentuman.nr.network;

import igentuman.nr.NuclearRadiation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CreativeRadSourceOpenPayload(BlockPos pos, double alphaBq, double betaBq, double xRayBq, double neutronBq)
        implements CustomPacketPayload {

    public static final Type<CreativeRadSourceOpenPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "creative_rad_open"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CreativeRadSourceOpenPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, CreativeRadSourceOpenPayload::pos,
                    ByteBufCodecs.DOUBLE, CreativeRadSourceOpenPayload::alphaBq,
                    ByteBufCodecs.DOUBLE, CreativeRadSourceOpenPayload::betaBq,
                    ByteBufCodecs.DOUBLE, CreativeRadSourceOpenPayload::xRayBq,
                    ByteBufCodecs.DOUBLE, CreativeRadSourceOpenPayload::neutronBq,
                    CreativeRadSourceOpenPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
