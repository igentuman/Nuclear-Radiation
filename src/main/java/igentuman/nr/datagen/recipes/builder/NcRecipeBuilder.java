package igentuman.nr.datagen.recipes.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nr.NuclearRadiation.MODID;

public class NcRecipeBuilder extends RecipeBuilder<NcRecipeBuilder> {

    private static NcRecipeBuilder instance;
    private double timeModifier = 1D;
    private double radiation = 1D;
    private double powerModifier = 1D;

    public double coolingRate = 0;
    public double heatRequired = 0;

    public String ID;
    private double rarityModifier = 1D;
    private double temperature = 0D;
    private List<String> outputItemsText = List.of();

    protected NcRecipeBuilder(String id) {
        super(ncSerializer(id));
        ID = id;
    }

    public static NcRecipeBuilder get(String id) {
        instance = new NcRecipeBuilder(id);
        return instance;
    }




    public NcRecipeBuilder modifiers(double timeModifier, double radiation, double powerModifier, double rarity) {
        this.timeModifier = timeModifier;
        this.radiation = radiation;
        this.powerModifier = powerModifier;
        this.rarityModifier = rarity;
        return this;
    }

    public NcRecipeBuilder modifiers(double timeModifier, double radiation, double powerModifier) {
        this.timeModifier = timeModifier;
        this.radiation = radiation;
        this.powerModifier = powerModifier;
        return this;
    }

    @Override
    protected NcRecipeResult getResult(ResourceLocation id) {
        return new NcRecipeResult(id);
    }

    public ResourceLocation getRecipeId()
    {
        StringBuilder name = new StringBuilder();

        name.replace(name.length()-1, name.length(), "");

        return new ResourceLocation(MODID, ID+"/"+recipeIdReplacements(name.toString()));
    }

    protected String recipeIdReplacements(String val) {
        val = val.replace("nuclearradiation_", "");
        val = val.replace("depleted_fuel", "d_f");
        return val;
    }

    public void build(Consumer<FinishedRecipe> consumer) {
        build(consumer, getRecipeId());
    }

    public NcRecipeBuilder temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public NcRecipeBuilder coolingRate(double coolingRate) {
        this.coolingRate = coolingRate;
        return this;
    }

    public NcRecipeBuilder heatRequired(double heatRequired) {
        this.heatRequired = heatRequired;
        return this;
    }

    private boolean useInputForId = false;

    public NcRecipeBuilder useInputForId(boolean b) {
        useInputForId = b;
        return this;
    }

    public class NcRecipeResult extends RecipeResult {

        protected NcRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            JsonArray inputJson = new JsonArray();


        }
    }
}