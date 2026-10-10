package igentuman.nr.datagen;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.radiation.source.Corium;
import igentuman.nr.registry.NRShieldingItems;
import igentuman.nr.registry.RadiationTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class NRItemTagsProvider extends ItemTagsProvider {

    public NRItemTagsProvider(PackOutput output,
                              CompletableFuture<HolderLookup.Provider> lookup,
                              CompletableFuture<TagsProvider.TagLookup<Block>> blockTags) {
        super(output, lookup, NuclearRadiation.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        tag(RadiationTags.ITEM_SPENT_FUEL)
                .addOptionalTag(cItem("spent_nuclear_fuel"));

        tag(RadiationTags.ITEM_AIRBORNE_CONTAMINANT)
                .add(NuclearRadiation.FALLOUT_DUST.get())
                .add(NuclearRadiation.FALLOUT_DUST_BLOCK_ITEM.get())
                .addOptionalTag(cItem("dusts/uranium"))
                .addOptionalTag(cItem("dusts/plutonium"));

        tag(cItem("storage_blocks")).add(Corium.CORIUM_BLOCK_ITEM.get());
        tag(cItem("storage_blocks/corium")).add(Corium.CORIUM_BLOCK_ITEM.get());

        tag(RadiationTags.SHIELDING_UPGRADE)
                .add(NRShieldingItems.RAD_SHIELDING_LIGHT.get())
                .add(NRShieldingItems.RAD_SHIELDING_MEDIUM.get())
                .add(NRShieldingItems.RAD_SHIELDING_HEAVY.get())
                .add(NRShieldingItems.RAD_SHIELDING_DPS.get());
    }

    private static Identifier c(String path) {
        return Identifier.fromNamespaceAndPath("c", path);
    }

    private static TagKey<Item> cItem(String path) {
        return TagKey.create(Registries.ITEM, c(path));
    }
}
