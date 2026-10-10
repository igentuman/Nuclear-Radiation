package igentuman.nr.client;

import com.mojang.serialization.MapCodec;
import igentuman.nr.items.GeigerCounterItem;
import igentuman.nr.network.ClientRadiationCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

public record GeigerModelProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<GeigerModelProperty> CODEC = MapCodec.unit(new GeigerModelProperty());

    @Override
    public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
        if (owner == null || owner.asLivingEntity() != Minecraft.getInstance().player) {
            return 0;
        }
        return (float) Math.floor(GeigerCounterItem.responseT(ClientRadiationCache.svPerHourAmbient()) * 5.0);
    }

    @Override
    public MapCodec<GeigerModelProperty> type() {
        return CODEC;
    }
}
