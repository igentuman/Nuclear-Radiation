package igentuman.nr.datagen;

import com.google.gson.JsonObject;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.shielding.ShieldingUpgradeDefinition;
import igentuman.nr.events.ShieldingUpgradeReloadListener;
import igentuman.nr.radiation.shielding.armor.DefaultShieldingUpgrades;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ShieldingUpgradeProvider implements DataProvider {

    private final PackOutput output;
    private final List<ShieldingUpgradeDefinition> entries;

    public ShieldingUpgradeProvider(PackOutput output) {
        this(output, DefaultShieldingUpgrades.defaults());
    }

    public ShieldingUpgradeProvider(PackOutput output, List<ShieldingUpgradeDefinition> entries) {
        this.output = output;
        this.entries = entries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Path base = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(NuclearRadiation.MODID)
                .resolve(ShieldingUpgradeReloadListener.FOLDER);

        List<CompletableFuture<?>> futures = new ArrayList<>(entries.size());
        for (ShieldingUpgradeDefinition def : entries) {
            JsonObject obj = new JsonObject();
            obj.addProperty("target", def.target.toString());
            obj.addProperty("value", def.value);

            Path p = base.resolve(def.fileId + ".json");
            futures.add(DataProvider.saveStable(cachedOutput, obj, p));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Nuclear Radiation Shielding Upgrades";
    }
}
