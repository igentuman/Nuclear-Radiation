package igentuman.nr;

import igentuman.nr.block.client.CreativeRadSourceScreen;
import igentuman.nr.network.ClientRadiationCache;
import igentuman.nr.network.CreativeRadSourceOpenPayload;
import igentuman.nr.tools.NRTools;
import igentuman.nr.tools.client.ContaminationHudLayer;
import igentuman.nr.tools.client.RadiationHudLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = NuclearRadiation.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = NuclearRadiation.MODID, value = Dist.CLIENT)
public class NuclearRadiationClient {
    public NuclearRadiationClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                NRTools.GEIGER_COUNTER.get(),
                ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "radiation"),
                (stack, level, entity, seed) -> {
                    double svh = ClientRadiationCache.svPerHour();
                    if (svh < 0.00001) { // 10 uSv/h
                        return 0f;
                    } else if (svh < 0.001) { // 1 mSv/h
                        return 1f;
                    } else if (svh < 0.1) { // 100 mSv/h
                        return 2f;
                    } else if (svh < 10) { // 100 Sv/h
                        return 3f;
                    } else if (svh < 100) {
                        return 4f;
                    }
                    return 5f;
                }));
    }

    @SubscribeEvent
    static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "radiation_hud"),
                new RadiationHudLayer());
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "contamination_hud"),
                new ContaminationHudLayer());
    }

    public static void handleCreativeRadSourceOpen(CreativeRadSourceOpenPayload payload) {
        Minecraft.getInstance().execute(() ->
                Minecraft.getInstance().setScreen(new CreativeRadSourceScreen(
                        payload.pos(),
                        payload.alphaBq(),
                        payload.betaBq(),
                        payload.xRayBq(),
                        payload.neutronBq())));
    }
}
