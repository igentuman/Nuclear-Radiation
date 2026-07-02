package igentuman.nr.datagen;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.binding.RadiationTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
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

        tag(RadiationTags.BLOCK_HIGH)
                .addOptionalTag(c("storage_blocks/plutonium"));

        tag(RadiationTags.BLOCK_MEDIUM)
                .addOptionalTag(c("storage_blocks/thorium"));

        tag(RadiationTags.BLOCK_SHIELD_LIGHT)
                .add(Blocks.DIRT, Blocks.SAND, Blocks.CLAY, Blocks.SOUL_SAND,
                        Blocks.WATER, Blocks.ICE, Blocks.PACKED_ICE);

        tag(RadiationTags.BLOCK_SHIELD_MID)
                .add(Blocks.STONE, Blocks.COBBLESTONE, Blocks.DEEPSLATE,
                        Blocks.LAPIS_BLOCK, Blocks.OBSIDIAN);

        tag(RadiationTags.BLOCK_SHIELD_HEAVY)
                .add(Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK, Blocks.NETHERITE_BLOCK);
    }

    private static ResourceLocation c(String path) {
        return ResourceLocation.fromNamespaceAndPath("c", path);
    }
}
