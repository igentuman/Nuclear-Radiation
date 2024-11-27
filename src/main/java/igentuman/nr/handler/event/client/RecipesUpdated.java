package igentuman.nr.handler.event.client;

import igentuman.nr.recipes.NcRecipeType;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static igentuman.nr.recipes.NcRecipeType.ALL_RECIPES;

public class RecipesUpdated {
    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(RecipesUpdated::recipesUpdated);
    }

    public static RecipeManager manager;
    public static void recipesUpdated(RecipesUpdatedEvent event) {
        manager = event.getRecipeManager();
        for (String name: ALL_RECIPES.keySet()) {
            NcRecipeType recipeType = ALL_RECIPES.get(name).getRecipeType();
            //recipeType.loadRecipes(RecipesUpdated.manager);
        }
    }
}
