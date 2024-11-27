package igentuman.nr.setup.registration;

import igentuman.nr.NuclearRadiation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

import static igentuman.nr.setup.registration.NCItems.*;
import static igentuman.nr.setup.registration.Registries.CREATIVE_TABS;
import static igentuman.nr.setup.registration.Registries.ITEM_REGISTRY;

public class CreativeTabs {

    public static List<Item> getItemsByTagKey(String key)
    {
        List<Item> tmp = new ArrayList<>();
        TagKey<Item> tag = TagKey.create(ITEM_REGISTRY, new ResourceLocation(key));
        for(Item holder : ForgeRegistries.ITEMS.tags().getTag(tag).stream().toList()) {
            tmp.add(holder);
        }
        return tmp;
    }


    public static final RegistryObject<CreativeModeTab> NC_ITEMS_TAB = CREATIVE_TABS.register("nc_items",
            () ->  CreativeModeTab.builder()
                    .icon(() -> new ItemStack(getItemsByTagKey("forge:ingots/uranium").get(0)))
                    .displayItems((displayParams, output) -> getItems().forEach(output::accept))
                    .title(Component.translatable("itemGroup.nuclearradiation_items"))
                    .build()
    );

    private static List<ItemStack> itemStacks(Collection<RegistryObject<Item>> map) {
        List<ItemStack> stacks = new ArrayList<>();
        for(RegistryObject<Item> item: map) {
            stacks.add(new ItemStack(item.get()));
        }
        return stacks;
    }

    private static List<ItemStack> blockStacks(Collection<RegistryObject<Block>> map) {
        List<ItemStack> stacks = new ArrayList<>();
        for(RegistryObject<Block> item: map) {
            stacks.add(new ItemStack(item.get()));
        }
        return stacks;
    }



    private static List<ItemStack> getItems()
    {
        List<ItemStack> items = itemStacks(NC_PARTS.values());
        items.addAll(itemStacks(NC_ITEMS.values()));
        items.addAll(itemStacks(NC_SHIELDING.values()));
        items.add(new ItemStack(HAZMAT_MASK.get()));
        items.add(new ItemStack(HAZMAT_CHEST.get()));
        items.add(new ItemStack(HAZMAT_PANTS.get()));
        items.add(new ItemStack(HAZMAT_BOOTS.get()));
        items.add(new ItemStack(GEIGER_COUNTER.get()));
        return items;
    }

    public static final RegistryObject<CreativeModeTab> NC_PARTS_TAB = CREATIVE_TABS.register("nc_parts",
            () ->  CreativeModeTab.builder()
            .icon(() -> new ItemStack(NC_PARTS.get("actuator").get()))
            .displayItems((displayParams, output) -> NC_PARTS.values().forEach(itemlike -> output.accept(new ItemStack(itemlike.get()))))
            .title(Component.translatable("itemGroup.nuclearradiation_items"))
            .build());

    public static void init() {

    }
}
