package igentuman.nr.datagen;

import igentuman.nr.NuclearRadiation;
import net.minecraft.core.HolderLookup;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;


@EventBusSubscriber(modid = NuclearRadiation.MODID)
public final class NRDataGenerators {
    private NRDataGenerators() {}

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Server event) {
        event.createProvider(output -> new IsotopeProvider(output));
        event.createProvider(output -> new RadiationBindingProvider(output));
        event.createProvider(output -> new ShieldingBindingProvider(output));
        event.createProvider(output -> new ArmorProtectionProvider(output));
        event.createProvider(output -> new ShieldingUpgradeProvider(output));
        event.createProvider(NRRecipeProvider.Runner::new);
        event.createBlockAndItemTags(NRBlockTagsProvider::new, NRItemTagsProvider::new);
    }
}
