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
                .comment("Apply the ionization glow to highly radioactive dropped items.")
                .define("enabled", true);
        GLOW_THRESHOLD_BQ = BUILDER
                .comment("Minimum total activity (Bq) of a dropped stack for its glow. Default 1e7 (10 MBq) includes a single fallout dust item.")
                .defineInRange("threshold_bq", 1.0e7, 0.0, 1.0e30);
        GLOW_INTENSITY = BUILDER
                .comment("Zero disables the ionization glow; nonzero values enable its current fixed brightness.")
                .defineInRange("intensity", 0.5, 0.0, 1.0);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
