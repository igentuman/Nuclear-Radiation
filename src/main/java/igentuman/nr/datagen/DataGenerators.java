package igentuman.nr.datagen;

import igentuman.nr.datagen.models.NCItemModels;
import igentuman.nr.datagen.recipes.NCRecipes;
import igentuman.nr.datagen.tags.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.List;

import static igentuman.nr.NuclearRadiation.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(event.includeServer(), new NCRecipes(generator));

        NCBlockTags blockTags = new NCBlockTags(generator, event);

        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new NCItemTags(generator, blockTags, event));
        generator.addProvider(event.includeServer(), new FluidTags(generator, event));
        generator.addProvider(event.includeServer(), new NCBiomeTags(generator, event));
        generator.addProvider(event.includeClient(), new NCItemModels(generator, event));
        generator.addProvider(event.includeClient(), new NCLanguageProvider(generator, "en_us"));
        generator.addProvider(event.includeClient(), new EmiLangProvider(generator, "en_gb"));
    }
}