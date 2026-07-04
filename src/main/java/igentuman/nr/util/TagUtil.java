package igentuman.nr.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class TagUtil {
    public static TagKey<Item> plateTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "plates/" + name));
    }

    public static TagKey<Item> dustTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dusts/" + name));
    }

    public static TagKey<Item> rawTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/" + name));
    }

    public static TagKey<Item> ingotTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/" + name));
    }

    public static TagKey<Item> blockTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + name));
    }

    public static TagKey<Item> gemTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/" + name));
    }

    public static TagKey<Item> nuggetTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "nuggets/" + name));
    }

    public static TagKey<Item> oreTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/" + name));
    }

    public static TagKey<Item> isotopeTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "isotopes/" + name));
    }

    public static TagKey<Item> pelletTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "pellets/" + name));
    }

    public static TagKey<Item> cTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
    }
}
