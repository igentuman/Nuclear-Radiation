package igentuman.nr.integration.jei;

import igentuman.nr.radiation.shielding.world.ShieldingRegistry;
import net.minecraft.world.item.ItemStack;

public record BlockShieldingEntry(ItemStack stack, ShieldingRegistry.Coeffs coeffs) {
}
