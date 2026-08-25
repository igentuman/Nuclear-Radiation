package igentuman.nr.radiation.source;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.block.CoriumFluidBlock;
import igentuman.nr.registry.CoriumFluidType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Self-contained corium registration: the molten corium fluid (type, source, flowing block, bucket)
 * and the solid {@code corium_block} it quenches/solidifies into. Owns its own deferred registers so
 * corium stays independent of the rest of the mod.
 */
public final class Corium {

    public static final int COLOR = 0xFF7C7C6F;

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, NuclearRadiation.MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, NuclearRadiation.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NuclearRadiation.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NuclearRadiation.MODID);

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_CORIUM_TYPE =
            FLUID_TYPES.register("molten_corium", () -> new CoriumFluidType(
                    FluidType.Properties.create()
                            .temperature(3000)
                            .density(3000)
                            .viscosity(6000)
                            .lightLevel(15)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_CORIUM =
            FLUIDS.register("molten_corium", () -> new BaseFlowingFluid.Source(coriumProps()));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_MOLTEN_CORIUM =
            FLUIDS.register("flowing_molten_corium", () -> new BaseFlowingFluid.Flowing(coriumProps()));

    public static final DeferredBlock<CoriumFluidBlock> MOLTEN_CORIUM_BLOCK =
            BLOCKS.register("molten_corium_block", () -> new CoriumFluidBlock(
                    MOLTEN_CORIUM.get(),
                    BlockBehaviour.Properties.of()
                            .noCollission()
                            .strength(100.0F)
                            .noLootTable()
                            .liquid()
                            .replaceable()));

    public static final DeferredItem<BucketItem> MOLTEN_CORIUM_BUCKET =
            ITEMS.register("molten_corium_bucket", () -> new BucketItem(
                    MOLTEN_CORIUM.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredBlock<Block> CORIUM_BLOCK =
            BLOCKS.register("corium_block", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(5.0F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    public static final DeferredItem<BlockItem> CORIUM_BLOCK_ITEM =
            ITEMS.register("corium_block", () -> new BlockItem(CORIUM_BLOCK.get(), new Item.Properties()));

    private Corium() {}

    // Fresh Properties per call; holders resolve lazily so the source <-> flowing <-> block <-> bucket
    // cycle is safe. Lava-like spread: slower ticks, shorter reach, faster level drop-off.
    private static BaseFlowingFluid.Properties coriumProps() {
        return new BaseFlowingFluid.Properties(MOLTEN_CORIUM_TYPE::get, MOLTEN_CORIUM, FLOWING_MOLTEN_CORIUM)
                .block(MOLTEN_CORIUM_BLOCK)
                .bucket(MOLTEN_CORIUM_BUCKET)
                .tickRate(60)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2);
    }

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
