package igentuman.nr.datagen.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraftforge.common.Tags.Fluids;
import net.minecraftforge.data.event.GatherDataEvent;

import static igentuman.nr.NuclearRadiation.MODID;
import static igentuman.nr.setup.registration.Tags.LIQUIDS_TAG;

public class FluidTags extends FluidTagsProvider
{
	public FluidTags(DataGenerator gen, GatherDataEvent event)
	{
		super(gen.getPackOutput(), event.getLookupProvider(), MODID, event.getExistingFileHelper());
	}

	@Override
	protected void addTags(HolderLookup.Provider provider)
	{
			//tag(Fluids.GASEOUS).add(NC_GASES.get(name).getStill());
			//tag(Fluids.GASEOUS).add(NC_GASES.get(name).getFlowing());

	}
}
