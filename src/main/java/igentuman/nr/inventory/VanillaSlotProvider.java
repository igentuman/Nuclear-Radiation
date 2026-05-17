package igentuman.nr.inventory;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class VanillaSlotProvider implements IInventoryRadSlotProvider {

    @Override
    public Stream<RadiatedSlot> slots(LivingEntity entity) {
        List<RadiatedSlot> out = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) continue;
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty()) out.add(new RadiatedSlot(stack, SlotFactors.WORN_ARMOR, true));
        }

        ItemStack offhand = entity.getOffhandItem();
        if (!offhand.isEmpty()) out.add(new RadiatedSlot(offhand, SlotFactors.OFFHAND, false));

        if (entity instanceof Player player) {
            Inventory inv = player.getInventory();
            int selected = inv.selected;
            for (int i = 0; i < inv.items.size(); i++) {
                ItemStack stack = inv.items.get(i);
                if (stack.isEmpty()) continue;
                double factor;
                if (i == selected) factor = SlotFactors.HELD_MAIN;
                else if (i < Inventory.getSelectionSize()) factor = SlotFactors.HOTBAR_OTHER;
                else factor = SlotFactors.MAIN_INV;
                out.add(new RadiatedSlot(stack, factor, false));
            }
        } else {
            ItemStack main = entity.getMainHandItem();
            if (!main.isEmpty()) out.add(new RadiatedSlot(main, SlotFactors.HELD_MAIN, false));
        }

        return out.stream();
    }
}
