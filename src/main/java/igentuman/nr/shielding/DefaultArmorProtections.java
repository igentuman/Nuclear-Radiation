package igentuman.nr.shielding;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class DefaultArmorProtections {
    private DefaultArmorProtections() {}

    public static List<ArmorProtectionDefinition> defaults() {
        List<ArmorProtectionDefinition> list = new ArrayList<>();

        addSet(list, "iron",      0.04, 0.20, 0.10, 0.01);
        addSet(list, "golden",    0.08, 0.22, 0.12, 0.02);
        addSet(list, "netherite", 0.10, 0.25, 0.15, 0.04);

        return list;
    }

    private static void addSet(List<ArmorProtectionDefinition> list, String material,
                               double xray, double alpha, double beta, double neutron) {
        for (String piece : new String[]{"helmet", "chestplate", "leggings", "boots"}) {
            String id = material + "_" + piece;
            list.add(new ArmorProtectionDefinition(
                    id,
                    ResourceLocation.fromNamespaceAndPath("minecraft", id),
                    xray, alpha, beta, neutron));
        }
    }
}
