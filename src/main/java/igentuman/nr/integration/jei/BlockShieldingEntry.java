package igentuman.nr.integration.jei;

import igentuman.nr.shielding.ShieldingRegistry;
import net.minecraft.world.item.ItemStack;

public record BlockShieldingEntry(ItemStack stack, ShieldingRegistry.Coeffs coeffs) {
}
