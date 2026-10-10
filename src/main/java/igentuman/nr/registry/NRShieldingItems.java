package igentuman.nr.registry;

import igentuman.nr.NuclearRadiation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NRShieldingItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NuclearRadiation.MODID);

    public static final DeferredItem<Item> RAD_SHIELDING_LIGHT  = ITEMS.registerItem("rad_shielding_light", Item::new);
    public static final DeferredItem<Item> RAD_SHIELDING_MEDIUM = ITEMS.registerItem("rad_shielding_medium", Item::new);
    public static final DeferredItem<Item> RAD_SHIELDING_HEAVY  = ITEMS.registerItem("rad_shielding_heavy", Item::new);
    public static final DeferredItem<Item> RAD_SHIELDING_DPS    = ITEMS.registerItem("rad_shielding_dps", Item::new);

    private NRShieldingItems() {}

    public static void register(IEventBus bus) { ITEMS.register(bus); }
}
