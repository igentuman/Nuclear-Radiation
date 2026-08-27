package igentuman.nr.registry;

import igentuman.nr.NuclearRadiation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NRShieldingItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NuclearRadiation.MODID);

    public static final DeferredItem<Item> RAD_SHIELDING_LIGHT  = ITEMS.register("rad_shielding_light",  () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RAD_SHIELDING_MEDIUM = ITEMS.register("rad_shielding_medium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RAD_SHIELDING_HEAVY  = ITEMS.register("rad_shielding_heavy",  () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RAD_SHIELDING_DPS    = ITEMS.register("rad_shielding_dps",    () -> new Item(new Item.Properties()));

    private NRShieldingItems() {}

    public static void register(IEventBus bus) { ITEMS.register(bus); }
}
