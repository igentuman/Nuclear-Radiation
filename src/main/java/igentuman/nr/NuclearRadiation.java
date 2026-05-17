package igentuman.nr;

import igentuman.nr.config.GeneralConfig;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import igentuman.nr.binding.DefaultBindings;
import igentuman.nr.binding.RadiationBindingsReloadListener;
import igentuman.nr.binding.RadiationComponent;
import igentuman.nr.binding.RadiationTooltip;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.persistence.NRAttachments;
import igentuman.nr.registry.Isotopes;
import igentuman.nr.containers.ContainerEvents;
import igentuman.nr.entity.EntityExposureEvents;
import igentuman.nr.medicine.NREffects;
import igentuman.nr.medicine.NRMedicineItems;
import igentuman.nr.simulation.SimulationEvents;
import igentuman.nr.tools.NRTools;
import igentuman.nr.tracking.RadSourceEvents;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(NuclearRadiation.MODID)
public class NuclearRadiation {
    public static final String MODID = "nuclear_radiation";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    public NuclearRadiation(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        RadiationComponent.register(modEventBus);
        NRAttachments.register(modEventBus);
        NREffects.register(modEventBus);
        NRMedicineItems.register(modEventBus);
        NRTools.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new SimulationEvents());
        NeoForge.EVENT_BUS.register(new RadSourceEvents());
        NeoForge.EVENT_BUS.register(new ContainerEvents());
        NeoForge.EVENT_BUS.register(new EntityExposureEvents());
        NeoForge.EVENT_BUS.register(new RadiationTooltip());

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, GeneralConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, RadiationConfig.SPEC, "nuclear_radiation-radiation.toml");

        Isotopes.bootstrap();
        DefaultBindings.registerDefaults();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Nuclear Radiation common setup");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Nuclear Radiation server starting");
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new RadiationBindingsReloadListener());
    }
}
