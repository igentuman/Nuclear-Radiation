package igentuman.nr.binding;

import igentuman.nr.NuclearRadiation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class RadiationTags {
    private RadiationTags() {}

    public static final TagKey<Item> ITEM_LOW = itemTag("radioactive/low");
    public static final TagKey<Item> ITEM_MEDIUM = itemTag("radioactive/medium");
    public static final TagKey<Item> ITEM_HIGH = itemTag("radioactive/high");
    public static final TagKey<Item> ITEM_URANIUM_ORE = itemTag("radioactive/uranium_ore");
    public static final TagKey<Item> ITEM_URANIUM_RAW = itemTag("radioactive/uranium_raw");
    public static final TagKey<Item> ITEM_URANIUM_INGOT = itemTag("radioactive/uranium_ingot");
    public static final TagKey<Item> ITEM_URANIUM_DUST = itemTag("radioactive/uranium_dust");
    public static final TagKey<Item> ITEM_SPENT_FUEL = itemTag("radioactive/spent_fuel");

    public static final TagKey<Block> BLOCK_LOW = blockTag("radioactive/low");
    public static final TagKey<Block> BLOCK_MEDIUM = blockTag("radioactive/medium");
    public static final TagKey<Block> BLOCK_HIGH = blockTag("radioactive/high");
    public static final TagKey<Block> BLOCK_URANIUM_ORE = blockTag("radioactive/uranium_ore");

    public static final TagKey<Fluid> FLUID_RADIOACTIVE = fluidTag("radioactive");

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, path));
    }

    private static TagKey<Block> blockTag(String path) {
        return TagKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, path));
    }

    private static TagKey<Fluid> fluidTag(String path) {
        return TagKey.create(Registries.FLUID,
                ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, path));
    }
}
