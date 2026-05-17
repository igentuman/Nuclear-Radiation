package igentuman.nr.datagen;

import igentuman.nr.NuclearRadiation;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = NuclearRadiation.MODID)
public final class NRDataGenerators {
    private NRDataGenerators() {}

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        gen.addProvider(event.includeServer(), new RadiationBindingProvider(output));
    }
}
