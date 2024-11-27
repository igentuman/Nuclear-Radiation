package igentuman.nr;

import igentuman.nr.handler.command.NCRadiationCommand;
import igentuman.nr.handler.config.*;
import igentuman.nr.handler.event.server.WorldEvents;
import igentuman.nr.radiation.data.PlayerRadiation;
import igentuman.nr.radiation.data.RadiationEvents;
import igentuman.nr.radiation.data.RadiationManager;
import igentuman.nr.radiation.data.WorldRadiation;
import igentuman.nr.network.PacketHandler;
import igentuman.nr.setup.ClientSetup;
import igentuman.nr.setup.ModSetup;
import igentuman.nr.setup.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(NuclearRadiation.MODID)
public class NuclearRadiation {

    public static final Logger LOGGER = LogManager.getLogger();
    public boolean isNcBeStopped = false;
    public static final WorldEvents worldTickHandler = new WorldEvents();
    public static final String MODID = "nuclearradiation";
    public static NuclearRadiation instance;
    private final PacketHandler packetHandler;

    public static void registerConfigs()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RadiationConfig.spec, "NuclearRadiation/radiation.toml");
    }



    public NuclearRadiation() {
        instance = this;
        registerConfigs();
        packetHandler = new PacketHandler();
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
        MinecraftForge.EVENT_BUS.addListener(this::gameShuttingDownEvent);
        ModSetup.setup();
        Registration.init();
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        modbus.addListener(ModSetup::init);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modbus.addListener(ClientSetup::init));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modbus.addListener(this::registerClientEventHandlers));
    }

    public static PacketHandler packetHandler() {
        return instance.packetHandler;
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent event) {
        if (event.getConfig().getType() == ModConfig.Type.COMMON)
            RadiationConfig.setLoaded();
    }

    private void registerCommands(RegisterCommandsEvent event) {
        NCRadiationCommand.register(event.getDispatcher());
    }

    private void registerClientEventHandlers(FMLClientSetupEvent event) {
        ClientSetup.registerEventHandlers(event);
    }

    public static ResourceLocation rl(String path)
    {
        return new ResourceLocation(MODID, path);
    }

    private void serverStopped(ServerStoppedEvent event) {
        NuclearRadiation.instance.isNcBeStopped = true;
        //stop capability tracking
        RadiationEvents.stopTracking();
        for(ServerLevel level: event.getServer().getAllLevels()) {
            RadiationManager.clear(level);
        }
    }
    private void gameShuttingDownEvent(GameShuttingDownEvent event) {
        NuclearRadiation.instance.isNcBeStopped = true;
    }

    private void serverStarted(ServerStartedEvent event) {
        NuclearRadiation.instance.isNcBeStopped = false;
        RadiationEvents.startTracking();
    }

    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(WorldRadiation.class);
        event.register(PlayerRadiation.class);
    }
}
