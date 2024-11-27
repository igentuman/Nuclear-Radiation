package igentuman.nr.setup;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.radiation.data.RadiationEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import static igentuman.nr.NuclearRadiation.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSetup {

    public static void setup() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(RadiationEvents::onPlayerCloned);
        bus.addGenericListener(Entity.class, RadiationEvents::attachPlayerRadiation);
        bus.addGenericListener(Level.class, RadiationEvents::attachWorldRadiation);
        bus.register(NuclearRadiation.worldTickHandler);
        bus.register(new RadiationEvents());
    }

    public static void init(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            //Dimensions.register();
            //CapabilityRegistration.register(event);

        });
        NuclearRadiation.packetHandler().initialize();
    }
}
