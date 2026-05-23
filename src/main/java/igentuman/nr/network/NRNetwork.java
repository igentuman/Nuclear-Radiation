package igentuman.nr.network;

import igentuman.nr.NuclearRadiation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = NuclearRadiation.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class NRNetwork {

    public static final String VERSION = "1";

    private NRNetwork() {}

    @SubscribeEvent
    public static void onRegister(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar(NuclearRadiation.MODID).versioned(VERSION);
        reg.playToClient(RadiationSyncPayload.TYPE, RadiationSyncPayload.STREAM_CODEC,
                (payload, ctx) -> ClientRadiationCache.update(payload));
        reg.playToClient(ChunkVectorDebugPayload.TYPE, ChunkVectorDebugPayload.STREAM_CODEC,
                (payload, ctx) -> ClientChunkVectorCache.update(payload));
        reg.playToClient(ShieldingRaysDebugPayload.TYPE, ShieldingRaysDebugPayload.STREAM_CODEC,
                (payload, ctx) -> ClientShieldingRaysCache.update(payload));
        reg.playToClient(ChunkContaminationDebugPayload.TYPE, ChunkContaminationDebugPayload.STREAM_CODEC,
                (payload, ctx) -> ClientChunkContaminationCache.update(payload));
    }

    public static void sendTo(ServerPlayer player, RadiationSyncPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendTo(ServerPlayer player, ChunkVectorDebugPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendTo(ServerPlayer player, ShieldingRaysDebugPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendTo(ServerPlayer player, ChunkContaminationDebugPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
