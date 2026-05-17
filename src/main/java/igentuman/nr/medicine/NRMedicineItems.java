package igentuman.nr.medicine;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.medicine.items.AntiRadInjectionItem;
import igentuman.nr.medicine.items.MedicineUseItem;
import igentuman.nr.medicine.items.RadawayItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NRMedicineItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NuclearRadiation.MODID);

    public static final DeferredItem<Item> IODINE_PILL = ITEMS.register("iodine_pill",
            () -> new MedicineUseItem(new Item.Properties(),
                    () -> NREffects.IODINE_PROTECTION, 30 * 60 * 20, 0));

    public static final DeferredItem<Item> PRUSSIAN_BLUE = ITEMS.register("prussian_blue",
            () -> new MedicineUseItem(new Item.Properties(),
                    () -> NREffects.CESIUM_PURGE, 20 * 60 * 20, 0));

    public static final DeferredItem<Item> RAD_PROTECTION_POTION = ITEMS.register("rad_protection_potion",
            () -> new MedicineUseItem(new Item.Properties(),
                    () -> NREffects.PROTECTION, 3 * 60 * 20, 0));

    public static final DeferredItem<Item> ANTI_RAD_INJECTION = ITEMS.register("anti_rad_injection",
            () -> new AntiRadInjectionItem(new Item.Properties()));

    public static final DeferredItem<Item> RADAWAY = ITEMS.register("radaway",
            () -> new RadawayItem(new Item.Properties()));

    private NRMedicineItems() {}

    public static void register(IEventBus bus) { ITEMS.register(bus); }
}
