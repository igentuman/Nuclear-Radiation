package igentuman.nr.binding;

import igentuman.nr.builder.RadiationBindingBuilder;
import igentuman.nr.registry.Isotopes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static igentuman.nr.util.TagUtil.*;

public final class DefaultBindings {
    private DefaultBindings() {}

    public static List<BindingDefinition> defaults() {
        List<BindingDefinition> list = new ArrayList<>();

        list.add(BindingDefinition.builder("fuel_rods/heuo2")
                .itemTag(cTag("fuel_rods/heuo2"))
                .isotope(Isotopes.U_235, 19.72e19)
                .isotope(Isotopes.U_238, 11.72e20)
                .build());

        list.add(BindingDefinition.builder("fuel_rods/leuo2")
                .itemTag(cTag("fuel_rods/leuo2"))
                .isotope(Isotopes.U_235, 1.72e19)
                .isotope(Isotopes.U_238, 11.72e20)
                .build());

        list.add(BindingDefinition.builder("fuel_rods/plutonium")
                .itemTag(cTag("fuel_rods/plutonium"))
                .isotope(Isotopes.PU_238, 21.72e19)
                .isotope(Isotopes.U_238, 11.72e20)
                .build());

        list.add(BindingDefinition.builder("fuel_rods/spent")
                .itemTag(cTag("fuel_rods/spent"))
                .isotope(Isotopes.PU_239, 1.72e19)
                .isotope(Isotopes.AM_241, 0.72e20)
                .build());

        list.add(BindingDefinition.builder("pellet_actinium225")
                .itemTag(pelletTag("actinium225"))
                .isotope(Isotopes.AC_225, 19.72e19)
                .build());

        list.add(BindingDefinition.builder("pellet_polonium")
                .itemTag(pelletTag("polonium"))
                .isotope(Isotopes.PO_210, 16.72e19)
                .build());

        list.add(BindingDefinition.builder("pellet_plutonium")
                .itemTag(pelletTag("plutonium"))
                .isotope(Isotopes.PU_238, 14.72e17)
                .isotope(Isotopes.PU_239, 19.72e17)
                .isotope(Isotopes.PU_241, 9.72e18)
                .isotope(Isotopes.PU_242, 3.72e18)
                .build());

        list.add(BindingDefinition.builder("pellet_uranium235")
                .itemTag(pelletTag("uranium235"))
                .isotope(Isotopes.U_235, 19.72e19)
                .build());

        list.add(BindingDefinition.builder("pellet_uranium238")
                .itemTag(pelletTag("uranium238"))
                .isotope(Isotopes.U_238, 19.72e19)
                .build());

        list.add(BindingDefinition.builder("actinium_225")
                .itemTag(isotopeTag("actinium_225"))
                .isotope(Isotopes.AC_225, 3.37e24)
                .build());

        list.add(BindingDefinition.builder("americium_241")
                .itemTag(isotopeTag("americium_241"))
                .isotope(Isotopes.AM_241, 19.72e19)
                .build());

        list.add(BindingDefinition.builder("americium_242")
                .itemTag(isotopeTag("americium_242"))
                .isotope(Isotopes.AM_242, 4.65e25)
                .build());

        list.add(BindingDefinition.builder("americium_243")
                .itemTag(isotopeTag("americium_243"))
                .isotope(Isotopes.AM_243, 1.15e19)
                .build());

        list.add(BindingDefinition.builder("berkelium_247")
                .itemTag(isotopeTag("berkelium_247"))
                .isotope(Isotopes.BK_247, 6.03e19)
                .build());

        list.add(BindingDefinition.builder("berkelium_248")
                .itemTag(isotopeTag("berkelium_248"))
                .isotope(Isotopes.BK_248, 2.76e20)
                .build());

        list.add(BindingDefinition.builder("beryllium_7")
                .itemTag(isotopeTag("beryllium_7"))
                .isotope(Isotopes.BE_7, 2.02e25)
                .build());

        list.add(BindingDefinition.builder("calcium_48")
                .itemTag(isotopeTag("calcium_48"))
                .isotope(Isotopes.CA_48, 6.7e3)
                .build());

        list.add(BindingDefinition.builder("californium_249")
                .itemTag(isotopeTag("californium_249"))
                .isotope(Isotopes.CF_249, 2.35e20)
                .build());

        list.add(BindingDefinition.builder("californium_250")
                .itemTag(isotopeTag("californium_250"))
                .isotope(Isotopes.CF_250, 6.29e21)
                .build());

        list.add(BindingDefinition.builder("californium_251")
                .itemTag(isotopeTag("californium_251"))
                .isotope(Isotopes.CF_251, 9.12e19)
                .build());

        list.add(BindingDefinition.builder("californium_252")
                .itemTag(isotopeTag("californium_252"))
                .isotope(Isotopes.CF_252, 3.08e22)
                .build());

        list.add(BindingDefinition.builder("cobalt_60")
                .itemTag(isotopeTag("cobalt_60"))
                .isotope(Isotopes.CO_60, 6.50e22)
                .build());

        list.add(BindingDefinition.builder("copernicium_291")
                .itemTag(isotopeTag("copernicium_291"))
                .isotope(Isotopes.CN_291, 7.43e28)
                .build());

        list.add(BindingDefinition.builder("curium_243")
                .itemTag(isotopeTag("curium_243"))
                .isotope(Isotopes.CM_243, 2.91e21)
                .build());

        list.add(BindingDefinition.builder("curium_245")
                .itemTag(isotopeTag("curium_245"))
                .isotope(Isotopes.CM_245, 1.02e19)
                .build());

        list.add(BindingDefinition.builder("curium_246")
                .itemTag(isotopeTag("curium_246"))
                .isotope(Isotopes.CM_246, 1.77e19)
                .build());

        list.add(BindingDefinition.builder("curium_247")
                .itemTag(isotopeTag("curium_247"))
                .isotope(Isotopes.CM_247, 5.34e15)
                .build());

        list.add(BindingDefinition.builder("iridium_192")
                .itemTag(isotopeTag("iridium_192"))
                .isotope(Isotopes.IR_192, 5.30e23)
                .build());

        list.add(BindingDefinition.builder("neptunium_236")
                .itemTag(isotopeTag("neptunium_236"))
                .isotope(Isotopes.NP_236, 5.66e17)
                .build());

        list.add(BindingDefinition.builder("neptunium_237")
                .itemTag(isotopeTag("neptunium_237"))
                .isotope(Isotopes.NP_237, 4.05e16)
                .build());

        list.add(BindingDefinition.builder("plutonium_238")
                .itemTag(isotopeTag("plutonium_238"))
                .isotope(Isotopes.PU_238, 9.85e20)
                .build());

        list.add(BindingDefinition.builder("plutonium_239")
                .itemTag(isotopeTag("plutonium_239"))
                .isotope(Isotopes.PU_239, 3.57e21)
                .build());

        list.add(BindingDefinition.builder("plutonium_241")
                .itemTag(isotopeTag("plutonium_241"))
                .isotope(Isotopes.PU_241, 5.95e21)
                .build());

        list.add(BindingDefinition.builder("plutonium_242")
                .itemTag(isotopeTag("plutonium_242"))
                .isotope(Isotopes.PU_242, 2.27e17)
                .build());

        list.add(BindingDefinition.builder("sodium_22")
                .itemTag(isotopeTag("sodium_22"))
                .isotope(Isotopes.NA_22, 3.59e23)
                .build());

        list.add(BindingDefinition.builder("thorium_230")
                .itemTag(isotopeTag("thorium_230"))
                .isotope(Isotopes.TH_230, 1.19e18)
                .build());

        list.add(BindingDefinition.builder("thorium_232")
                .itemTag(isotopeTag("thorium_232"))
                .isotope(Isotopes.TH_232, 6.31e12)
                .build());

        list.add(BindingDefinition.builder("uranium_233")
                .itemTag(isotopeTag("uranium_233"))
                .isotope(Isotopes.U_233, 5.54e17)
                .build());

        list.add(BindingDefinition.builder("uranium_234")
                .itemTag(isotopeTag("uranium_234"))
                .isotope(Isotopes.U_234, 3.58e17)
                .build());

        list.add(BindingDefinition.builder("uranium_235")
                .itemTag(isotopeTag("uranium_235"))
                .isotope(Isotopes.U_235, 1.24e14)
                .build());

        list.add(BindingDefinition.builder("uranium_238")
                .itemTag(isotopeTag("uranium_238"))
                .isotope(Isotopes.U_238, 1.93e13)
                .build());

        list.add(BindingDefinition.builder("uranium_ore_item")
                .itemTag(oreTag("uranium"))
                .isotope(Isotopes.U_238, 9.72e21)
                .isotope(Isotopes.U_235, 7.11e19)
                .build());

        list.add(BindingDefinition.builder("uranium_raw_item")
                .itemTag(rawTag("uranium"))
                .isotope(Isotopes.U_238, 2.51e24)
                .isotope(Isotopes.U_235, 1.84e22)
                .build());

        list.add(BindingDefinition.builder("uranium_ingot_item")
                .itemTag(ingotTag("uranium"))
                .isotope(Isotopes.U_238, 4.27e24)
                .isotope(Isotopes.U_235, 3.14e22)
                .build());

        list.add(BindingDefinition.builder("uranium_dust_item")
                .itemTag(dustTag("uranium"))
                .isotope(Isotopes.U_238, 4.27e24)
                .isotope(Isotopes.U_235, 3.14e22)
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

        // NuclearCraft: Neohaul uses c:<prefix>/<material> tags. NR's existing c:ores/ingots/dusts
        // uranium bindings already cover NCN uranium; below fills the raw + isotope/fuel gaps.
        list.add(BindingDefinition.builder("ncn_raw_uranium")
                .itemTag(cTag("raw_materials/uranium"))
                .isotope(Isotopes.U_238, 2.51e24)
                .isotope(Isotopes.U_235, 1.84e22)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_thorium")
                .itemTag(cTag("ingots/thorium"))
                .isotope(Isotopes.TH_232, 6.31e12)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_plutonium")
                .itemTag(cTag("ingots/plutonium"))
                .isotope(Isotopes.PU_239, 3.57e21)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_uranium_233")
                .itemTag(cTag("ingots/uranium_233"))
                .isotope(Isotopes.U_233, 5.54e17)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_uranium_235")
                .itemTag(cTag("ingots/uranium_235"))
                .isotope(Isotopes.U_235, 1.24e14)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_uranium_238")
                .itemTag(cTag("ingots/uranium_238"))
                .isotope(Isotopes.U_238, 1.93e13)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_neptunium_237")
                .itemTag(cTag("ingots/neptunium_237"))
                .isotope(Isotopes.NP_237, 4.05e16)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_plutonium_238")
                .itemTag(cTag("ingots/plutonium_238"))
                .isotope(Isotopes.PU_238, 9.85e20)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_plutonium_239")
                .itemTag(cTag("ingots/plutonium_239"))
                .isotope(Isotopes.PU_239, 3.57e21)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_plutonium_241")
                .itemTag(cTag("ingots/plutonium_241"))
                .isotope(Isotopes.PU_241, 5.95e21)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_plutonium_242")
                .itemTag(cTag("ingots/plutonium_242"))
                .isotope(Isotopes.PU_242, 2.27e17)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_americium_241")
                .itemTag(cTag("ingots/americium_241"))
                .isotope(Isotopes.AM_241, 19.72e19)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_americium_243")
                .itemTag(cTag("ingots/americium_243"))
                .isotope(Isotopes.AM_243, 1.15e19)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_curium_245")
                .itemTag(cTag("ingots/curium_245"))
                .isotope(Isotopes.CM_245, 1.02e19)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_californium_252")
                .itemTag(cTag("ingots/californium_252"))
                .isotope(Isotopes.CF_252, 3.08e22)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_cesium_137")
                .itemTag(cTag("ingots/cesium_137"))
                .isotope(Isotopes.CS_137, 4.4e20)
                .build());

        list.add(BindingDefinition.builder("ncn_ingot_strontium_90")
                .itemTag(cTag("ingots/strontium_90"))
                .isotope(Isotopes.SR_90, 3.0e20)
                .build());

        return list;
    }

    public static ResourceLocation rec(String s) {
        return  ResourceLocation.tryParse(s);
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
