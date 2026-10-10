package igentuman.nr.network;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.recipe.BlockIrradiationRecipe;
import igentuman.nr.recipe.MutationRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/** Sends the server's datapack recipes to the client for recipe viewers. */
public record RecipeSyncPayload(List<MutationRecipe> mutations, List<BlockIrradiationRecipe> blockIrradiations)
        implements CustomPacketPayload {

    public static final Type<RecipeSyncPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(NuclearRadiation.MODID, "recipe_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeSyncPayload> STREAM_CODEC = StreamCodec.composite(
            MutationRecipe.Serializer.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeSyncPayload::mutations,
            BlockIrradiationRecipe.Serializer.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeSyncPayload::blockIrradiations,
            RecipeSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleData(IPayloadContext context) {
        context.enqueueWork(() -> ClientRecipeCache.update(this));
    }
}
