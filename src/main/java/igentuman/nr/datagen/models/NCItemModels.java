package igentuman.nr.datagen.models;

import igentuman.nr.setup.registration.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static igentuman.nr.NuclearRadiation.MODID;
import static igentuman.nr.setup.registration.NCItems.*;

public class NCItemModels extends ItemModelProvider {

    public NCItemModels(DataGenerator generator, GatherDataEvent event) {
        super(generator.getPackOutput(), MODID, event.getExistingFileHelper());
    }

    @Override
    protected void registerModels() {

        parts();
        armor();
        items();
        shielding();
    }


    private String name(ItemLike item)
    {
        return ForgeRegistries.ITEMS.getKey(item.asItem()).getPath();
    }

    private ResourceLocation forgeLoc(String s)
    {
        return new ResourceLocation("forge", s);
    }


    private void shielding() {
        for(String name: NCItems.NC_SHIELDING.keySet()) {
            singleTexture(NCItems.NC_SHIELDING.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/rad_shielding/"+name));
        }
    }

    private void items() {
        for(String name: NCItems.NC_ITEMS.keySet()) {
            if(name.contains("collector")) continue;
            singleTexture(NCItems.NC_ITEMS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/"+name));
        }
    }

    private void armor() {

        singleTexture(HAZMAT_BOOTS.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+HAZMAT_BOOTS.getId().getPath()));
        singleTexture(HAZMAT_CHEST.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+HAZMAT_CHEST.getId().getPath()));
        singleTexture(HAZMAT_MASK.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+HAZMAT_MASK.getId().getPath()));
        singleTexture(HAZMAT_PANTS.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/armor/"+HAZMAT_PANTS.getId().getPath()));

    }

    private void parts() {
        for(String name: NCItems.NC_PARTS.keySet()) {
            if(name.matches("chassis|empty_frame|steel_frame")) continue;
            singleTexture(NCItems.NC_PARTS.get(name).getId().getPath(),
                    mcLoc("item/generated"),
                    "layer0", modLoc("item/part/"+name));
        }
    }

}