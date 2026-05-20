package igentuman.nr.datagen;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.binding.RadiationTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class NRBlockTagsProvider extends BlockTagsProvider {

    public NRBlockTagsProvider(PackOutput output,
                               CompletableFuture<HolderLookup.Provider> lookup,
                               ExistingFileHelper existingFileHelper) {
        super(output, lookup, NuclearRadiation.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(RadiationTags.BLOCK_URANIUM_ORE)
                .addOptionalTag(c("ores/uranium"));

        tag(RadiationTags.BLOCK_HIGH)
                .addOptionalTag(c("storage_blocks/plutonium"));

        tag(RadiationTags.BLOCK_MEDIUM)
                .addOptionalTag(c("storage_blocks/thorium"));
    }

    private static ResourceLocation c(String path) {
        return ResourceLocation.fromNamespaceAndPath("c", path);
    }
}
