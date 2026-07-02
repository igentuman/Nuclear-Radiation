package igentuman.nr.shielding;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ArmorProtectionRegistry {

    public record Protection(double xray, double alpha, double beta, double neutron) {
        public static final Protection NONE = new Protection(0, 0, 0, 0);
    }

    private static final Map<Item, Protection> BY_ITEM = new HashMap<>();

    private ArmorProtectionRegistry() {}

    public static void register(Item item, Protection p) {
        BY_ITEM.put(item, p);
    }

    public static void remove(Item item) {
        BY_ITEM.remove(item);
    }

    public static void clear() {
        BY_ITEM.clear();
    }

    public static Map<Item, Protection> all() {
        return Collections.unmodifiableMap(BY_ITEM);
    }

    public static Protection get(ItemStack stack) {
        if (stack.isEmpty()) return Protection.NONE;
        if (stack.getItem() instanceof IRadiationArmor a) {
            return new Protection(a.xrayProtection(), a.alphaProtection(),
                    a.betaProtection(), a.neutronProtection());
        }
        Protection p = BY_ITEM.get(stack.getItem());
        return p != null ? p : Protection.NONE;
    }

    public static Protection summed(LivingEntity entity) {
        double mxX = 0, mxA = 0, mxB = 0, mxN = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) continue;
            ItemStack stack = entity.getItemBySlot(slot);
            Protection p = get(stack);
            mxX = Math.min(1.0, 1.0 - (1.0 - mxX) * (1.0 - p.xray()));
            mxA = Math.min(1.0, 1.0 - (1.0 - mxA) * (1.0 - p.alpha()));
            mxB = Math.min(1.0, 1.0 - (1.0 - mxB) * (1.0 - p.beta()));
            mxN = Math.min(1.0, 1.0 - (1.0 - mxN) * (1.0 - p.neutron()));
        }
        return new Protection(mxX, mxA, mxB, mxN);
    }
}
