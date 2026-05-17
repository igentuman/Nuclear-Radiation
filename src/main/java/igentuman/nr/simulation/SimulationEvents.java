package igentuman.nr.simulation;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class SimulationEvents {

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        RadiationSimulator.get().startWorker();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        RadiationSimulator.get().stopWorker();
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel server) {
            RadiationSimulator.get().tick(server);
        }
    }
}
