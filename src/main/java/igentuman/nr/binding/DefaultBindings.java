package igentuman.nr.binding;

import igentuman.nr.builder.RadiationBindingBuilder;
import igentuman.nr.registry.Isotopes;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DefaultBindings {
    private DefaultBindings() {}

    public static List<BindingDefinition> defaults() {
        List<BindingDefinition> list = new ArrayList<>();

        list.add(BindingDefinition.builder("uranium_ore_item")
                .itemTag(RadiationTags.ITEM_URANIUM_ORE)
                .isotope(Isotopes.U_238, 9.72e21)
                .isotope(Isotopes.U_235, 7.11e19)
                .build());

        list.add(BindingDefinition.builder("uranium_ore_block")
                .blockTag(RadiationTags.BLOCK_URANIUM_ORE)
                .isotope(Isotopes.U_238, 9.72e21)
                .isotope(Isotopes.U_235, 7.11e19)
                .build());

        list.add(BindingDefinition.builder("uranium_raw_item")
                .itemTag(RadiationTags.ITEM_URANIUM_RAW)
                .isotope(Isotopes.U_238, 2.51e24)
                .isotope(Isotopes.U_235, 1.84e22)
                .build());

        list.add(BindingDefinition.builder("uranium_ingot_item")
                .itemTag(RadiationTags.ITEM_URANIUM_INGOT)
                .isotope(Isotopes.U_238, 4.27e24)
                .isotope(Isotopes.U_235, 3.14e22)
                .build());

        list.add(BindingDefinition.builder("uranium_dust_item")
                .itemTag(RadiationTags.ITEM_URANIUM_DUST)
                .isotope(Isotopes.U_238, 4.27e24)
                .isotope(Isotopes.U_235, 3.14e22)
                .build());

        list.add(BindingDefinition.builder("spent_fuel_item")
                .itemTag(RadiationTags.ITEM_SPENT_FUEL)
                .isotope(Isotopes.U_238, 4.0e24)
                .isotope(Isotopes.PU_239, 2.5e22)
                .isotope(Isotopes.CS_137, 2.6e22)
                .isotope(Isotopes.SR_90, 8.0e21)
                .build());

        list.add(BindingDefinition.builder("radioactive_low_item")
                .itemTag(RadiationTags.ITEM_LOW)
                .isotope(Isotopes.U_238, 2.5e21)
                .build());

        list.add(BindingDefinition.builder("radioactive_medium_item")
                .itemTag(RadiationTags.ITEM_MEDIUM)
                .isotope(Isotopes.CS_137, 4.4e20)
                .build());

        list.add(BindingDefinition.builder("radioactive_high_item")
                .itemTag(RadiationTags.ITEM_HIGH)
                .isotope(Isotopes.CO_60, 1.0e21)
                .isotope(Isotopes.CS_137, 4.4e21)
                .build());

        list.add(BindingDefinition.builder("radioactive_low_block")
                .blockTag(RadiationTags.BLOCK_LOW)
                .isotope(Isotopes.U_238, 2.5e21)
                .build());

        list.add(BindingDefinition.builder("radioactive_medium_block")
                .blockTag(RadiationTags.BLOCK_MEDIUM)
                .isotope(Isotopes.CS_137, 4.4e20)
                .build());

        list.add(BindingDefinition.builder("radioactive_high_block")
                .blockTag(RadiationTags.BLOCK_HIGH)
                .isotope(Isotopes.CO_60, 1.0e21)
                .isotope(Isotopes.CS_137, 4.4e21)
                .build());

        list.add(BindingDefinition.builder("radioactive_fluid")
                .fluidTag(RadiationTags.FLUID_RADIOACTIVE)
                .isotope(Isotopes.CS_137, 4.4e18)
                .build());

        return list;
    }

    public static void registerDefaults() {
        for (BindingDefinition def : defaults()) {
            apply(def);
        }
    }

    @SuppressWarnings("unchecked")
    private static void apply(BindingDefinition def) {
        RadiationBindingBuilder b = RadiationBindingBuilder.create();
        switch (def.type) {
            case BindingDefinition.TYPE_ITEM -> {
                if (def.tag) b.itemTag((TagKey<Item>) TagKey.create(Registries.ITEM, def.target));
                else b.item(def.target);
            }
            case BindingDefinition.TYPE_BLOCK -> {
                if (def.tag) b.blockTag((TagKey<Block>) TagKey.create(Registries.BLOCK, def.target));
                else b.block(def.target);
            }
            case BindingDefinition.TYPE_FLUID -> {
                if (def.tag) b.fluidTag((TagKey<Fluid>) TagKey.create(Registries.FLUID, def.target));
                else b.fluid(def.target);
            }
            default -> throw new IllegalStateException("Unknown binding type: " + def.type);
        }
        for (Map.Entry<String, Double> e : def.isotopes.entrySet()) {
            b.isotope(e.getKey(), e.getValue());
        }
        b.register();
    }
}
