package igentuman.nr.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.KubeStartupEvent;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.binding.Bindings;
import igentuman.nr.builder.RadiationBindingBuilder;
import igentuman.nr.integration.kubejs.NuclearRadiationKubeData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code NRStartupEvents.bindings} — attach radioactive isotope profiles to items, blocks and
 * fluids (or their tags), and remove existing bindings.
 * <p>Atom counts are the number of atoms present; activity (Bq) is derived from atoms and the
 * isotope's half-life at runtime.
 */
public class BindingKubeEvent implements KubeStartupEvent {

    private enum Kind { ITEM, BLOCK, FLUID, ITEM_TAG, BLOCK_TAG, FLUID_TAG }

    public Target item(String id) { return new Target(Kind.ITEM, id); }
    public Target block(String id) { return new Target(Kind.BLOCK, id); }
    public Target fluid(String id) { return new Target(Kind.FLUID, id); }
    public Target itemTag(String id) { return new Target(Kind.ITEM_TAG, id); }
    public Target blockTag(String id) { return new Target(Kind.BLOCK_TAG, id); }
    public Target fluidTag(String id) { return new Target(Kind.FLUID_TAG, id); }

    public void removeItem(String id) {
        NuclearRadiationKubeData.BINDING_OPS.add(() -> Bindings.removeItem(rl(id)));
    }
    public void removeBlock(String id) {
        NuclearRadiationKubeData.BINDING_OPS.add(() -> Bindings.removeBlock(rl(id)));
    }
    public void removeFluid(String id) {
        NuclearRadiationKubeData.BINDING_OPS.add(() -> Bindings.removeFluid(rl(id)));
    }
    public void removeItemTag(String id) {
        NuclearRadiationKubeData.BINDING_OPS.add(() -> Bindings.removeItemTag(TagKey.create(Registries.ITEM, rl(strip(id)))));
    }
    public void removeBlockTag(String id) {
        NuclearRadiationKubeData.BINDING_OPS.add(() -> Bindings.removeBlockTag(TagKey.create(Registries.BLOCK, rl(strip(id)))));
    }
    public void removeFluidTag(String id) {
        NuclearRadiationKubeData.BINDING_OPS.add(() -> Bindings.removeFluidTag(TagKey.create(Registries.FLUID, rl(strip(id)))));
    }

    private static ResourceLocation rl(String id) {
        return ResourceLocation.parse(id);
    }

    private static String strip(String id) {
        return id.startsWith("#") ? id.substring(1) : id;
    }

    public static final class Target {
        private final Kind kind;
        private final String id;
        private final Map<String, Double> atoms = new LinkedHashMap<>();

        Target(Kind kind, String id) {
            this.kind = kind;
            this.id = id;
            // Register lazily at reload time; the atoms map keeps mutating as the script chains .isotope(...).
            NuclearRadiationKubeData.BINDING_OPS.add(this::register);
        }

        /** Adds atoms of an isotope (e.g. {@code "nr:co_60"}) to this binding's profile. */
        public Target isotope(String isotopeId, double atomsCount) {
            atoms.merge(isotopeId, atomsCount, Double::sum);
            return this;
        }

        private void register() {
            if (atoms.isEmpty()) {
                NuclearRadiation.LOGGER.warn("KubeJS radiation binding for {} has no isotopes; skipping", id);
                return;
            }
            RadiationBindingBuilder b = RadiationBindingBuilder.create();
            switch (kind) {
                case ITEM -> b.item(rl(id));
                case BLOCK -> b.block(rl(id));
                case FLUID -> b.fluid(rl(id));
                case ITEM_TAG -> b.itemTag(TagKey.create(Registries.ITEM, rl(strip(id))));
                case BLOCK_TAG -> b.blockTag(TagKey.create(Registries.BLOCK, rl(strip(id))));
                case FLUID_TAG -> b.fluidTag(TagKey.create(Registries.FLUID, rl(strip(id))));
            }
            for (Map.Entry<String, Double> e : atoms.entrySet()) {
                b.isotope(e.getKey(), e.getValue());
            }
            b.register();
        }
    }
}
