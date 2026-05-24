package igentuman.nr.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class GeneralConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue ISOTOPE_DECAY_MULTIPLIER = BUILDER
            .comment("Multiplier applied to radiation source isotope decay rate. 1.0 = vanilla half-life; >1.0 decays faster; <1.0 decays slower.")
            .defineInRange("isotopeDecayMultiplier", 1.0, 0.0, 1_000_000.0);

    public static final ModConfigSpec.DoubleValue ENTITY_DECAY_MULTIPLIER = BUILDER
            .comment("Multiplier applied to how fast entities (player, cow, etc.) clean accumulated radiation dose. 1.0 = default; >1.0 recovers faster; <1.0 recovers slower.")
            .defineInRange("entityDecayMultiplier", 1.0, 0.0, 1_000_000.0);

    public static final ModConfigSpec.BooleanValue RADIATION_HARM_EFFECTS_PLAYERS = BUILDER
            .comment("If true, radiation exposure applies mob effects and damage (confusion, weakness, blindness, magic damage) to players. If false, simulation still runs and dose still accumulates, but no harmful effects are applied to players.")
            .define("radiationHarmEffectsPlayers", true);

    public static final ModConfigSpec.BooleanValue RADIATION_HARM_EFFECTS_MOBS = BUILDER
            .comment("If true, radiation exposure applies mob effects and damage to non-player living entities (mobs, animals). If false, simulation still runs and dose still accumulates, but no harmful effects are applied to those entities.")
            .define("radiationHarmEffectsMobs", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

}
