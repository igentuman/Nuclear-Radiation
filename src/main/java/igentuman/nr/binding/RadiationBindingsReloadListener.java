package igentuman.nr.binding;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.NREvents;
import igentuman.nr.builder.RadiationBindingBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.Map;

public class RadiationBindingsReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    public static final String FOLDER = "nuclear_radiation/bindings";

    public RadiationBindingsReloadListener() {
        super(GSON, FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager mgr, ProfilerFiller profiler) {
        for (Map.Entry<ResourceLocation, JsonElement> e : map.entrySet()) {
            try {
                JsonObject obj = e.getValue().getAsJsonObject();
                String type = obj.get("type").getAsString();
                String target = obj.get("target").getAsString();
                JsonObject isotopes = obj.getAsJsonObject("isotopes");
                if (isotopes == null) continue;

                RadiationBindingBuilder b = RadiationBindingBuilder.create();
                boolean isTag = target.startsWith("#");
                String idStr = isTag ? target.substring(1) : target;
                ResourceLocation id = ResourceLocation.parse(idStr);

                switch (type) {
                    case "item" -> {
                        if (isTag) b.itemTag(TagKey.create(Registries.ITEM, id));
                        else b.item(id);
                    }
                    case "block" -> {
                        if (isTag) b.blockTag(TagKey.create(Registries.BLOCK, id));
                        else b.block(id);
                    }
                    case "fluid" -> {
                        if (isTag) b.fluidTag(TagKey.create(Registries.FLUID, id));
                        else b.fluid(id);
                    }
                    default -> {
                        NuclearRadiation.LOGGER.warn("Unknown binding type '{}' in {}", type, e.getKey());
                        continue;
                    }
                }

                for (Map.Entry<String, JsonElement> iso : isotopes.entrySet()) {
                    JsonObject v = iso.getValue().getAsJsonObject();
                    double atoms = v.has("atoms") ? v.get("atoms").getAsDouble() : 0.0;
                    b.isotope(iso.getKey(), atoms);
                }
                b.register();
            } catch (Exception ex) {
                NuclearRadiation.LOGGER.error("Failed to load radiation binding {}", e.getKey(), ex);
            }
        }

        NREvents.runAfterBindingsReload();
    }

    @SuppressWarnings("unused")
    private static <T> TagKey<T> ignoreType(ResourceLocation id) {
        return null;
    }

    @SuppressWarnings("unused")
    private static Class<?> typeClass(String type) {
        return switch (type) {
            case "item" -> Item.class;
            case "block" -> Block.class;
            case "fluid" -> Fluid.class;
            default -> Object.class;
        };
    }
}
