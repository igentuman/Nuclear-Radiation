package igentuman.nr.api.shielding;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ShieldingUpgradeRegistry {

    private static final Map<Item, Double> BY_ITEM = new HashMap<>();

    private ShieldingUpgradeRegistry() {}

    public static void register(Item item, double value) { BY_ITEM.put(item, value); }

    public static void clear() { BY_ITEM.clear(); }

    public static double get(ItemStack stack) {
        return BY_ITEM.getOrDefault(stack.getItem(), 0.0);
    }

    public static Map<Item, Double> all() {
        return Collections.unmodifiableMap(BY_ITEM);
    }
}
