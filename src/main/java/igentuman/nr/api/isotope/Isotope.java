package igentuman.nr.api.isotope;

import igentuman.nr.api.RadiationQuality;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public interface Isotope {
    String id();

    float xRayStrength();

    float alphaStrength();

    float betaStrength();

    float neutronStrength();

    long halfLifeTicks();

    Optional<Isotope> decaysTo();

    RadiationQuality quality();

    final class BindingDefinition {

        public static final String TYPE_ITEM = "item";
        public static final String TYPE_BLOCK = "block";
        public static final String TYPE_FLUID = "fluid";

        public final String fileId;
        public final String type;
        public final ResourceLocation target;
        public final boolean tag;
        public final Map<String, Double> isotopes;

        private BindingDefinition(String fileId, String type, ResourceLocation target, boolean tag, Map<String, Double> isotopes) {
            this.fileId = fileId;
            this.type = type;
            this.target = target;
            this.tag = tag;
            this.isotopes = Collections.unmodifiableMap(isotopes);
        }

        public static Builder builder(String fileId) {
            return new Builder(fileId);
        }

        public static final class Builder {
            private final String fileId;
            private String type;
            private ResourceLocation target;
            private boolean tag;
            private final Map<String, Double> isotopes = new LinkedHashMap<>();

            private Builder(String fileId) {
                this.fileId = fileId;
            }

            public Builder item(ResourceLocation id) {
                this.type = TYPE_ITEM; this.target = id; this.tag = false; return this;
            }

            public Builder itemTag(TagKey<?> t) {
                this.type = TYPE_ITEM; this.target = t.location(); this.tag = true; return this;
            }

            public Builder block(ResourceLocation id) {
                this.type = TYPE_BLOCK; this.target = id; this.tag = false; return this;
            }

            public Builder blockTag(TagKey<?> t) {
                this.type = TYPE_BLOCK; this.target = t.location(); this.tag = true; return this;
            }

            public Builder fluid(ResourceLocation id) {
                this.type = TYPE_FLUID; this.target = id; this.tag = false; return this;
            }

            public Builder fluidTag(TagKey<?> t) {
                this.type = TYPE_FLUID; this.target = t.location(); this.tag = true; return this;
            }

            public Builder isotope(String id, double atoms) {
                isotopes.merge(id, atoms, Double::sum);
                return this;
            }

            public BindingDefinition build() {
                if (type == null || target == null) {
                    throw new IllegalStateException("Binding target not set for " + fileId);
                }
                return new BindingDefinition(fileId, type, target, tag, new LinkedHashMap<>(isotopes));
            }
        }
    }
}
