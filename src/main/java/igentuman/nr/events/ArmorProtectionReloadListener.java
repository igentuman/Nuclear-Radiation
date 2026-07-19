package igentuman.nr.events;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.shielding.ArmorProtectionDefinition;
import igentuman.nr.api.shielding.ArmorProtectionRegistry;
import igentuman.nr.radiation.shielding.armor.DefaultArmorProtections;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
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
        DefaultArmorProtections.tagDefaults().forEach(ArmorProtectionRegistry::registerTag);

        for (Map.Entry<ResourceLocation, JsonElement> e : map.entrySet()) {
            try {
                JsonObject obj = e.getValue().getAsJsonObject();
                String targetStr = obj.get("target").getAsString();
                double xray = readDouble(obj, "xray");
                double alpha = readDouble(obj, "alpha");
                double beta = readDouble(obj, "beta");
                double neutron = readDouble(obj, "neutron");
                boolean gas = obj.has("gas_protection") && obj.get("gas_protection").getAsBoolean();
                ArmorProtectionRegistry.Protection protection =
                        new ArmorProtectionRegistry.Protection(xray, alpha, beta, neutron, gas);

                if (targetStr.startsWith("#")) {
                    TagKey<Item> tag = TagKey.create(Registries.ITEM,
                            ResourceLocation.parse(targetStr.substring(1)));
                    ArmorProtectionRegistry.registerTag(tag, protection);
                } else {
                    ResourceLocation target = ResourceLocation.parse(targetStr);
                    Item item = BuiltInRegistries.ITEM.get(target);
                    if (item == null) {
                        NuclearRadiation.LOGGER.warn("Armor protection target not found: {}", target);
                        continue;
                    }
                    ArmorProtectionRegistry.register(item, protection);
                }
            } catch (Exception ex) {
                NuclearRadiation.LOGGER.error("Failed to load armor protection {}", e.getKey(), ex);
            }
        }

        NREvents.runAfterArmorReload();
    }

    private static void applyDefinition(ArmorProtectionDefinition def) {
        Item item = BuiltInRegistries.ITEM.get(def.target);
        if (item == null) {
            NuclearRadiation.LOGGER.warn("Default armor target not found: {}", def.target);
            return;
        }
        ArmorProtectionRegistry.register(item,
                new ArmorProtectionRegistry.Protection(def.xray, def.alpha, def.beta, def.neutron, def.protectsFromGas));
    }

    private static double readDouble(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsDouble() : 0.0;
    }
}
