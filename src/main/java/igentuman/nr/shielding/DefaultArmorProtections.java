package igentuman.nr.shielding;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static igentuman.nr.datagen.RadiationBindingProvider.rec;


public final class DefaultArmorProtections {
    private DefaultArmorProtections() {}

    public static List<ArmorProtectionDefinition> defaults() {
        List<ArmorProtectionDefinition> list = new ArrayList<>();

        addSet(list, "minecraft:iron",      0.04, 0.20, 0.10, 0.01, false);
        addSet(list, "minecraft:golden",    0.08, 0.22, 0.12, 0.02, false);
        addSet(list, "minecraft:netherite", 0.10, 0.25, 0.15, 0.04, false);
        addSet(list, "nuclear_radiation:hazmat", 0.90, 1, 1, 0.92, false);
        addCustom(list, "creatingspace:basic_spacesuit_helmet", 0.72, 1, 1, 0.72, true);
        addCustom(list, "creatingspace:copper_oxygen_backtank", 0.42, 1, 1, 0.42, false);
        addCustom(list, "creatingspace:basic_spacesuit_leggings", 0.72, 1, 1, 0.72, false);
        addCustom(list, "creatingspace:basic_spacesuit_boots", 0.72, 1, 1, 0.72, false);
        addCustom(list, "creatingspace:advanced_spacesuit_helmet", 0.85, 1, 1, 0.85, true);
        addCustom(list, "creatingspace:netherite_oxygen_backtank", 0.55, 1, 1, 0.55, false);
        addCustom(list, "creatingspace:advanced_spacesuit_leggings", 0.85, 1, 1, 0.85, false);
        addCustom(list, "creatingspace:advanced_spacesuit_boots", 0.85, 1, 1, 0.85, false);
        addCustom(list, "nuclearscience:hazmathelmet", 0.9, 1, 1, 0.92, true);
        addCustom(list, "nuclearscience:hazmatplate", 0.9, 1, 1, 0.92, false);
        addCustom(list, "nuclearscience:hazmatlegs", 0.9, 1, 1, 0.92, false);
        addCustom(list, "nuclearscience:hazmatboots", 0.9, 1, 1, 0.92, false);
        addCustom(list, "nuclearscience:reinforcedhazmathelmet", 0.95, 1, 20, 0.95, true);
        addCustom(list, "nuclearscience:reinforcedhazmatplate", 0.95, 1, 20, 0.95, false);
        addCustom(list, "nuclearscience:reinforcedhazmatlegs", 0.95, 1, 20, 0.95, false);
        addCustom(list, "nuclearscience:reinforcedhazmatboots", 0.95, 1, 20, 0.95, false);
        addCustom(list, "mekanism:hazmat_mask", 0.9, 1, 1, 0.92, true);
        addCustom(list, "mekanism:hazmat_gown", 0.9, 1, 1, 0.92, false);
        addCustom(list, "mekanism:hazmat_pants", 0.9, 1, 1, 0.92, false);
        addCustom(list, "mekanism:hazmat_boots", 0.9, 1, 1, 0.92, false);
        addCustom(list, "mekanism:mekasuit_helmet", 0.95, 1, 1, 0.95, true);
        addCustom(list, "mekanism:mekasuit_bodyarmour", 0.95, 1, 1, 0.95, false);
        addCustom(list, "mekanism:mekasuit_pants", 0.95, 1, 1, 0.95, false);
        addCustom(list, "mekanism:mekasuit_boots", 0.95, 1, 1, 0.95, false);
        addCustom(list, "nuclearcraftneohaul:hazmat_helmet", 0.9, 1, 1, 0.92, true);
        addCustom(list, "nuclearcraftneohaul:hazmat_chestplate", 0.9, 1, 1, 0.92, false);
        addCustom(list, "nuclearcraftneohaul:hazmat_leggings", 0.9, 1, 1, 0.92, false);
        addCustom(list, "nuclearcraftneohaul:hazmat_boots", 0.9, 1, 1, 0.92, false);
        addCustom(list, "createnucleartech:basic_hazmat_helmet",      0.80, 1, 1, 0.82, true);
        addCustom(list, "createnucleartech:basic_hazmat_chestplate",  0.80, 1, 1, 0.82, false);
        addCustom(list, "createnucleartech:basic_hazmat_leggings",    0.80, 1, 1, 0.82, false);
        addCustom(list, "createnucleartech:basic_hazmat_boots",       0.80, 1, 1, 0.82, false);
        addCustom(list, "createnucleartech:advanced_hazmat_helmet",     0.88, 1, 1, 0.88, true);
        addCustom(list, "createnucleartech:advanced_hazmat_chestplate", 0.88, 1, 1, 0.88, false);
        addCustom(list, "createnucleartech:advanced_hazmat_leggings",   0.88, 1, 1, 0.88, false);
        addCustom(list, "createnucleartech:advanced_hazmat_boots",      0.88, 1, 1, 0.88, false);
        addCustom(list, "createnucleartech:reinforced_hazmat_helmet",     0.92, 1, 1, 0.92, true);
        addCustom(list, "createnucleartech:reinforced_hazmat_chestplate", 0.92, 1, 1, 0.92, false);
        addCustom(list, "createnucleartech:reinforced_hazmat_leggings",   0.92, 1, 1, 0.92, false);
        addCustom(list, "createnucleartech:reinforced_hazmat_boots",      0.92, 1, 1, 0.92, false);
        addCustom(list, "createnucleartech:elite_hazmat_helmet",     0.95, 1, 1, 0.95, true);
        addCustom(list, "createnucleartech:elite_hazmat_chestplate", 0.95, 1, 1, 0.95, false);
        addCustom(list, "createnucleartech:elite_hazmat_leggings",   0.95, 1, 1, 0.95, false);
        addCustom(list, "createnucleartech:elite_hazmat_boots",      0.95, 1, 1, 0.95, false);
        return list;
    }

    public static Map<TagKey<Item>, ArmorProtectionRegistry.Protection> tagDefaults() {
        Map<TagKey<Item>, ArmorProtectionRegistry.Protection> map = new LinkedHashMap<>();
        addTag(map, "createnuclear:anti_radiation_helmet_dye",     0.85, 1.0, 1.0, 0.85, true);
        addTag(map, "createnuclear:anti_radiation_chestplate_dye", 0.85, 1.0, 1.0, 0.85, false);
        addTag(map, "createnuclear:anti_radiation_leggings_dye",   0.85, 1.0, 1.0, 0.85, false);
        addTag(map, "createnuclear:anti_radiation_boots_dye",      0.85, 1.0, 1.0, 0.85, false);
        return map;
    }

    private static void addTag(Map<TagKey<Item>, ArmorProtectionRegistry.Protection> map, String tag,
                               double xray, double alpha, double beta, double neutron, boolean protectsFromGas) {
        TagKey<Item> key = TagKey.create(Registries.ITEM, ResourceLocation.parse(tag));
        map.put(key, new ArmorProtectionRegistry.Protection(xray, alpha, beta, neutron, protectsFromGas));
    }

    private static void addCustom(List<ArmorProtectionDefinition> list, String material,
                               double xray, double alpha, double beta, double neutron, boolean protectsFromGas) {
                ResourceLocation loc = rec(material);
                list.add(new ArmorProtectionDefinition(loc.getNamespace()+"_"+loc.getPath(), loc, xray, alpha, beta, neutron, protectsFromGas));
    }

    private static void addSet(List<ArmorProtectionDefinition> list, String material,
                               double xray, double alpha, double beta, double neutron, boolean protectsFromGas) {
        for (String piece : new String[]{"helmet", "chestplate", "leggings", "boots"}) {
            String id = material + "_" + piece;
            if(id.equals("hazmat_helmet")) {
                protectsFromGas = true;
            }
            ResourceLocation loc = rec(id);
            list.add(new ArmorProtectionDefinition(loc.getNamespace()+"_"+loc.getPath(), loc, xray, alpha, beta, neutron, protectsFromGas));
        }
    }
}
