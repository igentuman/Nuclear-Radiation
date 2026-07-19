package igentuman.nr.radiation.simulation.inventory;

import igentuman.nr.api.IInventoryRadSlotProvider;
import igentuman.nr.radiation.simulation.containers.OpenContainerRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class OpenContainerSlotProvider implements IInventoryRadSlotProvider {

    @Override
    public Stream<RadiatedSlot> slots(LivingEntity entity) {
        if (!(entity instanceof Player player)) return Stream.empty();
        AbstractContainerMenu menu = OpenContainerRegistry.current(player.getUUID());
        if (menu == null) return Stream.empty();
        Inventory inv = player.getInventory();
        List<RadiatedSlot> out = new ArrayList<>();
        for (Slot slot : menu.slots) {
            Container source = slot.container;
            if (source == inv) continue;
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;
            out.add(new RadiatedSlot(stack, SlotFactors.OPEN_CONTAINER, false));
        }
        return out.stream();
    }
}
