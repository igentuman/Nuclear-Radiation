package igentuman.nr.shielding;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.NREvents;
import igentuman.nr.shielding.ShieldingBindings.ShieldEntry;
import igentuman.nr.shielding.ShieldingRegistry.Coeffs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public class ShieldingBindingsReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    public static final String FOLDER = "nuclear_radiation/shielding";

    public ShieldingBindingsReloadListener() {
        super(GSON, FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager mgr, ProfilerFiller profiler) {
        ShieldingBindings.clear();
        DefaultShieldingBindings.defaults().forEach(DefaultShieldingBindings::apply);

        for (Map.Entry<ResourceLocation, JsonElement> e : map.entrySet()) {
            try {
                JsonObject obj = e.getValue().getAsJsonObject();

                if (obj.has("tier_preset")) {
                    ShieldingTier tier = ShieldingTier.byId(obj.get("tier_preset").getAsString());
                    if (tier == null) {
                        NuclearRadiation.LOGGER.warn("Unknown shielding tier_preset '{}' in {}",
                                obj.get("tier_preset").getAsString(), e.getKey());
                        continue;
                    }
                    ShieldingBindings.setTierPreset(tier,
                            new Coeffs(readDouble(obj, "xray", tier.defaultXray),
                                    readDouble(obj, "neutron", tier.defaultNeutron)));
                    continue;
                }

                if (!obj.has("target")) {
                    NuclearRadiation.LOGGER.warn("Shielding binding {} missing 'target'", e.getKey());
                    continue;
                }
                String target = obj.get("target").getAsString();
                boolean isTag = target.startsWith("#");
                ResourceLocation id = ResourceLocation.parse(isTag ? target.substring(1) : target);

                ShieldEntry entry;
                if (obj.has("xray") || obj.has("neutron")) {
                    entry = ShieldEntry.ofRaw(new Coeffs(readDouble(obj, "xray", 0.0),
                            readDouble(obj, "neutron", 0.0)));
                } else if (obj.has("tier")) {
                    ShieldingTier tier = ShieldingTier.byId(obj.get("tier").getAsString());
                    if (tier == null) {
                        NuclearRadiation.LOGGER.warn("Unknown shielding tier '{}' in {}",
                                obj.get("tier").getAsString(), e.getKey());
                        continue;
                    }
                    entry = ShieldEntry.ofTier(tier);
                } else {
                    NuclearRadiation.LOGGER.warn("Shielding binding {} has neither tier nor raw coeffs", e.getKey());
                    continue;
                }

                if (isTag) {
                    ShieldingBindings.putBlockTag(TagKey.create(Registries.BLOCK, id), entry);
                } else {
                    ShieldingBindings.putBlock(id, entry);
                }
            } catch (Exception ex) {
                NuclearRadiation.LOGGER.error("Failed to load shielding binding {}", e.getKey(), ex);
            }
        }

        NREvents.runAfterShieldingReload();
    }

    private static double readDouble(JsonObject obj, String key, double fallback) {
        return obj.has(key) ? obj.get(key).getAsDouble() : fallback;
    }
}
