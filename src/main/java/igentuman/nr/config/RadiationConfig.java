package igentuman.nr.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

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
    public static final ModConfigSpec.DoubleValue WORLD_SOURCE_MIN_BQ;
    public static final ModConfigSpec.DoubleValue STATIC_HALF_LIFE_YEARS;
    public static final ModConfigSpec.DoubleValue CONTAMINATION_SPREAD_FACTOR;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> IGNORED_ENTITIES;
    public static final ModConfigSpec.DoubleValue THRESHOLD_MILD;
    public static final ModConfigSpec.DoubleValue THRESHOLD_MODERATE;
    public static final ModConfigSpec.DoubleValue THRESHOLD_SEVERE;
    public static final ModConfigSpec.DoubleValue THRESHOLD_LETHAL;
    public static final ModConfigSpec.DoubleValue CAREER_MILD;
    public static final ModConfigSpec.DoubleValue CAREER_MODERATE;
    public static final ModConfigSpec.DoubleValue CAREER_SEVERE;
    public static final ModConfigSpec.DoubleValue CAREER_LETHAL;
    public static final ModConfigSpec.DoubleValue TOTAL_SV_SCALE_K;
    public static final ModConfigSpec.DoubleValue BASE_DECAY_SV_PER_HOUR;
    public static final ModConfigSpec.DoubleValue GY_PER_BQ_SECOND;
    public static final ModConfigSpec.DoubleValue ARMOR_BLOCKS_INVENTORY;
    public static final ModConfigSpec.DoubleValue INVENTORY_ALPHA_PASS;
    public static final ModConfigSpec.DoubleValue INVENTORY_BETA_PASS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> IGNORED_MENUS;
    public static final ModConfigSpec.BooleanValue DEBUG_RADIATION_VECTORS;
    public static final ModConfigSpec.DoubleValue DEFAULT_BACKGROUND_USV_PER_HOUR;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> LEVEL_BACKGROUND_USV_PER_HOUR;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BIOME_BACKGROUND_USV_PER_HOUR;
    public static final ModConfigSpec.BooleanValue BLOCK_IRRADIATION_ENABLED;
    public static final ModConfigSpec.IntValue BLOCK_IRRADIATION_INTERVAL_TICKS;
    public static final ModConfigSpec.DoubleValue BLOCK_IRRADIATION_MIN_SOURCE_BQ;
    public static final ModConfigSpec.IntValue BLOCK_IRRADIATION_RADIUS;
    public static final ModConfigSpec.IntValue BLOCK_IRRADIATION_RAYS;
    public static final ModConfigSpec.IntValue BLOCK_IRRADIATION_MAX_SOURCES;
    public static final ModConfigSpec.IntValue BLOCK_IRRADIATION_MAX_TRANSFORMS;
    public static final ModConfigSpec.DoubleValue GAS_SOURCE_THRESHOLD_BQ;
    public static final ModConfigSpec.IntValue GAS_BASE_RADIUS;
    public static final ModConfigSpec.DoubleValue GAS_RADIUS_STEP_BQ;
    public static final ModConfigSpec.IntValue GAS_MAX_RADIUS;
    public static final ModConfigSpec.DoubleValue GAS_INHALE_CHANCE;
    public static final ModConfigSpec.DoubleValue GAS_POLLUTION_PER_INHALE;
    public static final ModConfigSpec.DoubleValue LUNG_DUST_POLLUTION_PER_INTERVAL;
    public static final ModConfigSpec.DoubleValue LUNG_RECOVERY_PER_INTERVAL;
    public static final ModConfigSpec.DoubleValue LUNG_MID_THRESHOLD;
    public static final ModConfigSpec.DoubleValue LUNG_HIGH_THRESHOLD;
    public static final ModConfigSpec.DoubleValue LUNG_CANCER_DAMAGE;
    public static final ModConfigSpec.IntValue LUNG_CANCER_DAMAGE_INTERVAL_TICKS;

    private static volatile Map<ResourceLocation, Double> levelBackgroundCache;
    private static volatile Map<ResourceLocation, Double> biomeBackgroundCache;
    private static volatile Set<ResourceLocation> ignoredMenusCache;

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
                .defineInRange("chunk_vector_ttl_ticks", 40, 1, 1200);
        STAGGER_ENTITIES = b.comment("Spread entity dose calc across interval buckets")
                .define("stagger_entities", true);
        b.pop();

        b.push("entities");
        IGNORE_CREATIVE = b.comment("Creative players are still simulated and synced (HUD/geiger), but exempt from radiation harm")
                .define("ignore_creative", true);
        IGNORE_SPECTATOR = b.comment("Spectator players are still simulated and synced (HUD/geiger), but exempt from radiation harm")
                .define("ignore_spectator", true);
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
        THRESHOLD_MILD     = b.comment("MEDIUM band: 1 mSv/h").defineInRange("mild", 0.001, 0.0, 1.0e6);
        THRESHOLD_MODERATE = b.comment("ELEVATED band: 500 mSv/h").defineInRange("moderate", 0.5, 0.0, 1.0e6);
        THRESHOLD_SEVERE   = b.comment("HIGH band: 10 Sv/h").defineInRange("severe", 10.0, 0.0, 1.0e6);
        THRESHOLD_LETHAL   = b.comment("EXTREME band: 100 Sv/h").defineInRange("lethal", 100.0, 0.0, 1.0e6);
        TOTAL_SV_SCALE_K   = b.comment("Legacy chronic scaling constant. No longer used by the stage formula (kept for datapack/back-compat).")
                .defineInRange("total_sv_scale_k", 5.0, 1.0e-6, 1.0e6);
        b.pop();

        b.push("thresholds_career_sv");
        b.comment("Accumulated career dose (real Sv) that triggers each harm stage, anchored to acute",
                "whole-body radiobiology (LD50 ~4-5 Sv). Entity stage = max(rate band, career band).");
        CAREER_MILD     = b.comment("Stage 1 at this career Sv").defineInRange("mild", 0.5, 0.0, 1.0e6);
        CAREER_MODERATE = b.comment("Stage 2 at this career Sv").defineInRange("moderate", 2.0, 0.0, 1.0e6);
        CAREER_SEVERE   = b.comment("Stage 3 at this career Sv").defineInRange("severe", 5.0, 0.0, 1.0e6);
        CAREER_LETHAL   = b.comment("Stage 4 at this career Sv").defineInRange("lethal", 8.0, 0.0, 1.0e6);
        b.pop();

        b.push("recovery");
        BASE_DECAY_SV_PER_HOUR = b.defineInRange("base_decay_per_hour_sv", 0.0001, 0.0, 1.0);
        b.pop();

        b.push("conversion");
        GY_PER_BQ_SECOND = b.comment("Conversion factor from Bq to Gy/s at point of exposure. Physical baseline ~5e-18 for pure-gamma 1m distance; mod uses 1e-16 to balance contact handheld + simulation pipeline.")
                .defineInRange("gy_per_bq_second", 1.0e-16, 0.0, 1.0);
        b.pop();

        b.push("inventory");
        ARMOR_BLOCKS_INVENTORY = b.defineInRange("armor_blocks_inventory", 0.75, 0.0, 1.0);
        INVENTORY_ALPHA_PASS = b.comment("Fraction of alpha radiation that escapes inventory containers/clothing to reach the body. Alpha particles are stopped by ~1 cm of any material, so default is 0 (fully blocked). Raise to simulate exposed/handheld items.")
                .defineInRange("inventory_alpha_pass", 0.0, 0.0, 1.0);
        INVENTORY_BETA_PASS = b.comment("Fraction of beta radiation that escapes inventory containers/clothing to reach the body. Beta particles are stopped by a few mm of plastic or cm of cloth, default 0.2 (most blocked).")
                .defineInRange("inventory_beta_pass", 0.2, 0.0, 1.0);
        IGNORED_MENUS = b.comment("List of GUI menu Registry IDs to ignore (e.g. appeng:crafting_terminal). Items in these menus will not emit radiation.")
                .defineList("ignored_menus", List.of(), o -> o instanceof String);
        b.pop();

        b.push("background");
        DEFAULT_BACKGROUND_USV_PER_HOUR = b.comment("Global background radiation in uSv/h applied everywhere when no level/biome override matches.")
                .defineInRange("default_usv_per_hour", 0.1, 0.0, 1.0e6);
        LEVEL_BACKGROUND_USV_PER_HOUR = b.comment(
                        "Per-dimension background radiation in uSv/h. Format: \"<dim_id>=<value>\".",
                        "Overrides default; biome entries override this.")
                .defineList("level_usv_per_hour",
                        List.of(
                                "minecraft:the_nether=1.5",
                                "nuclearcraft:wasteland=1500.5",
                                "nuclearcraftneohaul:wasteland=1500.5",
                                "creatingspace:earth_orbit=2500.0",
                                "creatingspace:mars=13000.0",
                                "creatingspace:the_moon=15000.0",
                                "creatingspace:moon_orbit=16000.0",
                                "creatingspace:mars_orbit=13500.0",
                                "creatingspace:venus=14000.0",
                                "minecraft:the_end=1.3"
                        ),
                        () -> "minecraft:overworld=0.1",
                        o -> o instanceof String && ((String) o).contains("="));
        BIOME_BACKGROUND_USV_PER_HOUR = b.comment(
                        "Per-biome background radiation in uSv/h. Format: \"<biome_id>=<value>\".",
                        "Highest priority; overrides level and default.")
                .defineList("biome_usv_per_hour",
                        List.of(
                                "minecraft:nether_wastes=50.0",
                                "nuclearcraftneohaul:wasteland=1500.0",
                                "nuclearcraft:wasteland=1500.0",
                                "createnucleartech:fallout=1500.0",
                                "minecraft:deep_dark=70.0"
                        ),
                        () -> "minecraft:plains=0.1",
                        o -> o instanceof String && ((String) o).contains("="));
        b.pop();

        b.push("debug");
        DEBUG_RADIATION_VECTORS = b.comment("Sync chunk radiation vector at player to client for in-world arrow rendering")
                .define("debug_radiation_vectors", false);
        b.pop();

        b.push("world_sources");
        ACTIVITY_FLOOR_BQ = b.defineInRange("activity_floor_bq", 1.0, 0.0, 1.0e30);
        WORLD_SOURCE_MIN_BQ = b.comment("Minimum activity (Bq) for an item/block/fluid to register as a world radiation source. Below this, the source is ignored by the in-world simulation (block placement, item entities on ground, container contents). Inventory exposure still counts all items regardless of this threshold. Default 1.0e6 (1 MBq).")
                .defineInRange("world_source_min_bq", 5.0e9, 0.0, 1.0e30);
        CONTAMINATION_SPREAD_FACTOR = b.defineInRange("contamination_spread_factor", 0.001, 0.0, 1.0);
        STATIC_HALF_LIFE_YEARS = b.comment("Isotopes with effective half-life (after isotopeDecayMultiplier) above this many IRL years are treated as static: no decay math, no daughter ingrowth, no expiry. Activity Bq is still computed and shown. Default 1000 yr captures U-238/U-235/Pu-239.")
                .defineInRange("static_half_life_years", 1000.0, 0.0, 1.0e15);
        b.pop();

        b.push("block_irradiation");
        BLOCK_IRRADIATION_ENABLED = b.comment("Strong world sources transform nearby blocks over time (datapack recipe driven)")
                .define("enabled", true);
        BLOCK_IRRADIATION_INTERVAL_TICKS = b.comment("Ticks between irradiation sampling passes")
                .defineInRange("interval_ticks", 200, 1, 24000);
        BLOCK_IRRADIATION_MIN_SOURCE_BQ = b.comment("Source activity (Bq) floor to be considered for block irradiation (cheap pre-gate)")
                .defineInRange("min_source_bq", 5.0e9, 0.0, 1.0e30);
        BLOCK_IRRADIATION_RADIUS = b.comment("Max ray length (blocks) cast from each source")
                .defineInRange("radius", 8, 1, 128);
        BLOCK_IRRADIATION_RAYS = b.comment("Random rays cast per source per interval; each ray irradiates the first solid block it hits (occlusion-aware)")
                .defineInRange("rays_per_source", 10, 1, 1000);
        BLOCK_IRRADIATION_MAX_SOURCES = b.comment("Cap on sources sampled per interval to prevent oversized jobs")
                .defineInRange("max_sources_per_job", 64, 1, 200);
        BLOCK_IRRADIATION_MAX_TRANSFORMS = b.comment("Safety cap on setBlock calls per drain")
                .defineInRange("max_transforms_per_drain", 10, 1, 1000);
        b.pop();

        b.push("gas");
        GAS_SOURCE_THRESHOLD_BQ = b.comment("Minimum source activity (Bq) to emit a radioactive gas cloud (radon/xenon). Default 2.0e11 = 200 GBq.")
                .defineInRange("gas_source_threshold_bq", 2.0e11, 0.0, 1.0e30);
        GAS_BASE_RADIUS = b.comment("Base gas cloud radius in blocks at the threshold activity.")
                .defineInRange("gas_base_radius_blocks", 8, 1, 256);
        GAS_RADIUS_STEP_BQ = b.comment("Extra activity (Bq) above threshold that grows the cloud by 1 block. Default 2.0e11 = +1 block per 200 GBq.")
                .defineInRange("gas_radius_step_bq", 2.0e11, 1.0, 1.0e30);
        GAS_MAX_RADIUS = b.comment("Hard cap on gas cloud radius (blocks).")
                .defineInRange("gas_max_radius_blocks", 48, 1, 256);
        GAS_INHALE_CHANCE = b.comment("Per entity-sim-interval chance a player inside a gas cloud inhales (no head gas protection).")
                .defineInRange("gas_inhale_chance", 0.15, 0.0, 1.0);
        GAS_POLLUTION_PER_INHALE = b.comment("Lung pollution (0..1) added per inhale.")
                .defineInRange("gas_pollution_per_inhale", 0.03, 0.0, 1.0);
        LUNG_DUST_POLLUTION_PER_INTERVAL = b.comment("Lung pollution (0..1) added per entity-sim-interval when airborne-contaminant items (tag nr:airborne_contaminant) are carried without head gas protection.")
                .defineInRange("lung_dust_pollution_per_interval", 0.01, 0.0, 1.0);
        LUNG_RECOVERY_PER_INTERVAL = b.comment("Passive lung pollution (0..1) cleared per entity-sim-interval. Scaled up by an active radiation_purge effect.")
                .defineInRange("lung_recovery_per_interval", 0.004, 0.0, 1.0);
        LUNG_MID_THRESHOLD = b.comment("Pollution (0..1) at/above which lungs reach MID stage (applies Weakness).")
                .defineInRange("lung_mid_threshold", 0.34, 0.0, 1.0);
        LUNG_HIGH_THRESHOLD = b.comment("Pollution (0..1) at/above which lungs reach HIGH stage (lung cancer: Weakness II + damage).")
                .defineInRange("lung_high_threshold", 0.75, 0.0, 1.0);
        LUNG_CANCER_DAMAGE = b.comment("Damage dealt per damage interval at HIGH lung stage.")
                .defineInRange("lung_cancer_damage", 2.0, 0.0, 1000.0);
        LUNG_CANCER_DAMAGE_INTERVAL_TICKS = b.comment("Ticks between lung cancer damage applications at HIGH stage.")
                .defineInRange("lung_cancer_damage_interval_ticks", 40, 1, 24000);
        b.pop();

        SPEC = b.build();
    }

    private RadiationConfig() {}

    public static Double levelBackgroundUSvPerHour(ResourceLocation dim) {
        if (dim == null) return null;
        Map<ResourceLocation, Double> map = levelBackgroundCache;
        if (map == null) {
            map = parse(LEVEL_BACKGROUND_USV_PER_HOUR.get());
            levelBackgroundCache = map;
        }
        return map.get(dim);
    }

    public static Double biomeBackgroundUSvPerHour(ResourceLocation biome) {
        if (biome == null) return null;
        Map<ResourceLocation, Double> map = biomeBackgroundCache;
        if (map == null) {
            map = parse(BIOME_BACKGROUND_USV_PER_HOUR.get());
            biomeBackgroundCache = map;
        }
        return map.get(biome);
    }

    public static boolean isMenuIgnored(ResourceLocation id) {
        if (id == null) return false;
        Set<ResourceLocation> set = ignoredMenusCache;
        if (set == null) {
            set = parseSet(IGNORED_MENUS.get());
            ignoredMenusCache = set;
        }
        return set.contains(id);
    }

    public static void invalidateBackgroundCaches() {
        levelBackgroundCache = null;
        biomeBackgroundCache = null;
        ignoredMenusCache = null;
    }

    private static Map<ResourceLocation, Double> parse(List<? extends String> entries) {
        Map<ResourceLocation, Double> out = new HashMap<>();
        if (entries == null) return out;
        for (String s : entries) {
            int eq = s.indexOf('=');
            if (eq <= 0 || eq >= s.length() - 1) continue;
            ResourceLocation key = ResourceLocation.tryParse(s.substring(0, eq).trim());
            if (key == null) continue;
            try {
                out.put(key, Double.parseDouble(s.substring(eq + 1).trim()));
            } catch (NumberFormatException ignored) {}
        }
        return out;
    }

    private static Set<ResourceLocation> parseSet(List<? extends String> entries) {
        Set<ResourceLocation> out = new HashSet<>();
        if (entries == null) return out;
        for (String s : entries) {
            ResourceLocation key = ResourceLocation.tryParse(s.trim());
            if (key != null) out.add(key);
        }
        return Collections.unmodifiableSet(out);
    }
}