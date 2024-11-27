package igentuman.nr.datagen.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import static igentuman.nr.NuclearRadiation.MODID;

public class NCBlockTags extends BlockTagsProvider {

    public NCBlockTags(DataGenerator generator, GatherDataEvent event) {
        super(generator.getPackOutput(), event.getLookupProvider(), MODID, event.getExistingFileHelper());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
    }


    @Override
    public @NotNull String getName() {
        return "NuclearRadiation Block Tags";
    }

}
