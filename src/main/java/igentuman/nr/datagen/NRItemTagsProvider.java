package igentuman.nr.datagen;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.binding.RadiationTags;
import igentuman.nr.corium.Corium;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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

        tag(RadiationTags.ITEM_SPENT_FUEL)
                .addOptionalTag(c("spent_nuclear_fuel"));

        tag(RadiationTags.ITEM_AIRBORNE_CONTAMINANT)
                .add(NuclearRadiation.FALLOUT_DUST.get())
                .add(NuclearRadiation.FALLOUT_DUST_BLOCK_ITEM.get())
                .addOptionalTag(c("dusts/uranium"))
                .addOptionalTag(c("dusts/plutonium"));

        tag(cItem("storage_blocks")).add(Corium.CORIUM_BLOCK_ITEM.get());
        tag(cItem("storage_blocks/corium")).add(Corium.CORIUM_BLOCK_ITEM.get());
    }

    private static ResourceLocation c(String path) {
        return ResourceLocation.fromNamespaceAndPath("c", path);
    }

    private static TagKey<Item> cItem(String path) {
        return TagKey.create(Registries.ITEM, c(path));
    }
}
