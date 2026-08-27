package igentuman.nr.events;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.shielding.ShieldingUpgradeDefinition;
import igentuman.nr.api.shielding.ShieldingUpgradeRegistry;
import igentuman.nr.radiation.shielding.armor.DefaultShieldingUpgrades;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

import java.util.Map;

public class ShieldingUpgradeReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    public static final String FOLDER = "nuclear_radiation/shielding_upgrade";

    public ShieldingUpgradeReloadListener() {
        super(GSON, FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager mgr, ProfilerFiller profiler) {
        ShieldingUpgradeRegistry.clear();
        DefaultShieldingUpgrades.defaults().forEach(ShieldingUpgradeReloadListener::applyDefinition);

        for (Map.Entry<ResourceLocation, JsonElement> e : map.entrySet()) {
            try {
                JsonObject obj = e.getValue().getAsJsonObject();
                String targetStr = obj.get("target").getAsString();
                double value = obj.get("value").getAsDouble();

                ResourceLocation target = ResourceLocation.parse(targetStr);
                Item item = BuiltInRegistries.ITEM.get(target);
                if (item == null) {
                    NuclearRadiation.LOGGER.warn("Shielding upgrade target not found: {}", target);
                    continue;
                }
                ShieldingUpgradeRegistry.register(item, value);
            } catch (Exception ex) {
                NuclearRadiation.LOGGER.error("Failed to load shielding upgrade {}", e.getKey(), ex);
            }
        }
    }

    private static void applyDefinition(ShieldingUpgradeDefinition def) {
        Item item = BuiltInRegistries.ITEM.get(def.target);
        if (item == null) {
            NuclearRadiation.LOGGER.warn("Default shielding upgrade target not found: {}", def.target);
            return;
        }
        ShieldingUpgradeRegistry.register(item, def.value);
    }
}
