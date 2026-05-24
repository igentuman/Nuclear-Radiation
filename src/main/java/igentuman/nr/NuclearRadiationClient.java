package igentuman.nr;

import igentuman.nr.network.ClientRadiationCache;
import igentuman.nr.tools.NRTools;
import igentuman.nr.tools.client.ContaminationHudLayer;
import igentuman.nr.tools.client.RadiationHudLayer;
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
                    double bq = ClientRadiationCache.bqAtPlayer();
                    if (bq < 1000.0)      return 0f;
                    if (bq < 10000.0)     return 1f;
                    if (bq < 100000.0)    return 2f;
                    if (bq < 10000000.0)   return 3f;
                    if (bq < 100000000.0)  return 4f;
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
}
