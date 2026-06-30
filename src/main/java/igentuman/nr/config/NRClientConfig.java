package igentuman.nr.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class NRClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue GLOW_ENABLED;
    public static final ModConfigSpec.DoubleValue GLOW_THRESHOLD_BQ;
    public static final ModConfigSpec.DoubleValue GLOW_INTENSITY;

    static {
        BUILDER.push("item_glow");
        GLOW_ENABLED = BUILDER
                .comment("Enable the ionization glow post-effect on highly radioactive dropped items.")
                .define("enabled", true);
        GLOW_THRESHOLD_BQ = BUILDER
                .comment("Minimum total activity (Bq) of a dropped item for it to glow. Default 1.0e6 (1 MBq), mirrors world_source_min_bq.")
                .defineInRange("threshold_bq", 1.0e6, 0.0, 1.0e30);
        GLOW_INTENSITY = BUILDER
                .comment("Glow strength multiplier (0..1). Higher = brighter aura.")
                .defineInRange("intensity", 0.6, 0.0, 1.0);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
