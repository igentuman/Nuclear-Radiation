package igentuman.nr.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SlotProviders {

    private static final List<IInventoryRadSlotProvider> PROVIDERS = new ArrayList<>();

    static {
        PROVIDERS.add(new VanillaSlotProvider());
        PROVIDERS.add(new OpenContainerSlotProvider());
    }

    private SlotProviders() {}

    public static void register(IInventoryRadSlotProvider p) {
        PROVIDERS.add(p);
    }

    public static List<IInventoryRadSlotProvider> all() {
        return Collections.unmodifiableList(PROVIDERS);
    }
}
