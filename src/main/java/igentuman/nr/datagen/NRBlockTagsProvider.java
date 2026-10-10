package igentuman.nr.datagen;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.radiation.source.Corium;
import igentuman.nr.registry.RadiationTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class NRBlockTagsProvider extends BlockTagsProvider {

    public NRBlockTagsProvider(PackOutput output,
                               CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, NuclearRadiation.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        tag(RadiationTags.BLOCK_HIGH)
                .addOptionalTag(cBlock("storage_blocks/plutonium"));

        tag(RadiationTags.BLOCK_MEDIUM)
                .addOptionalTag(cBlock("storage_blocks/thorium"));

        tag(RadiationTags.BLOCK_SHIELD_LIGHT)
                .add(Blocks.DIRT, Blocks.SAND, Blocks.CLAY, Blocks.SOUL_SAND,
                        Blocks.WATER, Blocks.ICE, Blocks.PACKED_ICE);

        tag(RadiationTags.BLOCK_SHIELD_MID)
                .add(Blocks.STONE, Blocks.COBBLESTONE, Blocks.DEEPSLATE,
                        Blocks.LAPIS_BLOCK, Blocks.OBSIDIAN);

        tag(RadiationTags.BLOCK_SHIELD_HEAVY)
                .add(Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK, Blocks.NETHERITE_BLOCK);

        tag(RadiationTags.BLOCK_SHIELD_EXTRA_HEAVY)
                .addOptionalTag(cBlock("storage_blocks/lead"));

        tag(RadiationTags.BLOCK_SHIELD_EXTRA_HEAVY)
                .add(Blocks.GOLD_BLOCK, Blocks.NETHERITE_BLOCK);

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(Corium.CORIUM_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(Corium.CORIUM_BLOCK.get());
        tag(cBlock("storage_blocks")).add(Corium.CORIUM_BLOCK.get());
        tag(cBlock("storage_blocks/corium")).add(Corium.CORIUM_BLOCK.get());
    }

    private static Identifier c(String path) {
        return Identifier.fromNamespaceAndPath("c", path);
    }

    private static TagKey<Block> cBlock(String path) {
        return TagKey.create(Registries.BLOCK, c(path));
    }
}
