package igentuman.nr.compat.ntm;

import com.hbm.handler.HazmatRegistry;
import com.hbm.util.ContaminationUtil;
import igentuman.nr.api.shielding.ArmorProtectionRegistry.Protection;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Only reached through the mod-presence guard in NtmCompatibility. */
final class NtmArmorAdapter {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private NtmArmorAdapter() {}

    static Protection piece(ItemStack stack) {
        double protection = NtmUnits.resistanceProtection(HazmatRegistry.getResistance(stack));
        return new Protection(protection, protection, protection, protection, false);
    }

    static double additionalProtection(LivingEntity entity) {
        // HazmatRegistry's entity overload includes RadX and a native player
        // modifier as well as armor. Armor has already been converted per piece.
        double resistance = HazmatRegistry.getResistance(entity);
        if (entity instanceof Player) {
            float wornResistance = 0.0F;
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                wornResistance += (float) HazmatRegistry.getResistance(entity.getItemBySlot(slot));
            }
            resistance -= wornResistance;
        }
        return NtmUnits.resistanceProtection(Math.max(0.0, resistance));
    }

    static boolean immune(LivingEntity entity) {
        return ContaminationUtil.isRadImmune(entity);
    }

    static double claddingProtection(ItemStack stack) {
        return NtmUnits.resistanceProtection(HazmatRegistry.getCladding(stack));
    }
}
