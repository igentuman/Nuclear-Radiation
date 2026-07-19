package igentuman.nr.events;

import igentuman.nr.radiation.irradiation.BlockIrradiationSimulator;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class BlockIrradiationEvents {

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        BlockIrradiationSimulator.get().startWorker();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        BlockIrradiationSimulator.get().stopWorker();
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel server) {
            BlockIrradiationSimulator.get().tick(server);
        }
    }
}
