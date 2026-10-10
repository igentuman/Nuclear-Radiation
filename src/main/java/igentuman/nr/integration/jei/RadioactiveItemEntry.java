package igentuman.nr.integration.jei;

import igentuman.nr.api.RadiationProfile;
import net.minecraft.world.item.ItemStack;

public record RadioactiveItemEntry(ItemStack stack, RadiationProfile profile) {
}
