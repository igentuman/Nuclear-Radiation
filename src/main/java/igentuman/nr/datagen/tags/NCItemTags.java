package igentuman.nr.datagen.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;

import static igentuman.nr.NuclearRadiation.MODID;

public class NCItemTags extends ItemTagsProvider {

    public NCItemTags(DataGenerator generator, BlockTagsProvider blockTags, GatherDataEvent event) {
        super(generator.getPackOutput(), event.getLookupProvider(), blockTags.contentsGetter(),  MODID, event.getExistingFileHelper());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

    }


    @Override
    public String getName() {
        return "NuclearRadiation Item Tags";
    }
}