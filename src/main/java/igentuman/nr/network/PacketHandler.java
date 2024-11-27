package igentuman.nr.network;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.network.toClient.PacketPlayerRadiationData;
import igentuman.nr.network.toClient.PacketWorldRadiationData;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler extends BasePacketHandler {

    private final SimpleChannel netHandler = createChannel(NuclearRadiation.rl(NuclearRadiation.MODID));

    @Override
    protected SimpleChannel getChannel() {
        return netHandler;
    }

    @Override
    public void initialize() {
        //Server to client messages

        registerServerToClient(PacketWorldRadiationData.class, PacketWorldRadiationData::decode);
        registerServerToClient(PacketPlayerRadiationData.class, PacketPlayerRadiationData::decode);
    }
}