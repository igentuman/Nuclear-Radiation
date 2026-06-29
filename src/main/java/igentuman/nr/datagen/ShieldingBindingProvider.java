package igentuman.nr.datagen;

import com.google.gson.JsonObject;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.shielding.DefaultShieldingBindings;
import igentuman.nr.shielding.ShieldingBindingDefinition;
import igentuman.nr.shielding.ShieldingBindingsReloadListener;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ShieldingBindingProvider implements DataProvider {

    private final PackOutput output;
    private final List<ShieldingBindingDefinition> entries;

    public ShieldingBindingProvider(PackOutput output) {
        this(output, DefaultShieldingBindings.defaults());
    }

    public ShieldingBindingProvider(PackOutput output, List<ShieldingBindingDefinition> entries) {
        this.output = output;
        this.entries = entries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Path base = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(NuclearRadiation.MODID)
                .resolve(ShieldingBindingsReloadListener.FOLDER);

        List<CompletableFuture<?>> futures = new ArrayList<>(entries.size());
        for (ShieldingBindingDefinition def : entries) {
            JsonObject obj = new JsonObject();
            switch (def.kind) {
                case TIER_PRESET -> {
                    obj.addProperty("tier_preset", def.tier.id());
                    obj.addProperty("xray", def.xray);
                    obj.addProperty("neutron", def.neutron);
                }
                case BLOCK_BINDING -> {
                    obj.addProperty("target", def.tag ? "#" + def.target : def.target.toString());
                    if (def.tier != null) {
                        obj.addProperty("tier", def.tier.id());
                    } else {
                        obj.addProperty("xray", def.xray);
                        obj.addProperty("neutron", def.neutron);
                    }
                }
            }

            Path p = base.resolve(def.fileId + ".json");
            futures.add(DataProvider.saveStable(cachedOutput, obj, p));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Nuclear Radiation Shielding Bindings";
    }
}
