package igentuman.nr.setup;

import igentuman.nr.client.particle.RadiationParticle;
import igentuman.nr.client.sound.SoundHandler;
import igentuman.nr.handler.event.client.*;
import igentuman.nr.client.ClientRadiationData;
import igentuman.nr.client.RadiationOverlay;
import igentuman.nr.client.WhiteNoiseOverlay;
import igentuman.nr.setup.registration.NcParticleTypes;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import static igentuman.nr.NuclearRadiation.MODID;
import static igentuman.nr.NuclearRadiation.rl;
import static igentuman.nr.setup.registration.NCItems.GEIGER_COUNTER;
import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.addListener(LOWEST, SoundHandler::onTilePlaySound);
        });


        event.enqueueWork(() -> {
            setPropertyOverride(GEIGER_COUNTER.get(), rl("radiation"), (stack, world, entity, seed) -> {
                if (entity instanceof Player) {
                    if(!((Player) entity).getInventory().contains(new ItemStack(GEIGER_COUNTER.get()))) return 0;
                    ClientRadiationData.setCurrentChunk(entity.chunkPosition().x, entity.chunkPosition().z);
                    return (int)((float)ClientRadiationData.getCurrentWorldRadiation()/400000);
                }
                return 0;
            });
        });
    }

    public static void setPropertyOverride(ItemLike itemProvider, ResourceLocation override, ItemPropertyFunction propertyGetter) {
        ItemProperties.register(itemProvider.asItem(), override, propertyGetter);
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("radiation_bar", RadiationOverlay.RADIATION_BAR);
        event.registerAboveAll("white_noise", WhiteNoiseOverlay.WHITE_NOISE);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NcParticleTypes.RADIATION.get(), RadiationParticle.Factory::new);
    }

    public static void setup() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
    }

    public static void registerEventHandlers(FMLClientSetupEvent event) {
        InputEvents.register(event);
        ColorHandler.register(event);
        ServerLoad.register(event);
        RecipesUpdated.register(event);
        TagsUpdated.register(event);
        TooltipHandler.register(event);
        TickHandler.register(event);
        BlockOverlayHandler.register(event);
    }
}
