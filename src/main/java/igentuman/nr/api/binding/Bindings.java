package igentuman.nr.api.binding;

import igentuman.nr.api.RadiationProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class Bindings {
    private static final Object LOCK = new Object();

    private static final Map<ResourceLocation, Supplier<RadiationProfile>> ITEMS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Supplier<RadiationProfile>> BLOCKS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Supplier<RadiationProfile>> FLUIDS = new LinkedHashMap<>();

    private static final Map<TagKey<Item>, Supplier<RadiationProfile>> ITEM_TAGS = new LinkedHashMap<>();
    private static final Map<TagKey<Block>, Supplier<RadiationProfile>> BLOCK_TAGS = new LinkedHashMap<>();
    private static final Map<TagKey<Fluid>, Supplier<RadiationProfile>> FLUID_TAGS = new LinkedHashMap<>();

    private Bindings() {}

    public static void putItem(ResourceLocation id, Supplier<RadiationProfile> profile) {
        synchronized (LOCK) { ITEMS.put(id, profile); }
    }
    public static void putBlock(ResourceLocation id, Supplier<RadiationProfile> profile) {
        synchronized (LOCK) { BLOCKS.put(id, profile); }
    }
    public static void putFluid(ResourceLocation id, Supplier<RadiationProfile> profile) {
        synchronized (LOCK) { FLUIDS.put(id, profile); }
    }
    public static void putItemTag(TagKey<Item> tag, Supplier<RadiationProfile> profile) {
        synchronized (LOCK) { ITEM_TAGS.put(tag, profile); }
    }
    public static void putBlockTag(TagKey<Block> tag, Supplier<RadiationProfile> profile) {
        synchronized (LOCK) { BLOCK_TAGS.put(tag, profile); }
    }
    public static void putFluidTag(TagKey<Fluid> tag, Supplier<RadiationProfile> profile) {
        synchronized (LOCK) { FLUID_TAGS.put(tag, profile); }
    }

    public static void removeItem(ResourceLocation id) { synchronized (LOCK) { ITEMS.remove(id); } }
    public static void removeBlock(ResourceLocation id) { synchronized (LOCK) { BLOCKS.remove(id); } }
    public static void removeFluid(ResourceLocation id) { synchronized (LOCK) { FLUIDS.remove(id); } }
    public static void removeItemTag(TagKey<Item> tag) { synchronized (LOCK) { ITEM_TAGS.remove(tag); } }
    public static void removeBlockTag(TagKey<Block> tag) { synchronized (LOCK) { BLOCK_TAGS.remove(tag); } }
    public static void removeFluidTag(TagKey<Fluid> tag) { synchronized (LOCK) { FLUID_TAGS.remove(tag); } }

    public static Supplier<RadiationProfile> getItem(ResourceLocation id) { synchronized (LOCK) { return ITEMS.get(id); } }
    public static Supplier<RadiationProfile> getBlock(ResourceLocation id) { synchronized (LOCK) { return BLOCKS.get(id); } }
    public static Supplier<RadiationProfile> getFluid(ResourceLocation id) { synchronized (LOCK) { return FLUIDS.get(id); } }

    public static Map<TagKey<Item>, Supplier<RadiationProfile>> itemTags() { synchronized (LOCK) { return new LinkedHashMap<>(ITEM_TAGS); } }
    public static Map<TagKey<Block>, Supplier<RadiationProfile>> blockTags() { synchronized (LOCK) { return new LinkedHashMap<>(BLOCK_TAGS); } }
    public static Map<TagKey<Fluid>, Supplier<RadiationProfile>> fluidTags() { synchronized (LOCK) { return new LinkedHashMap<>(FLUID_TAGS); } }

    public static Map<ResourceLocation, Supplier<RadiationProfile>> items() { synchronized (LOCK) { return new LinkedHashMap<>(ITEMS); } }
    public static Map<ResourceLocation, Supplier<RadiationProfile>> blocks() { synchronized (LOCK) { return new LinkedHashMap<>(BLOCKS); } }
    public static Map<ResourceLocation, Supplier<RadiationProfile>> fluids() { synchronized (LOCK) { return new LinkedHashMap<>(FLUIDS); } }

    public static void clear() {
        synchronized (LOCK) {
            ITEMS.clear();
            BLOCKS.clear();
            FLUIDS.clear();
            ITEM_TAGS.clear();
            BLOCK_TAGS.clear();
            FLUID_TAGS.clear();
        }
    }
}
