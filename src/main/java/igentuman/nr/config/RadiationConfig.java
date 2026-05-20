package igentuman.nr.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class RadiationConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue WORLD_SIM_INTERVAL_TICKS;
    public static final ModConfigSpec.IntValue ENTITY_SIM_INTERVAL_TICKS;
    public static final ModConfigSpec.IntValue MAX_SOURCE_RADIUS_M;
    public static final ModConfigSpec.IntValue CHUNK_VECTOR_TTL_TICKS;
    public static final ModConfigSpec.BooleanValue STAGGER_ENTITIES;
    public static final ModConfigSpec.BooleanValue IGNORE_CREATIVE;
    public static final ModConfigSpec.BooleanValue IGNORE_SPECTATOR;
    public static final ModConfigSpec.DoubleValue ACTIVITY_FLOOR_BQ;
    public static final ModConfigSpec.DoubleValue STATIC_HALF_LIFE_YEARS;
    public static final ModConfigSpec.DoubleValue CONTAMINATION_SPREAD_FACTOR;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> IGNORED_ENTITIES;
    public static final ModConfigSpec.DoubleValue THRESHOLD_MILD;
    public static final ModConfigSpec.DoubleValue THRESHOLD_MODERATE;
    public static final ModConfigSpec.DoubleValue THRESHOLD_SEVERE;
    public static final ModConfigSpec.DoubleValue THRESHOLD_LETHAL;
    public static final ModConfigSpec.DoubleValue BASE_DECAY_SV_PER_HOUR;
    public static final ModConfigSpec.DoubleValue GY_PER_BQ_SECOND;
    public static final ModConfigSpec.DoubleValue ARMOR_BLOCKS_INVENTORY;
    public static final ModConfigSpec.DoubleValue INVENTORY_ALPHA_PASS;
    public static final ModConfigSpec.DoubleValue INVENTORY_BETA_PASS;
    public static final ModConfigSpec.BooleanValue DEBUG_RADIATION_VECTORS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("radiation");
        WORLD_SIM_INTERVAL_TICKS = b.comment("Chunk vector recompute / source decay / contamination spread interval")
                .defineInRange("world_sim_interval_ticks", 5, 1, 1200);
        ENTITY_SIM_INTERVAL_TICKS = b.comment("Entity sampling / dose accumulation / effect interval")
                .defineInRange("entity_sim_interval_ticks", 5, 1, 1200);
        MAX_SOURCE_RADIUS_M = b.comment("Hard cutoff radius for source contribution (meters)")
                .defineInRange("max_source_radius_m", 64, 1, 1024);
        CHUNK_VECTOR_TTL_TICKS = b.comment("ChunkRadVector freshness ttl")
                .defineInRange("chunk_vector_ttl_ticks", 20, 1, 1200);
        STAGGER_ENTITIES = b.comment("Spread entity dose calc across interval buckets")
                .define("stagger_entities", true);
        b.pop();

        b.push("entities");
        IGNORE_CREATIVE = b.define("ignore_creative", true);
        IGNORE_SPECTATOR = b.define("ignore_spectator", true);
        IGNORED_ENTITIES = b.defineList("ignored",
                java.util.List.of(
                        "minecraft:armor_stand",
                        "minecraft:item",
                        "minecraft:experience_orb",
                        "minecraft:area_effect_cloud"
                ),
                () -> "minecraft:item",
                o -> o instanceof String);
        b.pop();

        b.push("thresholds_sv_per_hour");
        THRESHOLD_MILD     = b.defineInRange("mild", 0.001, 0.0, 1.0e6);
        THRESHOLD_MODERATE = b.defineInRange("moderate", 0.01, 0.0, 1.0e6);
        THRESHOLD_SEVERE   = b.defineInRange("severe", 0.1, 0.0, 1.0e6);
        THRESHOLD_LETHAL   = b.defineInRange("lethal", 1.0, 0.0, 1.0e6);
        b.pop();

        b.push("recovery");
        BASE_DECAY_SV_PER_HOUR = b.defineInRange("base_decay_per_hour_sv", 0.0001, 0.0, 1.0);
        b.pop();

        b.push("conversion");
        GY_PER_BQ_SECOND = b.comment("Conversion factor from Bq to Gy/s at point of exposure. Physical baseline ~5e-18 for pure-gamma 1m distance; mod uses 1e-16 to balance contact handheld + simulation pipeline.")
                .defineInRange("gy_per_bq_second", 1.0e-16, 0.0, 1.0);
        b.pop();

        b.push("inventory");
        ARMOR_BLOCKS_INVENTORY = b.defineInRange("armor_blocks_inventory", 0.5, 0.0, 1.0);
        INVENTORY_ALPHA_PASS = b.comment("Fraction of alpha radiation that escapes inventory containers/clothing to reach the body. Alpha particles are stopped by ~1 cm of any material, so default is 0 (fully blocked). Raise to simulate exposed/handheld items.")
                .defineInRange("inventory_alpha_pass", 0.0, 0.0, 1.0);
        INVENTORY_BETA_PASS = b.comment("Fraction of beta radiation that escapes inventory containers/clothing to reach the body. Beta particles are stopped by a few mm of plastic or cm of cloth, default 0.2 (most blocked).")
                .defineInRange("inventory_beta_pass", 0.2, 0.0, 1.0);
        b.pop();

        b.push("debug");
        DEBUG_RADIATION_VECTORS = b.comment("Sync chunk radiation vector at player to client for in-world arrow rendering")
                .define("debug_radiation_vectors", false);
        b.pop();

        b.push("world_sources");
        ACTIVITY_FLOOR_BQ = b.defineInRange("activity_floor_bq", 1.0, 0.0, 1.0e30);
        CONTAMINATION_SPREAD_FACTOR = b.defineInRange("contamination_spread_factor", 0.001, 0.0, 1.0);
        STATIC_HALF_LIFE_YEARS = b.comment("Isotopes with effective half-life (after isotopeDecayMultiplier) above this many IRL years are treated as static: no decay math, no daughter ingrowth, no expiry. Activity Bq is still computed and shown. Default 1000 yr captures U-238/U-235/Pu-239.")
                .defineInRange("static_half_life_years", 1000.0, 0.0, 1.0e15);
        b.pop();

        SPEC = b.build();
    }

    private RadiationConfig() {}
}
