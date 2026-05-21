package igentuman.nr.network;

import igentuman.nr.NuclearRadiation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ShieldingRaysDebugPayload(
        double originX, double originY, double originZ,
        double[] endX, double[] endY, double[] endZ,
        float[] passValue,
        byte[] channel
) implements CustomPacketPayload {

    public static final Type<ShieldingRaysDebugPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "shielding_rays_debug"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShieldingRaysDebugPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeDouble(p.originX);
                        buf.writeDouble(p.originY);
                        buf.writeDouble(p.originZ);
                        int n = p.endX.length;
                        buf.writeVarInt(n);
                        for (int i = 0; i < n; i++) {
                            buf.writeDouble(p.endX[i]);
                            buf.writeDouble(p.endY[i]);
                            buf.writeDouble(p.endZ[i]);
                            buf.writeFloat(p.passValue[i]);
                            buf.writeByte(p.channel[i]);
                        }
                    },
                    buf -> {
                        double ox = buf.readDouble();
                        double oy = buf.readDouble();
                        double oz = buf.readDouble();
                        int n = buf.readVarInt();
                        double[] ex = new double[n];
                        double[] ey = new double[n];
                        double[] ez = new double[n];
                        float[] pv = new float[n];
                        byte[] ch = new byte[n];
                        for (int i = 0; i < n; i++) {
                            ex[i] = buf.readDouble();
                            ey[i] = buf.readDouble();
                            ez[i] = buf.readDouble();
                            pv[i] = buf.readFloat();
                            ch[i] = buf.readByte();
                        }
                        return new ShieldingRaysDebugPayload(ox, oy, oz, ex, ey, ez, pv, ch);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
