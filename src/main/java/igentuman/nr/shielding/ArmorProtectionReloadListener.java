package igentuman.nr.shielding;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import igentuman.nr.NuclearRadiation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

import java.util.Map;

public class ArmorProtectionReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    public static final String FOLDER = "nuclear_radiation/armor_protection";

    public ArmorProtectionReloadListener() {
        super(GSON, FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager mgr, ProfilerFiller profiler) {
        ArmorProtectionRegistry.clear();
        DefaultArmorProtections.defaults().forEach(ArmorProtectionReloadListener::applyDefinition);

        for (Map.Entry<ResourceLocation, JsonElement> e : map.entrySet()) {
            try {
                JsonObject obj = e.getValue().getAsJsonObject();
                ResourceLocation target = ResourceLocation.parse(obj.get("target").getAsString());
                double xray = readDouble(obj, "xray");
                double alpha = readDouble(obj, "alpha");
                double beta = readDouble(obj, "beta");
                double neutron = readDouble(obj, "neutron");

                Item item = BuiltInRegistries.ITEM.get(target);
                if (item == null) {
                    NuclearRadiation.LOGGER.warn("Armor protection target not found: {}", target);
                    continue;
                }
                ArmorProtectionRegistry.register(item,
                        new ArmorProtectionRegistry.Protection(xray, alpha, beta, neutron));
            } catch (Exception ex) {
                NuclearRadiation.LOGGER.error("Failed to load armor protection {}", e.getKey(), ex);
            }
        }
    }

    private static void applyDefinition(ArmorProtectionDefinition def) {
        Item item = BuiltInRegistries.ITEM.get(def.target);
        if (item == null) {
            NuclearRadiation.LOGGER.warn("Default armor target not found: {}", def.target);
            return;
        }
        ArmorProtectionRegistry.register(item,
                new ArmorProtectionRegistry.Protection(def.xray, def.alpha, def.beta, def.neutron));
    }

    private static double readDouble(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsDouble() : 0.0;
    }
}
