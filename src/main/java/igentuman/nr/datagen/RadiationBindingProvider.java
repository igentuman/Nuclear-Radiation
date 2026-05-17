package igentuman.nr.datagen;

import com.google.gson.JsonObject;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.binding.BindingDefinition;
import igentuman.nr.binding.DefaultBindings;
import igentuman.nr.binding.RadiationBindingsReloadListener;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RadiationBindingProvider implements DataProvider {

    private final PackOutput output;
    private final List<BindingDefinition> bindings;

    public RadiationBindingProvider(PackOutput output) {
        this(output, DefaultBindings.defaults());
    }

    public RadiationBindingProvider(PackOutput output, List<BindingDefinition> bindings) {
        this.output = output;
        this.bindings = bindings;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Path base = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(NuclearRadiation.MODID)
                .resolve(RadiationBindingsReloadListener.FOLDER);

        List<CompletableFuture<?>> futures = new ArrayList<>(bindings.size());
        for (BindingDefinition def : bindings) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", def.type);
            obj.addProperty("target", def.tag ? "#" + def.target : def.target.toString());

            JsonObject isotopes = new JsonObject();
            for (Map.Entry<String, Double> e : def.isotopes.entrySet()) {
                JsonObject v = new JsonObject();
                v.addProperty("atoms", e.getValue());
                isotopes.add(e.getKey(), v);
            }
            obj.add("isotopes", isotopes);

            Path p = base.resolve(def.fileId + ".json");
            futures.add(DataProvider.saveStable(cachedOutput, obj, p));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Nuclear Radiation Bindings";
    }
}
