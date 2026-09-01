package igentuman.nr.registry;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.items.GuideBookItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NRGuide {

    public static final String GUIDEME_MODID = "guideme";
    public static final ResourceLocation GUIDE_ID = NuclearRadiation.rl("guide");

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NuclearRadiation.MODID);

    public static final DeferredItem<Item> GUIDE_BOOK = ITEMS.register("guide_book",
            () -> new GuideBookItem(new Item.Properties().stacksTo(1)));

    private NRGuide() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(GUIDEME_MODID);
    }

    public static void register(IEventBus bus) {
        if (!isLoaded()) return;
        ITEMS.register(bus);
        guideme.Guide.builder(GUIDE_ID)
                .defaultNamespace(NuclearRadiation.MODID)
                .startPage(NuclearRadiation.rl("guide/index.md"))
                .build();
    }
}
