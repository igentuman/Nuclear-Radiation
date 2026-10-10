package igentuman.nr.registry;

import igentuman.nr.NuclearRadiation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;

public final class NRArmorItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NuclearRadiation.MODID);

    public static final ResourceKey<EquipmentAsset> HAZMAT_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(NuclearRadiation.MODID, "hazmat"));
    public static final TagKey<Item> REPAIRS_HAZMAT = TagKey.create(
            Registries.ITEM, Identifier.fromNamespaceAndPath(NuclearRadiation.MODID, "repairs_hazmat_armor"));

    public static final ArmorMaterial HAZMAT_MATERIAL = new ArmorMaterial(
            15,
            Map.of(ArmorType.HELMET, 1, ArmorType.CHESTPLATE, 3,
                    ArmorType.LEGGINGS, 2, ArmorType.BOOTS, 1),
            10, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0f, 0.0f,
            REPAIRS_HAZMAT, HAZMAT_ASSET);

    public static final DeferredItem<Item> HAZMAT_HELMET = ITEMS.registerItem("hazmat_helmet", Item::new,
            props -> props.humanoidArmor(HAZMAT_MATERIAL, ArmorType.HELMET));
    public static final DeferredItem<Item> HAZMAT_CHESTPLATE = ITEMS.registerItem("hazmat_chestplate", Item::new,
            props -> props.humanoidArmor(HAZMAT_MATERIAL, ArmorType.CHESTPLATE));
    public static final DeferredItem<Item> HAZMAT_LEGGINGS = ITEMS.registerItem("hazmat_leggings", Item::new,
            props -> props.humanoidArmor(HAZMAT_MATERIAL, ArmorType.LEGGINGS));
    public static final DeferredItem<Item> HAZMAT_BOOTS = ITEMS.registerItem("hazmat_boots", Item::new,
            props -> props.humanoidArmor(HAZMAT_MATERIAL, ArmorType.BOOTS));

    private NRArmorItems() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
