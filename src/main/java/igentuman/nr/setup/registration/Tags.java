package igentuman.nr.setup.registration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import java.util.HashMap;
import static igentuman.nr.NuclearRadiation.MODID;
import static igentuman.nr.setup.registration.Registries.BLOCK_REGISTRY;
import static igentuman.nr.setup.registration.Registries.ITEM_REGISTRY;

public class Tags {

    public static HashMap<String, TagKey<Item>> INGOTS_TAG = new HashMap<>();
    public static HashMap<String, TagKey<Item>> CHUNKS_TAG = new HashMap<>();
    public static HashMap<String, TagKey<Item>> GEMS_TAG = new HashMap<>();
    public static HashMap<String, TagKey<Item>> NUGGETS_TAG = new HashMap<>();
    public static HashMap<String, TagKey<Item>> PLATES_TAG = new HashMap<>();
    public static HashMap<String, TagKey<Item>> DUSTS_TAG = new HashMap<>();
    public static HashMap<String, TagKey<Item>> ORE_ITEM_TAGS = new HashMap<>();
    public static HashMap<String, TagKey<Item>> BLOCK_ITEM_TAGS = new HashMap<>();
    public static HashMap<String, TagKey<Block>> ORE_TAGS = new HashMap<>();
    public static HashMap<String, TagKey<Block>> BLOCK_TAGS = new HashMap<>();
    public static HashMap<String, TagKey<Fluid>> LIQUIDS_TAG = new HashMap<>();

    public static TagKey<Block> blockTag(String name) {
        return TagKey.create(BLOCK_REGISTRY, new ResourceLocation(MODID, name));
    }

    public static TagKey<Item> itemTag(String name) {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation(MODID, name));
    }


    public static TagKey<Item> forgeIngot(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:ingots/"+name));
    }

    public static TagKey<Item> forgeGem(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:gems/"+name));
    }

    public static TagKey<Item> forgeNugget(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:nuggets/"+name));
    }

    public static TagKey<Item> forgeBlock(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:storage_blocks/"+name));
    }

    public static TagKey<Item> forgeOre(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:ores/"+name));
    }

    public static TagKey<Item> forgeBucket(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:buckets/"+name));
    }

    public static TagKey<Item> forgeChunk(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:raw_materials/"+name));
    }

    public static TagKey<Item> forgeDust(String name)
    {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:dusts/"+name));
    }

    public static TagKey<Item> forgePlate(String name) {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:plates/"+name));
    }

    public static TagKey<Item> forgeDye(String name) {
        return TagKey.create(ITEM_REGISTRY, new ResourceLocation("forge:dye/"+name));
    }
}
