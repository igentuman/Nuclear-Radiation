package igentuman.nr.datagen.tags;

import net.minecraft.core.HolderLookup;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import static igentuman.nr.NuclearRadiation.MODID;

public class NCBiomeTags extends TagsProvider<Biome> {

    public NCBiomeTags(DataGenerator generator, GatherDataEvent event) {
        super(generator.getPackOutput(), ForgeRegistries.BIOMES.getRegistryKey(), event.getLookupProvider(), MODID, event.getExistingFileHelper());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        ForgeRegistries.BIOMES.getValues().forEach(biome -> {

        });
    }

    @Override
    public String getName() {
        return "NuclearRadiation Tags";
    }
}