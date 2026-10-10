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

    public static final DeferredItem<Item> GEIGER_COUNTER = ITEMS.registerItem("geiger_counter", GeigerCounterItem::new);

    public static final DeferredItem<Item> DOSIMETER = ITEMS.registerItem("dosimeter", DosimeterItem::new);

    private NRTools() {}

    public static void register(IEventBus bus) { ITEMS.register(bus); }
}
