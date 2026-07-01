package igentuman.nr.simulation;

import igentuman.nr.containers.ContainerRadiationTicker;
import igentuman.nr.util.tracking.WorldSourceRegistry;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
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
    public void onLevelSave(LevelEvent.Save event) {
        if (event.getLevel() instanceof ServerLevel server) {
            WorldSourceRegistry.get(server).saveToLevel();
        }
    }

    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel server) {
            WorldSourceRegistry.get(server).saveToLevel();
            WorldSourceRegistry.unload(server);
            ContainerRadiationTicker.unloadLevel(server);
        }
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel server) {
            RadiationSimulator.get().tick(server);
        }
    }
}
