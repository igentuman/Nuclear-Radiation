package igentuman.nr.radiation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.HashMap;
import static igentuman.nr.NuclearRadiation.MODID;
import static igentuman.nr.handler.config.RadiationConfig.RADIATION_CONFIG;
import static net.minecraft.world.item.Items.AIR;

public class ItemRadiation {
    protected static HashMap<Item, Double> radiationMap = new HashMap<>();
    protected static boolean initialized = false;
    public static HashMap<Item, Double> get()
    {
        return radiationMap;
    }

    public static void init()
    {
        if(!radiationMap.isEmpty()) {
            return;
        }
        for(String line: RADIATION_CONFIG.ITEM_RADIATION.get()) {
            String[] split = line.split("\\|");
            if(split.length != 2) {
                continue;
            }
            Item item = getItemByName(split[0].trim());
            if(item.equals(AIR)) {
                continue;
            }
            try {
                radiationMap.put(item, Double.parseDouble(split[1].trim())/1000000000);
            } catch (NumberFormatException ignored) {}
        }


    }

    public static void add(String item, double radiation)
    {
        Item toAdd = getItemByName(item);
        if(toAdd.equals(AIR)) {
            return;
        }
        radiationMap.put(toAdd, radiation);
    }

    public static void add(Item item, double radiation)
    {
        radiationMap.put(item, radiation);
    }

    public static Item getItemByName(String name)
    {
        if(!name.contains(":")) {
            name = MODID +":" + name;
        }
        ResourceLocation itemKey = new ResourceLocation(name.replace("/", "_"));
        return ForgeRegistries.ITEMS.getValue(itemKey);
    }

    public static double byItem(Item item) {
        if(item instanceof AirItem) {
            return 0;
        }
        if(!initialized) {
            init();
            initialized = true;
        }
        if(radiationMap.containsKey(item)) {
            return radiationMap.get(item);
        }
        return 0;
    }
}
