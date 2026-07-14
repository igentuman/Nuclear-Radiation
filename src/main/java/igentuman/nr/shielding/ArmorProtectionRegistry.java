package igentuman.nr.shielding;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ArmorProtectionRegistry {

    public record Protection(double xray, double alpha, double beta, double neutron, boolean protectsFromGas) {
        public static final Protection NONE = new Protection(0, 0, 0, 0, false);
    }

    private static final Map<Item, Protection> BY_ITEM = new HashMap<>();
    private static final Map<TagKey<Item>, Protection> BY_TAG = new HashMap<>();

    private ArmorProtectionRegistry() {}

    public static void register(Item item, Protection p) {
        BY_ITEM.put(item, p);
    }

    public static void registerTag(TagKey<Item> tag, Protection p) {
        BY_TAG.put(tag, p);
    }

    public static void remove(Item item) {
        BY_ITEM.remove(item);
    }

    public static void clear() {
        BY_ITEM.clear();
        BY_TAG.clear();
    }

    public static Map<Item, Protection> all() {
        return Collections.unmodifiableMap(BY_ITEM);
    }

    public static Map<TagKey<Item>, Protection> allTags() {
        return Collections.unmodifiableMap(BY_TAG);
    }

    public static Protection get(ItemStack stack) {
        if (stack.isEmpty()) return Protection.NONE;
        if (stack.getItem() instanceof IRadiationArmor a) {
            return new Protection(a.xrayProtection(), a.alphaProtection(),
                    a.betaProtection(), a.neutronProtection(), a.gasProtection());
        }
        Protection p = BY_ITEM.get(stack.getItem());
        if (p != null) return p;
        for (Map.Entry<TagKey<Item>, Protection> e : BY_TAG.entrySet()) {
            if (stack.is(e.getKey())) return e.getValue();
        }
        return Protection.NONE;
    }

    public static Protection summed(LivingEntity entity) {
        double mxX = 0, mxA = 0, mxB = 0, mxN = 0;
        boolean gasProtection = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) continue;
            ItemStack stack = entity.getItemBySlot(slot);
            Protection p = get(stack);
            mxX = Math.min(1.0, 1.0 - (1.0 - mxX) * (1.0 - p.xray()));
            mxA = Math.min(1.0, 1.0 - (1.0 - mxA) * (1.0 - p.alpha()));
            mxB = Math.min(1.0, 1.0 - (1.0 - mxB) * (1.0 - p.beta()));
            mxN = Math.min(1.0, 1.0 - (1.0 - mxN) * (1.0 - p.neutron()));
            if (slot == EquipmentSlot.HEAD) gasProtection = p.protectsFromGas();
        }
        return new Protection(mxX, mxA, mxB, mxN, gasProtection);
    }
}
