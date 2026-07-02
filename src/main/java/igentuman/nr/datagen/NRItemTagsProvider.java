package igentuman.nr.datagen;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.binding.RadiationTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class NRItemTagsProvider extends ItemTagsProvider {

    public NRItemTagsProvider(PackOutput output,
                              CompletableFuture<HolderLookup.Provider> lookup,
                              CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
                              ExistingFileHelper existingFileHelper) {
        super(output, lookup, blockTags, NuclearRadiation.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        tag(RadiationTags.ITEM_HIGH)
                .addOptionalTag(c("dusts/plutonium"))
                .addOptionalTag(c("ingots/plutonium"));

        tag(RadiationTags.ITEM_MEDIUM)
                .addOptionalTag(c("dusts/thorium"))
                .addOptionalTag(c("ingots/thorium"));

        tag(RadiationTags.ITEM_SPENT_FUEL)
                .addOptionalTag(c("spent_nuclear_fuel"));
    }

    private static ResourceLocation c(String path) {
        return ResourceLocation.fromNamespaceAndPath("c", path);
    }
}
