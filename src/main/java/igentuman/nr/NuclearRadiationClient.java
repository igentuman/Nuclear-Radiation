package igentuman.nr;

import igentuman.nr.client.CreativeRadSourceScreen;
import igentuman.nr.client.particle.GasCloudParticle;
import igentuman.nr.client.particle.VomitParticle;
import igentuman.nr.radiation.source.Corium;
import igentuman.nr.registry.CoriumFluidType;
import igentuman.nr.client.GlowSilhouette;
import igentuman.nr.client.particle.RadiationParticle;
import igentuman.nr.client.particle.VomitEmitter;
import igentuman.nr.client.IonizationGlowRenderer;
import igentuman.nr.client.RadiationScreenLayer;
import igentuman.nr.network.ClientRadiationCache;
import igentuman.nr.network.CreativeRadSourceOpenPayload;
import igentuman.nr.network.VomitPayload;
import igentuman.nr.registry.NRTools;
import igentuman.nr.client.ContaminationHudLayer;
import igentuman.nr.client.RadiationHudLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

import static igentuman.nr.NuclearRadiation.rl;

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
                rl("radiation"),
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
    static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NuclearRadiation.RADIATION_PARTICLE.get(), RadiationParticle.Provider::new);
        event.registerSpriteSet(NuclearRadiation.GAS_CLOUD_PARTICLE.get(), GasCloudParticle.Provider::new);
        event.registerSpriteSet(NuclearRadiation.VOMIT_PARTICLE.get(), VomitParticle.Provider::new);
    }

    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        if (Corium.MOLTEN_CORIUM_TYPE.get() instanceof CoriumFluidType type) {
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public @NonNull ResourceLocation getStillTexture() {
                    return type.getStillTexture();
                }

                @Override
                public @NonNull ResourceLocation getFlowingTexture() {
                    return type.getFlowingTexture();
                }

                @Override
                public int getTintColor() {
                    return type.getTintColor();
                }
            }, type);
        }
    }

    @SubscribeEvent
    static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new DynamicFluidContainerModel.Colors(), Corium.MOLTEN_CORIUM_BUCKET.get());
    }

    @SubscribeEvent
    static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        RadiationScreenLayer.onRegisterShaders(event);
        GlowSilhouette.onRegisterShaders(event);
    }

    @SubscribeEvent
    static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(
                (ResourceManagerReloadListener) rm -> IonizationGlowRenderer.onResourceReload());
    }

    @SubscribeEvent
    static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelowAll(
                rl("radiation_screen_effect"),
                new RadiationScreenLayer());
        event.registerAboveAll(
                rl("radiation_hud"),
                new RadiationHudLayer());
        event.registerAboveAll(
                rl("contamination_hud"),
                new ContaminationHudLayer());
    }

    public static void handleVomit(VomitPayload payload) {
        Minecraft.getInstance().execute(() ->
                VomitEmitter.add(payload.entityId(), payload.stage()));
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
