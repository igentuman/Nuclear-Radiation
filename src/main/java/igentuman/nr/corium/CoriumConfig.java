package igentuman.nr.corium;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CoriumConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CORIUM_TICK_INTERVAL;
    public static final ModConfigSpec.DoubleValue CORIUM_BURN_RADIUS;
    public static final ModConfigSpec.IntValue CORIUM_FIRE_SECONDS;
    public static final ModConfigSpec.DoubleValue CORIUM_DAMAGE;
    public static final ModConfigSpec.DoubleValue CORIUM_MELT_RATE;
    public static final ModConfigSpec.IntValue CORIUM_SOLIDIFY_TICKS;
    public static final ModConfigSpec.IntValue CORIUM_WATER_COOLING;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("corium");
        CORIUM_TICK_INTERVAL = BUILDER
                .comment("Game ticks between corium hazard updates (melt/burn/solidify). Lower = more aggressive and costlier.")
                .defineInRange("tick_interval", 10, 1, 100);
        CORIUM_BURN_RADIUS = BUILDER
                .comment("Radius in blocks around each corium source block within which entities are ignited/damaged. 0 disables.")
                .defineInRange("burn_radius", 5.0, 0.0, 32.0);
        CORIUM_FIRE_SECONDS = BUILDER
                .comment("Seconds of fire applied to entities within the burn radius.")
                .defineInRange("fire_seconds", 8, 0, 60);
        CORIUM_DAMAGE = BUILDER
                .comment("Direct damage per update dealt to entities within the burn radius. 0 disables.")
                .defineInRange("burn_damage", 4.0, 0.0, 100.0);
        CORIUM_MELT_RATE = BUILDER
                .comment("Melt aggressiveness for the block beneath corium. Per-update melt chance = rate / (hardness + 1). 0 disables melting.")
                .defineInRange("melt_rate", 0.4, 0.0, 10.0);
        CORIUM_SOLIDIFY_TICKS = BUILDER
                .comment("Mean game ticks before a corium source block solidifies into corium_block (randomized).")
                .defineInRange("solidify_ticks", 2400, 20, 200000);
        CORIUM_WATER_COOLING = BUILDER
                .comment("Water contacts required to quench a corium block into corium_block. Each adjacent water block evaporates and counts once per update.")
                .defineInRange("water_cooling_contacts", 3, 1, 100);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
