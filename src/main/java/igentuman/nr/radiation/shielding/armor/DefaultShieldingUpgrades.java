package igentuman.nr.radiation.shielding.armor;

import igentuman.nr.api.shielding.ShieldingUpgradeDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nr.datagen.RadiationBindingProvider.rec;

public final class DefaultShieldingUpgrades {
    private DefaultShieldingUpgrades() {}

    public static List<ShieldingUpgradeDefinition> defaults() {
        List<ShieldingUpgradeDefinition> list = new ArrayList<>();
        add(list, "nuclear_radiation:rad_shielding_light",  0.02);
        add(list, "nuclear_radiation:rad_shielding_medium", 0.04);
        add(list, "nuclear_radiation:rad_shielding_heavy",  0.07);
        add(list, "nuclear_radiation:rad_shielding_dps",   0.12);
        return list;
    }

    private static void add(List<ShieldingUpgradeDefinition> list, String id, double value) {
        ResourceLocation loc = rec(id);
        list.add(new ShieldingUpgradeDefinition(loc.getNamespace() + "_" + loc.getPath(), loc, value));
    }
}
