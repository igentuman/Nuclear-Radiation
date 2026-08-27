package igentuman.nr.datagen;

import igentuman.nr.NuclearRadiation;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = NuclearRadiation.MODID)
public final class NRDataGenerators {
    private NRDataGenerators() {}

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();
        ExistingFileHelper existing = event.getExistingFileHelper();

        gen.addProvider(event.includeServer(), new IsotopeProvider(output));
        gen.addProvider(event.includeServer(), new RadiationBindingProvider(output));
        gen.addProvider(event.includeServer(), new ShieldingBindingProvider(output));
        gen.addProvider(event.includeServer(), new ArmorProtectionProvider(output));
        gen.addProvider(event.includeServer(), new ShieldingUpgradeProvider(output));
        gen.addProvider(event.includeServer(), new NRRecipeProvider(output, lookup));

        NRBlockTagsProvider blockTags = new NRBlockTagsProvider(output, lookup, existing);
        gen.addProvider(event.includeServer(), blockTags);
        gen.addProvider(event.includeServer(),
                new NRItemTagsProvider(output, lookup, blockTags.contentsGetter(), existing));
    }
}
