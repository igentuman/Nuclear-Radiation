package igentuman.nr.events;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.DecayGraph;
import igentuman.nr.api.IChunkRadiation;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.registry.DefaultIsotopes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;

public class IsotopesReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    public static final String FOLDER = "nuclear_radiation/isotopes";

    public IsotopesReloadListener() {
        super(GSON, FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager mgr, ProfilerFiller profiler) {
        Map<String, IChunkRadiation.IsotopeDefinition> merged = new LinkedHashMap<>();
        for (IChunkRadiation.IsotopeDefinition def : DefaultIsotopes.defaults()) {
            merged.put(def.id, def);
        }

        for (Map.Entry<ResourceLocation, JsonElement> e : map.entrySet()) {
            String id = e.getKey().toString();
            try {
                merged.put(id, parse(id, e.getValue().getAsJsonObject()));
            } catch (Exception ex) {
                NuclearRadiation.LOGGER.error("Failed to load isotope {}", e.getKey(), ex);
            }
        }

        IsotopeRegistry.clear();
        DecayGraph.clear();
        for (IChunkRadiation.IsotopeDefinition def : merged.values()) {
            DefaultIsotopes.apply(def);
        }

        NREvents.runAfterIsotopesReload();
    }

    private static IChunkRadiation.IsotopeDefinition parse(String id, JsonObject obj) {
        IChunkRadiation.IsotopeDefinition.Builder b = IChunkRadiation.IsotopeDefinition.create(id)
                .alpha(readFloat(obj, "alpha", 0.0f))
                .beta(readFloat(obj, "beta", 0.0f))
                .xray(readFloat(obj, "xray", 0.0f))
                .neutron(readFloat(obj, "neutron", 0.0f))
                .halfLife(obj.has("half_life_ticks") ? obj.get("half_life_ticks").getAsLong() : 1L);

        if (obj.has("quality")) {
            JsonObject q = obj.getAsJsonObject("quality");
            b.quality(readFloat(q, "xray", 1.0f),
                    readFloat(q, "beta", 1.0f),
                    readFloat(q, "alpha", 20.0f),
                    readFloat(q, "neutron", 10.0f));
        }

        if (obj.has("branches")) {
            JsonArray branches = obj.getAsJsonArray("branches");
            for (JsonElement el : branches) {
                JsonObject br = el.getAsJsonObject();
                b.branch(br.get("target").getAsString(), br.get("probability").getAsDouble());
            }
        }

        return b.build();
    }

    private static float readFloat(JsonObject obj, String key, float fallback) {
        return obj.has(key) ? obj.get(key).getAsFloat() : fallback;
    }
}
