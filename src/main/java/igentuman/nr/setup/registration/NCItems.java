package igentuman.nr.setup.registration;

import igentuman.nr.item.*;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static igentuman.nr.setup.registration.Registries.ITEMS;

public class NCItems {

    public static HashMap<String, RegistryObject<Item>> ALL_NC_ITEMS = new HashMap<>();

    public static HashMap<String, RegistryObject<Item>> NC_PARTS = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> NC_SHIELDING = new HashMap<>();
    public static HashMap<String, RegistryObject<Item>> NC_ITEMS = new HashMap<>();

    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties();
    public static final Item.Properties ONE_ITEM_PROPERTIES = new Item.Properties().stacksTo(1);
    public static final Item.Properties HAZMAT_PROPS = new Item.Properties().stacksTo(1).durability(250);

    public static final RegistryObject<Item> HAZMAT_MASK =
            ITEMS.register("hazmat_mask", () -> new HazmatItem(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, HAZMAT_PROPS));
    public static final RegistryObject<Item> HAZMAT_CHEST =
            ITEMS.register("hazmat_chest", () -> new HazmatItem(ArmorMaterials.LEATHER, ArmorItem.Type.CHESTPLATE, HAZMAT_PROPS));
    public static final RegistryObject<Item> HAZMAT_BOOTS =
            ITEMS.register("hazmat_boots", () -> new HazmatItem(ArmorMaterials.LEATHER, ArmorItem.Type.BOOTS, HAZMAT_PROPS));
    public static final RegistryObject<Item> HAZMAT_PANTS =
            ITEMS.register("hazmat_pants", () -> new HazmatItem(ArmorMaterials.LEATHER, ArmorItem.Type.LEGGINGS, HAZMAT_PROPS));

    public static final RegistryObject<Item> GEIGER_COUNTER = ITEMS.register("geiger_counter", () -> new GeigerCounterItem(ONE_ITEM_PROPERTIES));

    public static RegistryObject<Item> registerItem(String name) {
        return ITEMS.register(name, () -> new Item(ITEM_PROPERTIES));
    }

    public static void init() {
        registerItems();
        registerShielding();
    }

    private static void registerItems() {
        List<String> items = Arrays.asList(
                "dosimeter"
        );
        for(String name: items) {
            if(name.equals("dosimeter")) {
                NC_ITEMS.put(name, ITEMS.register(name, () -> new DosimiterItem(ONE_ITEM_PROPERTIES)));
            } else {
                NC_ITEMS.put(name, registerItem(name));
            }
            ALL_NC_ITEMS.put(name, NC_ITEMS.get(name));
        }
    }

    private static void registerShielding() {
        List<String> parts = Arrays.asList(
                "light",
                "medium",
                "heavy",
                "dps"
        );
        int i = 1;
        for(String name: parts) {
            int finalI = i;
            NC_SHIELDING.put(name, ITEMS.register(name, () -> new RadShieldingItem(ITEM_PROPERTIES, finalI)));
            i+=2;
            ALL_NC_ITEMS.put(name, NC_SHIELDING.get(name));
        }
    }

}
