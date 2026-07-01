package igentuman.nr.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Radiation-driven entity mutation. Not a crafting recipe — it is loaded through the vanilla
 * {@link net.minecraft.world.item.crafting.RecipeManager} purely so it is datapack- and
 * KubeJS-driven. The {@link Recipe} crafting methods are inert; matching is done against a
 * live entity and its accumulated dose via {@link #matchesEntity} / {@link #inRange}.
 */
public class MutationRecipe implements Recipe<RecipeInput> {

    private final EntityIngredient input;
    private final EntityResult result;
    private final double totalDoseSv;
    private final double minSvPerHour;
    private final double maxSvPerHour;
    private final float chance;

    public MutationRecipe(EntityIngredient input, EntityResult result,
                          double totalDoseSv, double minSvPerHour, double maxSvPerHour, float chance) {
        this.input = input;
        this.result = result;
        this.totalDoseSv = totalDoseSv;
        this.minSvPerHour = minSvPerHour;
        this.maxSvPerHour = maxSvPerHour;
        this.chance = chance;
    }

    public EntityIngredient input() { return input; }
    public EntityResult result() { return result; }
    public double totalDoseSv() { return totalDoseSv; }
    public double minSvPerHour() { return minSvPerHour; }
    public double maxSvPerHour() { return maxSvPerHour; }
    public float chance() { return chance; }

    public boolean matchesEntity(LivingEntity entity) {
        return input.test(entity);
    }

    public boolean inRange(double svTotalCareer, double svPerHour) {
        return svTotalCareer >= totalDoseSv
                && svPerHour >= minSvPerHour
                && svPerHour <= maxSvPerHour;
    }

    // --- inert Recipe boilerplate (this recipe is not item-crafted) ---

    @Override
    public boolean matches(RecipeInput input, Level level) { return false; }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) { return ItemStack.EMPTY; }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) { return ItemStack.EMPTY; }

    @Override
    public boolean isSpecial() { return true; }

    @Override
    public RecipeSerializer<?> getSerializer() { return NRRecipes.MUTATION_SERIALIZER.get(); }

    @Override
    public RecipeType<?> getType() { return NRRecipes.MUTATION_TYPE.get(); }

    public static class Serializer implements RecipeSerializer<MutationRecipe> {

        public static final MapCodec<MutationRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                EntityIngredient.CODEC.fieldOf("input").forGetter(r -> r.input),
                EntityResult.CODEC.fieldOf("result").forGetter(r -> r.result),
                com.mojang.serialization.Codec.DOUBLE.fieldOf("total_dose_sv").forGetter(r -> r.totalDoseSv),
                com.mojang.serialization.Codec.DOUBLE.fieldOf("min_sv_per_hour").forGetter(r -> r.minSvPerHour),
                com.mojang.serialization.Codec.DOUBLE.optionalFieldOf("max_sv_per_hour", Double.MAX_VALUE).forGetter(r -> r.maxSvPerHour),
                com.mojang.serialization.Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(r -> r.chance)
        ).apply(inst, MutationRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MutationRecipe> STREAM_CODEC = StreamCodec.composite(
                EntityIngredient.STREAM_CODEC, r -> r.input,
                EntityResult.STREAM_CODEC, r -> r.result,
                ByteBufCodecs.DOUBLE, r -> r.totalDoseSv,
                ByteBufCodecs.DOUBLE, r -> r.minSvPerHour,
                ByteBufCodecs.DOUBLE, r -> r.maxSvPerHour,
                ByteBufCodecs.FLOAT, r -> r.chance,
                MutationRecipe::new);

        @Override
        public MapCodec<MutationRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MutationRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
