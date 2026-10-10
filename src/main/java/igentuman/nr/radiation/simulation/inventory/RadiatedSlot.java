package igentuman.nr.radiation.simulation.inventory;

import net.minecraft.world.item.ItemStack;

public record RadiatedSlot(ItemStack stack, double slotFactor, boolean wornOnBody) {
}
