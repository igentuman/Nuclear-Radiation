package igentuman.nr;

import igentuman.nr.block.CreativeRadSourceBlock;
import igentuman.nr.block.CreativeRadSourceBlockEntity;
import igentuman.nr.config.GeneralConfig;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import igentuman.nr.binding.DefaultBindings;
import igentuman.nr.binding.RadiationBindingsReloadListener;
import igentuman.nr.binding.RadiationComponent;
import igentuman.nr.binding.RadiationTooltip;
import igentuman.nr.command.NRCommands;
import igentuman.nr.config.NRClientConfig;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.util.persistence.NRAttachments;
import igentuman.nr.registry.Isotopes;
import igentuman.nr.containers.ContainerEvents;
import igentuman.nr.entity.EntityExposureEvents;
import igentuman.nr.medicine.NREffects;
import igentuman.nr.medicine.NRMedicineItems;
import igentuman.nr.recipe.NRRecipes;
import igentuman.nr.shielding.ArmorProtectionReloadListener;
import igentuman.nr.shielding.ShieldingBindingsReloadListener;
import igentuman.nr.simulation.SimulationEvents;
import igentuman.nr.tools.NRTools;
import igentuman.nr.util.tracking.RadSourceEvents;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(NuclearRadiation.MODID)
public class NuclearRadiation {
    public static final String MODID = "nuclear_radiation";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final DeferredBlock<Block> CREATIVE_RAD_SOURCE_BLOCK = BLOCKS.register(
            "creative_rad_source",
            () -> new CreativeRadSourceBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final DeferredItem<Item> CREATIVE_RAD_SOURCE_ITEM = ITEMS.register(
            "creative_rad_source",
            () -> new BlockItem(CREATIVE_RAD_SOURCE_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeRadSourceBlockEntity>> CREATIVE_RAD_SOURCE_BE =
            BLOCK_ENTITIES.register("creative_rad_source",
                    () -> BlockEntityType.Builder
                            .of(CreativeRadSourceBlockEntity::new, CREATIVE_RAD_SOURCE_BLOCK.get())
                            .build(null));

    public NuclearRadiation(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        RadiationComponent.register(modEventBus);
        NRAttachments.register(modEventBus);
        NREffects.register(modEventBus);
        NRMedicineItems.register(modEventBus);
        NRTools.register(modEventBus);
        NRSounds.register(modEventBus);
        NRRecipes.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new SimulationEvents());
        NeoForge.EVENT_BUS.register(new RadSourceEvents());
        NeoForge.EVENT_BUS.register(new ContainerEvents());
        NeoForge.EVENT_BUS.register(new EntityExposureEvents());
        NeoForge.EVENT_BUS.register(new RadiationTooltip());

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, GeneralConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, RadiationConfig.SPEC, "nuclear_radiation-radiation.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, NRClientConfig.SPEC, "nuclear_radiation-client.toml");

        Isotopes.bootstrap();
        DefaultBindings.registerDefaults();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Nuclear Radiation common setup");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(NRTools.GEIGER_COUNTER);
            event.accept(NRTools.DOSIMETER);
            event.accept(CREATIVE_RAD_SOURCE_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(NRMedicineItems.IODINE_PILL);
            event.accept(NRMedicineItems.PRUSSIAN_BLUE);
            event.accept(NRMedicineItems.RAD_PROTECTION_POTION);
            event.accept(NRMedicineItems.RAD_PROTECTION_POTION_2);
            event.accept(NRMedicineItems.ANTI_RAD_INJECTION);
            event.accept(NRMedicineItems.RADAWAY);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Nuclear Radiation server starting");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        NRCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new RadiationBindingsReloadListener());
        event.addListener(new ShieldingBindingsReloadListener());
        event.addListener(new ArmorProtectionReloadListener());
    }
}
