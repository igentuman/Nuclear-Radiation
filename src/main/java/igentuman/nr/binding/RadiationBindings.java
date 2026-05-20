package igentuman.nr.binding;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.RadiationProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class RadiationBindings {

    private RadiationBindings() {}

    public static RadiationProfile of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return RadiationProfile.empty();
        RadiationComponent comp = stack.get(RadiationComponent.TYPE.get());
        if (comp != null && !comp.isEmpty()) return comp.toProfile(0L);

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Supplier<RadiationProfile> direct = Bindings.getItem(id);
        if (direct != null) return direct.get();

        for (Map.Entry<TagKey<Item>, Supplier<RadiationProfile>> e : Bindings.itemTags().entrySet()) {
            if (stack.is(e.getKey())) return e.getValue().get();
        }
        return RadiationProfile.empty();
    }

    public static RadiationProfile of(BlockState state) {
        if (state == null) return RadiationProfile.empty();
        Block block = state.getBlock();
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        Supplier<RadiationProfile> direct = Bindings.getBlock(id);
        if (direct != null) return direct.get();

        for (Map.Entry<TagKey<Block>, Supplier<RadiationProfile>> e : Bindings.blockTags().entrySet()) {
            if (state.is(e.getKey())) return e.getValue().get();
        }
        return RadiationProfile.empty();
    }

    public static RadiationProfile of(FluidState fluidState) {
        if (fluidState == null) return RadiationProfile.empty();
        Fluid fluid = fluidState.getType();
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        Supplier<RadiationProfile> direct = Bindings.getFluid(id);
        if (direct != null) return direct.get();

        for (Map.Entry<TagKey<Fluid>, Supplier<RadiationProfile>> e : Bindings.fluidTags().entrySet()) {
            if (fluidState.is(e.getKey())) return e.getValue().get();
        }
        return RadiationProfile.empty();
    }

    public static boolean isRadioactive(ItemStack stack) {
        return !of(stack).isEmpty();
    }

    public static Optional<RadiationProfile> forItem(ItemStack s) {
        RadiationProfile p = of(s);
        return p.isEmpty() ? Optional.empty() : Optional.of(p);
    }

    public static ResourceLocation modId(String path) {
        return ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, path);
    }
}
