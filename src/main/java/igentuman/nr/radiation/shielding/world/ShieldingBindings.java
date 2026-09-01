package igentuman.nr.radiation.shielding.world;

import igentuman.nr.api.shielding.ShieldingTier;
import igentuman.nr.radiation.shielding.world.ShieldingRegistry.Coeffs;
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

    private static final Object LOCK = new Object();

    private static final Map<ResourceLocation, ShieldEntry> BLOCKS = new LinkedHashMap<>();
    private static final Map<TagKey<Block>, ShieldEntry> BLOCK_TAGS = new LinkedHashMap<>();
    private static final EnumMap<ShieldingTier, Coeffs> TIER_PRESETS = new EnumMap<>(ShieldingTier.class);

    static {
        seedPresets();
    }

    public static Map<ResourceLocation, ShieldEntry> blocks() { synchronized (LOCK) { return new LinkedHashMap<>(BLOCKS); } }
    public static Map<TagKey<Block>, ShieldEntry> blockTags() { synchronized (LOCK) { return new LinkedHashMap<>(BLOCK_TAGS); } }
    public static Map<ShieldingTier, Coeffs> tierPresets() { synchronized (LOCK) { return new EnumMap<>(TIER_PRESETS); } }

    private ShieldingBindings() {}

    private static void seedPresets() {
        for (ShieldingTier t : ShieldingTier.values()) {
            TIER_PRESETS.put(t, t.defaultCoeffs());
        }
    }

    public static void setTierPreset(ShieldingTier tier, Coeffs coeffs) {
        synchronized (LOCK) { TIER_PRESETS.put(tier, coeffs); }
    }

    public static Coeffs tierPreset(ShieldingTier tier) {
        synchronized (LOCK) {
            Coeffs c = TIER_PRESETS.get(tier);
            return c != null ? c : tier.defaultCoeffs();
        }
    }

    public static void putBlock(ResourceLocation id, ShieldEntry entry) {
        synchronized (LOCK) { BLOCKS.put(id, entry); }
    }

    public static void putBlockTag(TagKey<Block> tag, ShieldEntry entry) {
        synchronized (LOCK) { BLOCK_TAGS.put(tag, entry); }
    }

    public static void removeBlock(ResourceLocation id) {
        synchronized (LOCK) { BLOCKS.remove(id); }
    }

    public static void removeBlockTag(TagKey<Block> tag) {
        synchronized (LOCK) { BLOCK_TAGS.remove(tag); }
    }

    public static void clear() {
        synchronized (LOCK) {
            BLOCKS.clear();
            BLOCK_TAGS.clear();
            TIER_PRESETS.clear();
            seedPresets();
        }
    }

    /** Resolve attenuation coefficients for a block state. Direct block id binding wins over tag bindings. */
    @Nullable
    public static Coeffs resolve(BlockState state) {
        Block block = state.getBlock();
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        ShieldEntry direct;
        Map<TagKey<Block>, ShieldEntry> tagsSnapshot;
        synchronized (LOCK) {
            direct = BLOCKS.get(id);
            if (direct == null) {
                tagsSnapshot = new LinkedHashMap<>(BLOCK_TAGS);
            } else {
                tagsSnapshot = null;
            }
        }
        if (direct != null) return direct.resolve();

        for (Map.Entry<TagKey<Block>, ShieldEntry> e : tagsSnapshot.entrySet()) {
            if (state.is(e.getKey())) return e.getValue().resolve();
        }
        return null;
    }
}
