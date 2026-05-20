package igentuman.nr.builder;

import igentuman.nr.binding.Bindings;
import igentuman.nr.api.RadiationProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RadiationBindingBuilder {

    private enum TargetKind { ITEM, ITEM_TAG, BLOCK, BLOCK_TAG, FLUID, FLUID_TAG }

    private TargetKind kind;
    private ResourceLocation idTarget;
    private TagKey<?> tagTarget;
    private final Map<String, Double> atoms = new LinkedHashMap<>();

    public static RadiationBindingBuilder create() { return new RadiationBindingBuilder(); }

    public RadiationBindingBuilder item(ResourceLocation id) {
        kind = TargetKind.ITEM; idTarget = id; return this;
    }
    public RadiationBindingBuilder itemTag(TagKey<Item> tag) {
        kind = TargetKind.ITEM_TAG; tagTarget = tag; return this;
    }
    public RadiationBindingBuilder block(ResourceLocation id) {
        kind = TargetKind.BLOCK; idTarget = id; return this;
    }
    public RadiationBindingBuilder blockTag(TagKey<Block> tag) {
        kind = TargetKind.BLOCK_TAG; tagTarget = tag; return this;
    }
    public RadiationBindingBuilder fluid(ResourceLocation id) {
        kind = TargetKind.FLUID; idTarget = id; return this;
    }
    public RadiationBindingBuilder fluidTag(TagKey<Fluid> tag) {
        kind = TargetKind.FLUID_TAG; tagTarget = tag; return this;
    }

    public RadiationBindingBuilder isotope(String id, double atomsCount) {
        atoms.merge(id, atomsCount, Double::sum);
        return this;
    }

    @SuppressWarnings("unchecked")
    public void register() {
        if (kind == null) throw new IllegalStateException("Binding target not set");
        Supplier<RadiationProfile> supplier = () -> {
            RadiationProfileBuilder b = RadiationProfileBuilder.create();
            for (Map.Entry<String, Double> e : atoms.entrySet()) b.isotope(e.getKey(), e.getValue());
            return b.build();
        };
        switch (kind) {
            case ITEM      -> Bindings.putItem(idTarget, supplier);
            case BLOCK     -> Bindings.putBlock(idTarget, supplier);
            case FLUID     -> Bindings.putFluid(idTarget, supplier);
            case ITEM_TAG  -> Bindings.putItemTag((TagKey<Item>) tagTarget, supplier);
            case BLOCK_TAG -> Bindings.putBlockTag((TagKey<Block>) tagTarget, supplier);
            case FLUID_TAG -> Bindings.putFluidTag((TagKey<Fluid>) tagTarget, supplier);
        }
    }
}
