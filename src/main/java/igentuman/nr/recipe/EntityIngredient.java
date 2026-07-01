package igentuman.nr.recipe;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

/**
 * Match side of a mutation recipe: an {@link EntityType} plus an optional NBT sub-tag.
 * When NBT is present, an entity matches only if the given tag is a subset of the
 * entity's saved NBT (partial match), so variant data such as {@code {Color:0b}} or
 * {@code {Profession:"minecraft:farmer"}} can be required.
 */
public record EntityIngredient(EntityType<?> type, Optional<CompoundTag> nbt) {

    /** SNBT-string codec so JSON authors keep exact NBT types (e.g. {@code "{Color:0b}"}). */
    public static final Codec<CompoundTag> TAG_CODEC = Codec.STRING.comapFlatMap(
            s -> {
                try {
                    return DataResult.success(TagParser.parseTag(s));
                } catch (CommandSyntaxException e) {
                    return DataResult.error(() -> "Invalid NBT: " + e.getMessage());
                }
            },
            CompoundTag::toString);

    public static final Codec<EntityIngredient> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(EntityIngredient::type),
            TAG_CODEC.optionalFieldOf("nbt").forGetter(EntityIngredient::nbt)
    ).apply(inst, EntityIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityIngredient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ENTITY_TYPE), EntityIngredient::type,
            ByteBufCodecs.OPTIONAL_COMPOUND_TAG, EntityIngredient::nbt,
            EntityIngredient::new);

    public boolean test(Entity entity) {
        if (entity.getType() != type) return false;
        if (nbt.isEmpty()) return true;
        return NbtUtils.compareNbt(nbt.get(), entity.saveWithoutId(new CompoundTag()), true);
    }
}
