package igentuman.nr.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import igentuman.nr.api.DecayEdge;
import igentuman.nr.api.IChunkRadiation;
import igentuman.nr.registry.DefaultIsotopes;
import igentuman.nr.events.IsotopesReloadListener;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static igentuman.nr.NuclearRadiation.MODID;

public class IsotopeProvider implements DataProvider {

    private final PackOutput output;
    private final List<IChunkRadiation.IsotopeDefinition> isotopes;

    public IsotopeProvider(PackOutput output) {
        this(output, DefaultIsotopes.defaults());
    }

    public IsotopeProvider(PackOutput output, List<IChunkRadiation.IsotopeDefinition> isotopes) {
        this.output = output;
        this.isotopes = isotopes;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Path root = output.getOutputFolder(PackOutput.Target.DATA_PACK);

        List<CompletableFuture<?>> futures = new ArrayList<>(isotopes.size());
        for (IChunkRadiation.IsotopeDefinition def : isotopes) {
            JsonObject obj = new JsonObject();
            obj.addProperty("alpha", def.alpha);
            obj.addProperty("beta", def.beta);
            obj.addProperty("xray", def.xray);
            obj.addProperty("neutron", def.neutron);
            obj.addProperty("half_life_ticks", def.halfLifeTicks);

            JsonObject quality = new JsonObject();
            quality.addProperty("xray", def.qXray);
            quality.addProperty("beta", def.qBeta);
            quality.addProperty("alpha", def.qAlpha);
            quality.addProperty("neutron", def.qNeutron);
            obj.add("quality", quality);

            if (!def.branches.isEmpty()) {
                JsonArray branches = new JsonArray();
                for (DecayEdge e : def.branches) {
                    JsonObject br = new JsonObject();
                    br.addProperty("target", e.targetIsotopeId());
                    br.addProperty("probability", e.probability());
                    branches.add(br);
                }
                obj.add("branches", branches);
            }

            Path p = root.resolve(MODID)
                    .resolve(IsotopesReloadListener.FOLDER)
                    .resolve(def.path() + ".json");
            futures.add(DataProvider.saveStable(cachedOutput, obj, p));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Nuclear Radiation Isotopes";
    }
}
