package igentuman.nr.network;

import igentuman.nr.NuclearRadiation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VomitPayload(int entityId, int stage) implements CustomPacketPayload {

    public static final Type<VomitPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "vomit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VomitPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, VomitPayload::entityId,
                    ByteBufCodecs.VAR_INT, VomitPayload::stage,
                    VomitPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
