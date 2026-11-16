package igentuman.nr.api.binding;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.compat.ntm.NtmCompatibility;
import net.minecraft.resources.Identifier;
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
        if (binding != null) return binding.get();
        // Imported fallback: explicit NR components/bindings above always win.
        return NtmCompatibility.itemProfile(stack);
    }

    public static RadiationProfile of(BlockState state) {
        if (state == null) return RadiationProfile.empty();
        Supplier<RadiationProfile> binding = Bindings.resolvedBlock(state.getBlock());
        if (binding != null) return binding.get();
        // Imported fallback: explicit NR bindings above always win.
        return NtmCompatibility.blockProfile(state);
    }

    public static RadiationProfile of(FluidState fluidState) {
        if (fluidState == null) return RadiationProfile.empty();
        Supplier<RadiationProfile> binding = Bindings.resolvedFluid(fluidState.getType());
        return binding != null ? binding.get() : RadiationProfile.empty();
    }

    /** NR component or binding, excluding imported fallbacks. */
    public static boolean explicit(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        RadiationComponent comp = stack.get(RadiationComponent.TYPE.get());
        return (comp != null && !comp.isEmpty()) || Bindings.resolvedItem(stack.getItem()) != null;
    }

    public static boolean explicit(BlockState state) {
        return state != null && Bindings.resolvedBlock(state.getBlock()) != null;
    }

    /** Non-empty block profile that comes from an imported fallback rather than an NR binding. */
    public static boolean imported(BlockState state) {
        return state != null && !explicit(state) && !NtmCompatibility.blockProfile(state).isEmpty();
    }

    public static boolean isRadioactive(ItemStack stack) {
        return !of(stack).isEmpty();
    }

    public static Optional<RadiationProfile> forItem(ItemStack s) {
        RadiationProfile p = of(s);
        return p.isEmpty() ? Optional.empty() : Optional.of(p);
    }

    public static Identifier modId(String path) {
        return Identifier.fromNamespaceAndPath(NuclearRadiation.MODID, path);
    }
}
