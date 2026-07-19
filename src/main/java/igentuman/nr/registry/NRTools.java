package igentuman.nr.registry;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.items.DosimeterItem;
import igentuman.nr.items.GeigerCounterItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NRTools {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NuclearRadiation.MODID);

    public static final DeferredItem<Item> GEIGER_COUNTER = ITEMS.register("geiger_counter",
            () -> new GeigerCounterItem(new Item.Properties()));

    public static final DeferredItem<Item> DOSIMETER = ITEMS.register("dosimeter",
            () -> new DosimeterItem(new Item.Properties()));

    private NRTools() {}

    public static void register(IEventBus bus) { ITEMS.register(bus); }
}
