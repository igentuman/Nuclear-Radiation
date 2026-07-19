package igentuman.nr.radiation.simulation.containers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Map;

public final class ContainerAttenuationRegistry {

    private static final Map<BlockEntityType<?>, Double> BY_TYPE = new HashMap<>();
    private static final Map<ResourceLocation, Double> BY_ID = new HashMap<>();

    private ContainerAttenuationRegistry() {}

    public static void register(BlockEntityType<?> type, double attenuation) {
        BY_TYPE.put(type, clamp(attenuation));
    }

    public static void register(ResourceLocation id, double attenuation) {
        BY_ID.put(id, clamp(attenuation));
    }

    public static double get(BlockEntityType<?> type, ResourceLocation id) {
        Double v = BY_TYPE.get(type);
        if (v != null) return v;
        v = BY_ID.get(id);
        return v == null ? 0.0 : v;
    }

    private static double clamp(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }
}
