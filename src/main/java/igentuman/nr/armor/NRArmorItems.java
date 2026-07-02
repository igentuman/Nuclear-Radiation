package igentuman.nr.armor;

import igentuman.nr.NuclearRadiation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public final class NRArmorItems {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, NuclearRadiation.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(NuclearRadiation.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HAZMAT_MATERIAL =
            ARMOR_MATERIALS.register("hazmat", () -> new ArmorMaterial(
                    Map.of(
                            ArmorItem.Type.HELMET,     1,
                            ArmorItem.Type.CHESTPLATE, 3,
                            ArmorItem.Type.LEGGINGS,   2,
                            ArmorItem.Type.BOOTS,      1
                    ),
                    10,
                    SoundEvents.ARMOR_EQUIP_GENERIC,
                    () -> Ingredient.of(Items.PHANTOM_MEMBRANE),
                    List.of(new ArmorMaterial.Layer(
                            ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "hazmat")
                    )),
                    0.0f,
                    0.0f
            ));

    public static final DeferredItem<ArmorItem> HAZMAT_HELMET = ITEMS.register("hazmat_helmet",
            () -> new ArmorItem(HAZMAT_MATERIAL, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(15))));

    public static final DeferredItem<ArmorItem> HAZMAT_CHESTPLATE = ITEMS.register("hazmat_chestplate",
            () -> new ArmorItem(HAZMAT_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(15))));

    public static final DeferredItem<ArmorItem> HAZMAT_LEGGINGS = ITEMS.register("hazmat_leggings",
            () -> new ArmorItem(HAZMAT_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(15))));

    public static final DeferredItem<ArmorItem> HAZMAT_BOOTS = ITEMS.register("hazmat_boots",
            () -> new ArmorItem(HAZMAT_MATERIAL, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(15))));

    private NRArmorItems() {}

    public static void register(IEventBus bus) {
        ARMOR_MATERIALS.register(bus);
        ITEMS.register(bus);
    }
}
