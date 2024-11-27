package igentuman.nr.datagen.tags;

import igentuman.nr.setup.registration.NCFluids;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraftforge.common.Tags.Fluids;
import net.minecraftforge.data.event.GatherDataEvent;

import static igentuman.nr.NuclearRadiation.MODID;
import static igentuman.nr.setup.registration.NCFluids.NC_GASES;
import static igentuman.nr.setup.registration.NCFluids.NC_MATERIALS;
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
		for(String name: NC_MATERIALS.keySet()) {
			tag(LIQUIDS_TAG.get(name)).add(NC_MATERIALS.get(name).getStill());
			tag(LIQUIDS_TAG.get(name)).add(NC_MATERIALS.get(name).getFlowing());
		}
		for(String name: NC_GASES.keySet()) {
			tag(LIQUIDS_TAG.get(name)).add(NC_GASES.get(name).getStill());
			tag(LIQUIDS_TAG.get(name)).add(NC_GASES.get(name).getFlowing());

			tag(Fluids.GASEOUS).add(NC_GASES.get(name).getStill());
			tag(Fluids.GASEOUS).add(NC_GASES.get(name).getFlowing());
		}
	}
}
