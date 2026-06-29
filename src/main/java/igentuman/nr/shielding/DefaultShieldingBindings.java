package igentuman.nr.shielding;

import igentuman.nr.binding.RadiationTags;
import igentuman.nr.shielding.ShieldingBindings.ShieldEntry;
import igentuman.nr.shielding.ShieldingRegistry.Coeffs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class DefaultShieldingBindings {
    private DefaultShieldingBindings() {}

    public static List<ShieldingBindingDefinition> defaults() {
        List<ShieldingBindingDefinition> list = new ArrayList<>();

        // Tier presets (datapack-tunable). Values derived from previous hard-coded coefficients.
        list.add(ShieldingBindingDefinition.preset("tier_light", ShieldingTier.LIGHT,
                ShieldingTier.LIGHT.defaultXray, ShieldingTier.LIGHT.defaultNeutron));
        list.add(ShieldingBindingDefinition.preset("tier_mid", ShieldingTier.MID,
                ShieldingTier.MID.defaultXray, ShieldingTier.MID.defaultNeutron));
        list.add(ShieldingBindingDefinition.preset("tier_heavy", ShieldingTier.HEAVY,
                ShieldingTier.HEAVY.defaultXray, ShieldingTier.HEAVY.defaultNeutron));

        // Tier tag bindings: blocks in these tags inherit the tier preset.
        list.add(ShieldingBindingDefinition.tierBinding("shielding_light",
                RadiationTags.BLOCK_SHIELD_LIGHT.location(), true, ShieldingTier.LIGHT));
        list.add(ShieldingBindingDefinition.tierBinding("shielding_mid",
                RadiationTags.BLOCK_SHIELD_MID.location(), true, ShieldingTier.MID));
        list.add(ShieldingBindingDefinition.tierBinding("shielding_heavy",
                RadiationTags.BLOCK_SHIELD_HEAVY.location(), true, ShieldingTier.HEAVY));

        return list;
    }

    public static void registerDefaults() {
        for (ShieldingBindingDefinition def : defaults()) {
            apply(def);
        }
    }

    @SuppressWarnings("unchecked")
    public static void apply(ShieldingBindingDefinition def) {
        switch (def.kind) {
            case TIER_PRESET -> ShieldingBindings.setTierPreset(def.tier,
                    new Coeffs(def.xray, def.neutron));
            case BLOCK_BINDING -> {
                ShieldEntry entry = def.tier != null
                        ? ShieldEntry.ofTier(def.tier)
                        : ShieldEntry.ofRaw(new Coeffs(def.xray, def.neutron));
                if (def.tag) {
                    ShieldingBindings.putBlockTag((TagKey<Block>) TagKey.create(Registries.BLOCK, def.target), entry);
                } else {
                    ShieldingBindings.putBlock(def.target, entry);
                }
            }
        }
    }
}
