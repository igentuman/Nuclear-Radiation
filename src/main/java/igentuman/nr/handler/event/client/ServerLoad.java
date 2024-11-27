package igentuman.nr.handler.event.client;

import igentuman.nr.recipes.NcRecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


import static igentuman.nr.NuclearRadiation.MODID;
import static igentuman.nr.recipes.NcRecipeType.ALL_RECIPES;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerLoad {
    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(ServerLoad::onLevelLoad);
    }
    public static boolean initialized = false;
    public static void onLevelLoad(LevelEvent.Load event) {
        if(initialized) return;
        if(event.getLevel().getServer() == null) return;
        Level level = event.getLevel().getServer().getLevel(Level.OVERWORLD);
        for (String name: ALL_RECIPES.keySet()) {
            NcRecipeType<?> recipeType = ALL_RECIPES.get(name).getRecipeType();
            recipeType.loadRecipes(level);
        }
        initialized = true;
    }
}
