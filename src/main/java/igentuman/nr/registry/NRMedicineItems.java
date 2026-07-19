package igentuman.nr.registry;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.items.AntiRadInjectionItem;
import igentuman.nr.items.MedicineInstantItem;
import igentuman.nr.items.RadawayItem;
import igentuman.nr.registry.NREffects;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NRMedicineItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NuclearRadiation.MODID);

    public static final DeferredItem<Item> IODINE_PILL = ITEMS.register("iodine_pill",
            () -> new MedicineInstantItem(new Item.Properties(), 0.5, 600, 0,
                    () -> NREffects.IODINE_PROTECTION));

    public static final DeferredItem<Item> PRUSSIAN_BLUE = ITEMS.register("prussian_blue",
            () -> new MedicineInstantItem(new Item.Properties(), 1.0, 2400, 1,
                    () -> NREffects.CESIUM_PURGE));

    public static final DeferredItem<Item> RAD_PROTECTION_POTION = ITEMS.register("rad_protection_potion",
            () -> new MedicineInstantItem(new Item.Properties(), 0.0, 6000, 0));

    public static final DeferredItem<Item> RAD_PROTECTION_POTION_2 = ITEMS.register("rad_protection_potion_2",
            () -> new MedicineInstantItem(new Item.Properties(), 0.0, 12000, 1));

    public static final DeferredItem<Item> ANTI_RAD_INJECTION = ITEMS.register("anti_rad_injection",
            () -> new AntiRadInjectionItem(new Item.Properties()));

    public static final DeferredItem<Item> RADAWAY = ITEMS.register("radaway",
            () -> new RadawayItem(new Item.Properties()));

    private NRMedicineItems() {}

    public static void register(IEventBus bus) { ITEMS.register(bus); }
}
