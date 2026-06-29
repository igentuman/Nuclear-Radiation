package igentuman.nr.shielding;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class ShieldingRegistry {

    public record Coeffs(double xray, double neutron) {}

    private ShieldingRegistry() {}

    /** Convenience API: bind a block directly to raw coefficients. */
    public static void register(Block block, double xray, double neutron) {
        ShieldingBindings.putBlock(BuiltInRegistries.BLOCK.getKey(block),
                ShieldingBindings.ShieldEntry.ofRaw(new Coeffs(xray, neutron)));
    }

    public static Coeffs get(BlockState state) {
        if (state == null || state.isAir()) return null;
        if (state.getBlock() instanceof IShieldingBlock s) {
            return new Coeffs(s.xrayAttenuationCoeff(), s.neutronAttenuationCoeff());
        }
        return ShieldingBindings.resolve(state);
    }

    public static boolean shields(BlockState state) {
        return get(state) != null;
    }
}
