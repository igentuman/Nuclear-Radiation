package igentuman.nr.shielding;

import igentuman.nr.shielding.ShieldingRegistry.Coeffs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ShieldingBindings {

    /** A binding payload: either a reference to a tier preset, or explicit raw coefficients. */
    public record ShieldEntry(@Nullable ShieldingTier tier, @Nullable Coeffs raw) {
        public static ShieldEntry ofTier(ShieldingTier tier) {
            return new ShieldEntry(tier, null);
        }
        public static ShieldEntry ofRaw(Coeffs raw) {
            return new ShieldEntry(null, raw);
        }
        public Coeffs resolve() {
            if (raw != null) return raw;
            return tierPreset(tier);
        }
    }

    private static final Map<ResourceLocation, ShieldEntry> BLOCKS = new LinkedHashMap<>();
    private static final Map<TagKey<Block>, ShieldEntry> BLOCK_TAGS = new LinkedHashMap<>();
    private static final EnumMap<ShieldingTier, Coeffs> TIER_PRESETS = new EnumMap<>(ShieldingTier.class);

    static {
        seedPresets();
    }

    private ShieldingBindings() {}

    private static void seedPresets() {
        for (ShieldingTier t : ShieldingTier.values()) {
            TIER_PRESETS.put(t, t.defaultCoeffs());
        }
    }

    public static void setTierPreset(ShieldingTier tier, Coeffs coeffs) {
        TIER_PRESETS.put(tier, coeffs);
    }

    public static Coeffs tierPreset(ShieldingTier tier) {
        Coeffs c = TIER_PRESETS.get(tier);
        return c != null ? c : tier.defaultCoeffs();
    }

    public static void putBlock(ResourceLocation id, ShieldEntry entry) {
        BLOCKS.put(id, entry);
    }

    public static void putBlockTag(TagKey<Block> tag, ShieldEntry entry) {
        BLOCK_TAGS.put(tag, entry);
    }

    public static void clear() {
        BLOCKS.clear();
        BLOCK_TAGS.clear();
        TIER_PRESETS.clear();
        seedPresets();
    }

    /** Resolve attenuation coefficients for a block state. Direct block id binding wins over tag bindings. */
    @Nullable
    public static Coeffs resolve(BlockState state) {
        Block block = state.getBlock();
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        ShieldEntry direct = BLOCKS.get(id);
        if (direct != null) return direct.resolve();

        for (Map.Entry<TagKey<Block>, ShieldEntry> e : BLOCK_TAGS.entrySet()) {
            if (state.is(e.getKey())) return e.getValue().resolve();
        }
        return null;
    }
}
