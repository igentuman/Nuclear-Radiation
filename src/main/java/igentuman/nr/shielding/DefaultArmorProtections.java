package igentuman.nr.shielding;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nr.datagen.RadiationBindingProvider.rec;


public final class DefaultArmorProtections {
    private DefaultArmorProtections() {}

    public static List<ArmorProtectionDefinition> defaults() {
        List<ArmorProtectionDefinition> list = new ArrayList<>();

        addSet(list, "minecraft:iron",      0.04, 0.20, 0.10, 0.01);
        addSet(list, "minecraft:golden",    0.08, 0.22, 0.12, 0.02);
        addSet(list, "minecraft:netherite", 0.10, 0.25, 0.15, 0.04);
        addSet(list, "nuclear_radiation:hazmat", 0.90, 1, 1, 0.92);
        addCustom(list, "nuclearscience:hazmathelmet", 0.9, 1, 1, 0.92);
        addCustom(list, "nuclearscience:hazmatplate", 0.9, 1, 1, 0.92);
        addCustom(list, "nuclearscience:hazmatlegs", 0.9, 1, 1, 0.92);
        addCustom(list, "nuclearscience:hazmatboots", 0.9, 1, 1, 0.92);
        addCustom(list, "nuclearscience:reinforcedhazmathelmet", 0.95, 1, 20, 0.95);
        addCustom(list, "nuclearscience:reinforcedhazmatplate", 0.95, 1, 20, 0.95);
        addCustom(list, "nuclearscience:reinforcedhazmatlegs", 0.95, 1, 20, 0.95);
        addCustom(list, "nuclearscience:reinforcedhazmatboots", 0.95, 1, 20, 0.95);
        addCustom(list, "mekanism:hazmat_mask", 0.9, 1, 1, 0.92);
        addCustom(list, "mekanism:hazmat_gown", 0.9, 1, 1, 0.92);
        addCustom(list, "mekanism:hazmat_pants", 0.9, 1, 1, 0.92);
        addCustom(list, "mekanism:hazmat_boots", 0.9, 1, 1, 0.92);
        addCustom(list, "mekanism:mekasuit_helmet", 0.95, 1, 1, 0.95);
        addCustom(list, "mekanism:mekasuit_bodyarmour", 0.95, 1, 1, 0.95);
        addCustom(list, "mekanism:mekasuit_pants", 0.95, 1, 1, 0.95);
        addCustom(list, "mekanism:mekasuit_boots", 0.95, 1, 1, 0.95);
        addCustom(list, "nuclearcraftneohaul:hazmat_helmet", 0.9, 1, 1, 0.92);
        addCustom(list, "nuclearcraftneohaul:hazmat_chestplate", 0.9, 1, 1, 0.92);
        addCustom(list, "nuclearcraftneohaul:hazmat_leggings", 0.9, 1, 1, 0.92);
        addCustom(list, "nuclearcraftneohaul:hazmat_boots", 0.9, 1, 1, 0.92);
        return list;
    }

    private static void addCustom(List<ArmorProtectionDefinition> list, String material,
                               double xray, double alpha, double beta, double neutron) {
                ResourceLocation loc = rec(material);
                list.add(new ArmorProtectionDefinition(loc.getNamespace()+"_"+loc.getPath(), loc, xray, alpha, beta, neutron));
    }

    private static void addSet(List<ArmorProtectionDefinition> list, String material,
                               double xray, double alpha, double beta, double neutron) {
        for (String piece : new String[]{"helmet", "chestplate", "leggings", "boots"}) {
            String id = material + "_" + piece;
            ResourceLocation loc = rec(id);
            list.add(new ArmorProtectionDefinition(loc.getNamespace()+"_"+loc.getPath(), loc, xray, alpha, beta, neutron));
        }
    }
}
