package igentuman.nr.network;

import igentuman.nr.NuclearRadiation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CreativeRadSourceConfigPayload(BlockPos pos, double alphaBq, double betaBq, double xRayBq, double neutronBq)
        implements CustomPacketPayload {

    public static final Type<CreativeRadSourceConfigPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "creative_rad_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CreativeRadSourceConfigPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, CreativeRadSourceConfigPayload::pos,
                    ByteBufCodecs.DOUBLE, CreativeRadSourceConfigPayload::alphaBq,
                    ByteBufCodecs.DOUBLE, CreativeRadSourceConfigPayload::betaBq,
                    ByteBufCodecs.DOUBLE, CreativeRadSourceConfigPayload::xRayBq,
                    ByteBufCodecs.DOUBLE, CreativeRadSourceConfigPayload::neutronBq,
                    CreativeRadSourceConfigPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
