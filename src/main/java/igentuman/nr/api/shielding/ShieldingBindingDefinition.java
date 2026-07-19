package igentuman.nr.api.shielding;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public final class ShieldingBindingDefinition {

    public enum Kind { BLOCK_BINDING, TIER_PRESET }

    public final String fileId;
    public final Kind kind;
    /** Block id or tag location for BLOCK_BINDING; null for TIER_PRESET. */
    @Nullable public final ResourceLocation target;
    /** Whether {@link #target} is a tag (BLOCK_BINDING only). */
    public final boolean tag;
    /** Referenced tier (BLOCK_BINDING tier-ref) or the tier being defined (TIER_PRESET). Null for raw block binding. */
    @Nullable public final ShieldingTier tier;
    /** Raw override coeffs (BLOCK_BINDING) or preset values (TIER_PRESET). Null for pure tier-ref binding. */
    @Nullable public final Double xray;
    @Nullable public final Double neutron;

    private ShieldingBindingDefinition(String fileId, Kind kind, @Nullable ResourceLocation target,
                                       boolean tag, @Nullable ShieldingTier tier,
                                       @Nullable Double xray, @Nullable Double neutron) {
        this.fileId = fileId;
        this.kind = kind;
        this.target = target;
        this.tag = tag;
        this.tier = tier;
        this.xray = xray;
        this.neutron = neutron;
    }

    /** Bind a block/tag to a tier preset. */
    public static ShieldingBindingDefinition tierBinding(String fileId, ResourceLocation target, boolean tag, ShieldingTier tier) {
        return new ShieldingBindingDefinition(fileId, Kind.BLOCK_BINDING, target, tag, tier, null, null);
    }

    /** Bind a block/tag to explicit raw coefficients (bypasses tier preset). */
    public static ShieldingBindingDefinition rawBinding(String fileId, ResourceLocation target, boolean tag, double xray, double neutron) {
        return new ShieldingBindingDefinition(fileId, Kind.BLOCK_BINDING, target, tag, null, xray, neutron);
    }

    /** Define/override the coefficients of a tier preset. */
    public static ShieldingBindingDefinition preset(String fileId, ShieldingTier tier, double xray, double neutron) {
        return new ShieldingBindingDefinition(fileId, Kind.TIER_PRESET, null, false, tier, xray, neutron);
    }
}
