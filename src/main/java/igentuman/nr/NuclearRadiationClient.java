package igentuman.nr;

import igentuman.nr.client.CreativeRadSourceScreen;
import igentuman.nr.client.ContaminationHudLayer;
import igentuman.nr.client.GeigerModelProperty;
import igentuman.nr.client.RadiationDebugControls;
import igentuman.nr.client.RadiationDebugRenderer;
import igentuman.nr.client.RadiationHudLayer;
import igentuman.nr.client.WhiteNoiseLayer;
import igentuman.nr.client.particle.GasCloudParticle;
import igentuman.nr.client.particle.RadiationParticle;
import igentuman.nr.client.particle.VomitEmitter;
import igentuman.nr.client.particle.VomitParticle;
import igentuman.nr.network.CreativeRadSourceOpenPayload;
import igentuman.nr.network.VomitPayload;
import igentuman.nr.radiation.source.Corium;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterDebugRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

import static igentuman.nr.NuclearRadiation.rl;

@Mod(value = NuclearRadiation.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = NuclearRadiation.MODID, value = Dist.CLIENT)
public class NuclearRadiationClient {
    public NuclearRadiationClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForge.EVENT_BUS.addListener(RadiationDebugControls::onClientTick);
        NeoForge.EVENT_BUS.addListener(RadiationDebugControls::onLogout);
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(RadiationDebugControls.TOGGLE_KEY);
    }

    @SubscribeEvent
    static void registerDebugRenderers(RegisterDebugRenderersEvent event) {
        event.register(new RadiationDebugRenderer());
    }

    @SubscribeEvent
    static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NuclearRadiation.RADIATION_PARTICLE.get(), RadiationParticle.Provider::new);
        event.registerSpriteSet(NuclearRadiation.GAS_CLOUD_PARTICLE.get(), GasCloudParticle.Provider::new);
        event.registerSpriteSet(NuclearRadiation.VOMIT_PARTICLE.get(), VomitParticle.Provider::new);
    }

    @SubscribeEvent
    static void registerItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(rl("radiation"), GeigerModelProperty.CODEC);
    }

    @SubscribeEvent
    static void registerFluidModels(RegisterFluidModelsEvent event) {
        event.register(new FluidModel.Unbaked(
                        new Material(rl("block/fluid/molten_still")),
                        new Material(rl("block/fluid/molten_flow")),
                        null,
                        state -> Corium.COLOR),
                Corium.MOLTEN_CORIUM, Corium.FLOWING_MOLTEN_CORIUM);
    }

    @SubscribeEvent
    static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(rl("white_noise"), new WhiteNoiseLayer());
        event.registerAboveAll(rl("radiation_hud"), new RadiationHudLayer());
        event.registerAboveAll(rl("contamination_hud"), new ContaminationHudLayer());
    }

    public static void handleVomit(VomitPayload payload) {
        Minecraft.getInstance().execute(() -> VomitEmitter.add(payload.entityId(), payload.stage()));
    }

    public static void handleCreativeRadSourceOpen(CreativeRadSourceOpenPayload payload) {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new CreativeRadSourceScreen(
                payload.pos(), payload.alphaBq(), payload.betaBq(), payload.xRayBq(), payload.neutronBq())));
    }
}
