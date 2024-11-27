package igentuman.nr.datagen.recipes;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;

import java.util.function.Consumer;

public class NCRecipes extends RecipeProvider {

    public NCRecipes(DataGenerator generatorIn) {
        super(generatorIn.getPackOutput());
    }
    public Consumer<FinishedRecipe> consumer;

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        this.consumer = consumer;

    }

}