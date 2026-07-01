package igentuman.nr.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

/**
 * Result side of a mutation recipe: an {@link EntityType} plus an optional NBT tag that is
 * merged onto the freshly created entity, letting recipes set variant data on the output
 * (e.g. {@code "{Color:14b}"} for a red sheep).
 */
public record EntityResult(EntityType<?> type, Optional<CompoundTag> nbt) {

    public static final Codec<EntityResult> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(EntityResult::type),
            EntityIngredient.TAG_CODEC.optionalFieldOf("nbt").forGetter(EntityResult::nbt)
    ).apply(inst, EntityResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityResult> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ENTITY_TYPE), EntityResult::type,
            ByteBufCodecs.OPTIONAL_COMPOUND_TAG, EntityResult::nbt,
            EntityResult::new);

    /**
     * Spawns the output at {@code from}'s position/rotation. Returns the spawned entity, or
     * {@code null} if the type is not a living entity (mutations only replace living entities).
     */
    public LivingEntity spawn(ServerLevel level, LivingEntity from) {
        Entity created = type.create(level);
        if (!(created instanceof LivingEntity out)) {
            if (created != null) created.discard();
            return null;
        }
        if (nbt.isPresent()) {
            CompoundTag tag = out.saveWithoutId(new CompoundTag());
            tag.merge(nbt.get());
            out.load(tag);
        }
        out.moveTo(from.getX(), from.getY(), from.getZ(), from.getYRot(), from.getXRot());
        out.setYHeadRot(from.getYHeadRot());
        level.addFreshEntity(out);
        return out;
    }
}
