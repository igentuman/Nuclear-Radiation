package igentuman.nr.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.KubeStartupEvent;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.integration.kubejs.NuclearRadiationKubeData;
import igentuman.nr.shielding.ShieldingBindings;
import igentuman.nr.shielding.ShieldingBindings.ShieldEntry;
import igentuman.nr.shielding.ShieldingRegistry.Coeffs;
import igentuman.nr.shielding.ShieldingTier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * {@code NRStartupEvents.shielding} — define how blocks attenuate radiation. Bind a block or block
 * tag to a tier preset ({@code "light"}, {@code "mid"}, {@code "heavy"}) or to raw per-meter
 * (x-ray, neutron) coefficients, retune a tier preset, or remove a binding.
 */
public class ShieldingKubeEvent implements KubeStartupEvent {

    public Entry block(String id) { return new Entry(false, id); }
    public Entry blockTag(String id) { return new Entry(true, id); }

    /** Retunes a tier preset's raw (x-ray, neutron) coefficients. */
    public void tierPreset(String tier, double xray, double neutron) {
        NuclearRadiationKubeData.SHIELDING_OPS.add(() -> {
            ShieldingTier t = ShieldingTier.byId(tier);
            if (t == null) {
                NuclearRadiation.LOGGER.warn("KubeJS shielding: unknown tier '{}'", tier);
                return;
            }
            ShieldingBindings.setTierPreset(t, new Coeffs(xray, neutron));
        });
    }

    public void remove(String id) {
        NuclearRadiationKubeData.SHIELDING_OPS.add(() -> ShieldingBindings.removeBlock(rl(id)));
    }

    public void removeTag(String id) {
        NuclearRadiationKubeData.SHIELDING_OPS.add(() ->
                ShieldingBindings.removeBlockTag(TagKey.create(Registries.BLOCK, rl(strip(id)))));
    }

    private static ResourceLocation rl(String id) {
        return ResourceLocation.parse(id);
    }

    private static String strip(String id) {
        return id.startsWith("#") ? id.substring(1) : id;
    }

    public static final class Entry {
        private final boolean isTag;
        private final String id;

        Entry(boolean isTag, String id) {
            this.isTag = isTag;
            this.id = id;
        }

        /** Bind to a tier preset: {@code "light"}, {@code "mid"} or {@code "heavy"}. */
        public void tier(String tier) {
            NuclearRadiationKubeData.SHIELDING_OPS.add(() -> {
                ShieldingTier t = ShieldingTier.byId(tier);
                if (t == null) {
                    NuclearRadiation.LOGGER.warn("KubeJS shielding: unknown tier '{}' for {}", tier, id);
                    return;
                }
                put(ShieldEntry.ofTier(t));
            });
        }

        /** Bind to raw per-meter (x-ray, neutron) attenuation coefficients. */
        public void raw(double xray, double neutron) {
            NuclearRadiationKubeData.SHIELDING_OPS.add(() -> put(ShieldEntry.ofRaw(new Coeffs(xray, neutron))));
        }

        private void put(ShieldEntry entry) {
            if (isTag) {
                ShieldingBindings.putBlockTag(TagKey.create(Registries.BLOCK, rl(strip(id))), entry);
            } else {
                ShieldingBindings.putBlock(rl(id), entry);
            }
        }
    }
}
