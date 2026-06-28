package igentuman.nr.network;

import igentuman.nr.NuclearRadiation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RadiationSyncPayload(double svTotal, double svPerHour)
        implements CustomPacketPayload {

    public static final Type<RadiationSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "radiation_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadiationSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, RadiationSyncPayload::svTotal,
                    ByteBufCodecs.DOUBLE, RadiationSyncPayload::svPerHour,
                    RadiationSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
