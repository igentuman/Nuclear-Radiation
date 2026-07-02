package igentuman.nr.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Radiation-driven block transformation. Not a crafting recipe — it is loaded through the vanilla
 * {@link net.minecraft.world.item.crafting.RecipeManager} purely so it is datapack- and
 * KubeJS-driven. The {@link Recipe} crafting methods are inert; matching happens in the block
 * irradiation simulator against a candidate block and the local attenuated Bq it receives.
 */
public class BlockIrradiationRecipe implements Recipe<RecipeInput> {

    /** {@code input} is a block id ({@code "minecraft:grass_block"}) or block tag ({@code "#minecraft:dirt"}). */
    private final String input;
    private final double minBq;
    private final List<BlockOutput> outputs;
    private final float chance;

    public BlockIrradiationRecipe(String input, double minBq, List<BlockOutput> outputs, float chance) {
        this.input = input;
        this.minBq = minBq;
        this.outputs = outputs;
        this.chance = chance;
    }

    public String input() { return input; }
    public double minBq() { return minBq; }
    public List<BlockOutput> outputs() { return outputs; }
    public float chance() { return chance; }

    /** Resolves the input (block id or tag) to concrete blocks. Called once at cache rebuild. */
    public List<Block> resolvedInputBlocks() {
        if (input.startsWith("#")) {
            ResourceLocation rl = ResourceLocation.tryParse(input.substring(1));
            if (rl == null) return List.of();
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, rl);
            return BuiltInRegistries.BLOCK.getTag(tag)
                    .map(named -> named.stream().map(Holder::value).toList())
                    .orElse(List.of());
        }
        ResourceLocation rl = ResourceLocation.tryParse(input);
        if (rl == null) return List.of();
        return BuiltInRegistries.BLOCK.getOptional(rl).map(List::<Block>of).orElse(List.of());
    }

    public record BlockOutput(Block block, int weight) {
        public static final com.mojang.serialization.Codec<BlockOutput> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockOutput::block),
                com.mojang.serialization.Codec.INT.optionalFieldOf("weight", 1).forGetter(BlockOutput::weight)
        ).apply(inst, BlockOutput::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, BlockOutput> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.BLOCK), BlockOutput::block,
                ByteBufCodecs.VAR_INT, BlockOutput::weight,
                BlockOutput::new);
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
    public RecipeSerializer<?> getSerializer() { return NRRecipes.BLOCK_IRRADIATION_SERIALIZER.get(); }

    @Override
    public RecipeType<?> getType() { return NRRecipes.BLOCK_IRRADIATION_TYPE.get(); }

    public static class Serializer implements RecipeSerializer<BlockIrradiationRecipe> {

        public static final MapCodec<BlockIrradiationRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                com.mojang.serialization.Codec.STRING.fieldOf("input").forGetter(r -> r.input),
                com.mojang.serialization.Codec.DOUBLE.fieldOf("min_bq").forGetter(r -> r.minBq),
                BlockOutput.CODEC.listOf().fieldOf("outputs").forGetter(r -> r.outputs),
                com.mojang.serialization.Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(r -> r.chance)
        ).apply(inst, BlockIrradiationRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, BlockIrradiationRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, r -> r.input,
                ByteBufCodecs.DOUBLE, r -> r.minBq,
                BlockOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.outputs,
                ByteBufCodecs.FLOAT, r -> r.chance,
                BlockIrradiationRecipe::new);

        @Override
        public MapCodec<BlockIrradiationRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BlockIrradiationRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
