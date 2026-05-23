package igentuman.nr.datagen;

import com.google.gson.JsonObject;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.shielding.ArmorProtectionDefinition;
import igentuman.nr.shielding.ArmorProtectionReloadListener;
import igentuman.nr.shielding.DefaultArmorProtections;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ArmorProtectionProvider implements DataProvider {

    private final PackOutput output;
    private final List<ArmorProtectionDefinition> entries;

    public ArmorProtectionProvider(PackOutput output) {
        this(output, DefaultArmorProtections.defaults());
    }

    public ArmorProtectionProvider(PackOutput output, List<ArmorProtectionDefinition> entries) {
        this.output = output;
        this.entries = entries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Path base = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(NuclearRadiation.MODID)
                .resolve(ArmorProtectionReloadListener.FOLDER);

        List<CompletableFuture<?>> futures = new ArrayList<>(entries.size());
        for (ArmorProtectionDefinition def : entries) {
            JsonObject obj = new JsonObject();
            obj.addProperty("target", def.target.toString());
            obj.addProperty("xray", def.xray);
            obj.addProperty("alpha", def.alpha);
            obj.addProperty("beta", def.beta);
            obj.addProperty("neutron", def.neutron);

            Path p = base.resolve(def.fileId + ".json");
            futures.add(DataProvider.saveStable(cachedOutput, obj, p));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Nuclear Radiation Armor Protection";
    }
}
