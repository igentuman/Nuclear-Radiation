package igentuman.nr.integration.jei;

import igentuman.nr.api.shielding.ArmorProtectionRegistry;
import net.minecraft.world.item.ItemStack;

public record ArmorProtectionEntry(ItemStack stack, ArmorProtectionRegistry.Protection protection) {
}
