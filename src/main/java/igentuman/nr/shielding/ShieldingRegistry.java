package igentuman.nr.shielding;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public final class ShieldingRegistry {

    public record Coeffs(double xray, double neutron) {}

    private static final Map<Block, Coeffs> BY_BLOCK = new HashMap<>();
    private static volatile boolean defaultsInstalled = false;

    private ShieldingRegistry() {}

    public static void register(Block block, double xray, double neutron) {
        BY_BLOCK.put(block, new Coeffs(xray, neutron));
    }

    public static Coeffs get(BlockState state) {
        if (state.isAir()) return null;
        ensureDefaults();
        if (state.getBlock() instanceof IShieldingBlock s) {
            return new Coeffs(s.xrayAttenuationCoeff(), s.neutronAttenuationCoeff());
        }
        return BY_BLOCK.get(state.getBlock());
    }

    public static boolean shields(BlockState state) {
        return get(state) != null;
    }

    private static synchronized void ensureDefaults() {
        if (defaultsInstalled) return;
        defaultsInstalled = true;
        // x-ray μ in 1/m, neutron μ in 1/m. Approximate per-block (1m thickness).
        register(Blocks.STONE,        0.20, 0.05);
        register(Blocks.COBBLESTONE,  0.20, 0.05);
        register(Blocks.DEEPSLATE,    0.25, 0.05);
        register(Blocks.IRON_BLOCK,   0.60, 0.10);
        register(Blocks.GOLD_BLOCK,   1.20, 0.05);
        register(Blocks.LAPIS_BLOCK,  0.30, 0.10);
        register(Blocks.OBSIDIAN,     0.45, 0.10);
        register(Blocks.WATER,        0.05, 0.40);
        register(Blocks.ICE,          0.05, 0.35);
        register(Blocks.PACKED_ICE,   0.06, 0.40);
        register(Blocks.SOUL_SAND,    0.10, 0.30);
        register(Blocks.NETHERITE_BLOCK, 1.50, 0.20);
        register(Blocks.DIRT,         0.10, 0.10);
        register(Blocks.SAND,         0.08, 0.08);
        register(Blocks.CLAY,         0.15, 0.20);
    }
}
