package igentuman.nr.api.binding;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.RadiationProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;
import java.util.function.Supplier;

public final class RadiationBindings {

    private RadiationBindings() {}

    public static RadiationProfile of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return RadiationProfile.empty();
        RadiationComponent comp = stack.get(RadiationComponent.TYPE.get());
        if (comp != null && !comp.isEmpty()) return comp.toProfile(0L);

        Supplier<RadiationProfile> binding = Bindings.resolvedItem(stack.getItem());
        return binding != null ? binding.get() : RadiationProfile.empty();
    }

    public static RadiationProfile of(BlockState state) {
        if (state == null) return RadiationProfile.empty();
        Supplier<RadiationProfile> binding = Bindings.resolvedBlock(state.getBlock());
        return binding != null ? binding.get() : RadiationProfile.empty();
    }

    public static RadiationProfile of(FluidState fluidState) {
        if (fluidState == null) return RadiationProfile.empty();
        Supplier<RadiationProfile> binding = Bindings.resolvedFluid(fluidState.getType());
        return binding != null ? binding.get() : RadiationProfile.empty();
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
