package igentuman.nr.datagen;

import com.google.gson.JsonObject;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.binding.BindingDefinition;
import igentuman.nr.binding.RadiationBindingsReloadListener;
import igentuman.nr.binding.RadiationTags;
import igentuman.nr.builder.RadiationBindingBuilder;
import igentuman.nr.registry.Isotopes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static igentuman.nr.util.TagUtil.*;
import static igentuman.nr.util.TagUtil.dustTag;
import static igentuman.nr.util.TagUtil.ingotTag;
import static igentuman.nr.util.TagUtil.oreTag;
import static igentuman.nr.util.TagUtil.pelletTag;
import static igentuman.nr.util.TagUtil.rawTag;

public class RadiationBindingProvider implements DataProvider {

    private final PackOutput output;
    private final List<BindingDefinition> bindings;

    public RadiationBindingProvider(PackOutput output) {
        this(output, defaults());
    }

    public RadiationBindingProvider(PackOutput output, List<BindingDefinition> bindings) {
        this.output = output;
        this.bindings = bindings;
        this.bindings.addAll(getAdditionalBindings());
    }

    public List<BindingDefinition> getAdditionalBindings() {
        List<BindingDefinition> list = new ArrayList<>();

        // NuclearCraft: Neohaul c:isotopes/<element>/<mass> grouped tags.
        // Atoms mirror the flat c:isotopes/<element>_<mass> bindings in DefaultBindings.
        // boron/10, boron/11, lithium/6, lithium/7 are stable -> no radiation binding.
        list.add(isotopeBinding("americium/241", Isotopes.AM_241, 19.72e19));
        list.add(isotopeBinding("americium/242", Isotopes.AM_242, 4.65e14));
        list.add(isotopeBinding("americium/243", Isotopes.AM_243, 9.15e20));

        list.add(isotopeBinding("berkelium/247", Isotopes.BK_247, 6.03e19));
        list.add(isotopeBinding("berkelium/248", Isotopes.BK_248, 2.76e20));

        list.add(isotopeBinding("californium/249", Isotopes.CF_249, 2.35e21));
        list.add(isotopeBinding("californium/250", Isotopes.CF_250, 6.29e21));
        list.add(isotopeBinding("californium/251", Isotopes.CF_251, 9.12e20));
        list.add(isotopeBinding("californium/252", Isotopes.CF_252, 3.08e21));

        list.add(isotopeBinding("curium/243", Isotopes.CM_243, 2.91e20));
        list.add(isotopeBinding("curium/245", Isotopes.CM_245, 1.02e20));
        list.add(isotopeBinding("curium/246", Isotopes.CM_246, 1.77e21));
        list.add(isotopeBinding("curium/247", Isotopes.CM_247, 5.34e24));

        list.add(isotopeBinding("neptunium/236", Isotopes.NP_236, 5.66e20));
        list.add(isotopeBinding("neptunium/237", Isotopes.NP_237, 4.05e21));

        list.add(isotopeBinding("plutonium/238", Isotopes.PU_238, 1.85e18));
        list.add(isotopeBinding("plutonium/239", Isotopes.PU_239, 3.57e20));
        list.add(isotopeBinding("plutonium/241", Isotopes.PU_241, 5.95e18));
        list.add(isotopeBinding("plutonium/242", Isotopes.PU_242, 2.27e19));

        list.add(isotopeBinding("uranium/233", Isotopes.U_233, 5.54e21));
        list.add(isotopeBinding("uranium/235", Isotopes.U_235, 3.24e22));
        list.add(isotopeBinding("uranium/238", Isotopes.U_238, 1.93e22));

        addFissionFuels(list);
        addDepletedFissionFuels(list);

        return list;
    }

    public static List<BindingDefinition> defaults() {
        List<BindingDefinition> list = new ArrayList<>();

        list.add(BindingDefinition.builder("moon_surface_regolith")
                .item(rec("creatingspace:moon_surface_regolith"))
                .isotope(Isotopes.H_3, 1.1e17)
                .build());

        list.add(BindingDefinition.builder("enriched_yellowcake")
                .item(rec("createnuclear:enriched_yellowcake"))
                .isotope(Isotopes.U_235, 2.72e26)
                .isotope(Isotopes.U_233, 2.72e22)
                .isotope(Isotopes.U_238, 11.72e24)
                .build());

        list.add(BindingDefinition.builder("yellowcake")
                .item(rec("createnuclear:yellowcake"))
                .isotope(Isotopes.U_235, 19.72e22)
                .isotope(Isotopes.U_238, 11.72e24)
                .build());

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
                .isotope(Isotopes.AC_225, 19.72e15)
                .build());

        list.add(BindingDefinition.builder("pellet_polonium")
                .itemTag(pelletTag("polonium"))
                .isotope(Isotopes.PO_210, 16.72e17)
                .build());

        list.add(BindingDefinition.builder("dust_promethium")
                .itemTag(dustTag("promethium_147"))
                .isotope(Isotopes.PM_147, 3.72e19)
                .build());

        list.add(BindingDefinition.builder("dust_ruthenium")
                .itemTag(dustTag("ruthenium_106"))
                .isotope(Isotopes.RU_106, 1.72e19)
                .build());

        list.add(BindingDefinition.builder("dust_strontium")
                .itemTag(dustTag("strontium_90"))
                .isotope(Isotopes.SR_90, 3.72e19)
                .build());

        list.add(BindingDefinition.builder("dust_protactinium")
                .itemTag(dustTag("protactinium_233"))
                .isotope(Isotopes.PA_233, 3.72e19)
                .isotope(Isotopes.PA_91, 1.72e13)
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
                .isotope(Isotopes.AC_225, 3.37e21)
                .build());

        list.add(BindingDefinition.builder("americium_241")
                .itemTag(isotopeTag("americium_241"))
                .isotope(Isotopes.AM_241, 19.72e19)
                .build());

        list.add(BindingDefinition.builder("americium_242")
                .itemTag(isotopeTag("americium_242"))
                .isotope(Isotopes.AM_242, 4.65e18)
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

        list.add(BindingDefinition.builder("europium_155_dust")
                .itemTag(dustTag("europium_155"))
                .isotope(Isotopes.EU_155, 1.89e19)
                .build());

        list.add(BindingDefinition.builder("caesium_137_dust")
                .itemTag(dustTag("caesium_137"))
                .isotope(Isotopes.CS_137, 1.84e19)
                .build());

        list.add(BindingDefinition.builder("uranium_raw_item")
                .itemTag(rawTag("uranium"))
                .isotope(Isotopes.U_238, 2.51e24)
                .isotope(Isotopes.U_235, 1.84e22)
                .build());

        list.add(BindingDefinition.builder("uranium_ingot_item")
                .itemTag(ingotTag("uranium"))
                .isotope(Isotopes.U_238, 4.27e25)
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
                .isotope(Isotopes.CS_137, 4.4e17)
                .build());

        list.add(BindingDefinition.builder("radioactive_high_item")
                .itemTag(RadiationTags.ITEM_HIGH)
                .isotope(Isotopes.CO_60, 1.0e20)
                .isotope(Isotopes.CS_137, 4.4e20)
                .build());

        list.add(BindingDefinition.builder("radioactive_low_block")
                .blockTag(RadiationTags.BLOCK_LOW)
                .isotope(Isotopes.U_238, 2.5e17)
                .build());

        list.add(BindingDefinition.builder("radioactive_medium_block")
                .blockTag(RadiationTags.BLOCK_MEDIUM)
                .isotope(Isotopes.CS_137, 4.4e18)
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

        list.add(BindingDefinition.builder("berkelium_block")
                .itemTag(blockTag("berkelium"))
                .isotope(Isotopes.BK_248, 2.76e20)
                .isotope(Isotopes.BK_247, 3.76e20)
                .build());

        list.add(BindingDefinition.builder("curium_block")
                .itemTag(blockTag("curium"))
                .isotope(Isotopes.CM_247, 5.34e17)
                .isotope(Isotopes.CM_246, 2.35e21)
                .build());

        list.add(BindingDefinition.builder("californium_block")
                .itemTag(blockTag("californium"))
                .isotope(Isotopes.CF_249, 2.35e20)
                .isotope(Isotopes.CF_250, 2.35e19)
                .isotope(Isotopes.CF_251, 2.35e19)
                .build());

        list.add(BindingDefinition.builder("neptunium_block")
                .itemTag(blockTag("neptunium"))
                .isotope(Isotopes.NP_236, 5.66e17)
                .isotope(Isotopes.NP_237, 5.66e18)
                .build());

        list.add(BindingDefinition.builder("plutonium_block")
                .itemTag(blockTag("plutonium"))
                .isotope(Isotopes.PU_242, 2.27e18)
                .isotope(Isotopes.PU_241, 2.27e17)
                .build());

        list.add(BindingDefinition.builder("americium_block")
                .itemTag(blockTag("americium"))
                .isotope(Isotopes.AM_241, 19.72e19)
                .isotope(Isotopes.AM_242, 19.72e19)
                .build());

        list.add(BindingDefinition.builder("raw_uranium")
                .itemTag(cTag("raw_materials/uranium"))
                .isotope(Isotopes.U_238, 2.51e24)
                .isotope(Isotopes.U_235, 1.84e22)
                .build());

        list.add(BindingDefinition.builder("raw_uranium_block")
                .itemTag(blockTag("raw_uranium"))
                .isotope(Isotopes.U_238, 18.51e24)
                .isotope(Isotopes.U_235, 9.84e22)
                .build());

        list.add(BindingDefinition.builder("raw_thorium_block")
                .itemTag(blockTag("raw_thorium"))
                .isotope(Isotopes.TH_232, 56.31e19)
                .isotope(Isotopes.TH_230, 9.31e14)
                .build());

        list.add(BindingDefinition.builder("uranium_block")
                .itemTag(blockTag("uranium"))
                .isotope(Isotopes.U_238, 18.51e24)
                .isotope(Isotopes.U_235, 9.84e22)
                .build());

        list.add(BindingDefinition.builder("thorium_block")
                .itemTag(blockTag("thorium"))
                .isotope(Isotopes.TH_232, 56.31e19)
                .isotope(Isotopes.TH_230, 9.31e14)
                .build());

        list.add(BindingDefinition.builder("ingots_thorium")
                .itemTag(cTag("ingots/thorium"))
                .isotope(Isotopes.TH_232, 6.31e19)
                .isotope(Isotopes.TH_230, 6.31e14)
                .build());

        list.add(BindingDefinition.builder("dusts_thorium")
                .itemTag(cTag("dusts/thorium"))
                .isotope(Isotopes.TH_232, 6.31e19)
                .isotope(Isotopes.TH_230, 6.31e14)
                .build());

        list.add(BindingDefinition.builder("dusts_tbp")
                .itemTag(dustTag("tbp"))
                .isotope(Isotopes.TH_232, 6.31e16)
                .isotope(Isotopes.TH_230, 6.31e19)
                .isotope(Isotopes.PA_91, 6.31e20)
                .build());

        list.add(BindingDefinition.builder("dusts_thorium")
                .itemTag(rawTag("thorium"))
                .isotope(Isotopes.TH_232, 6.31e19)
                .isotope(Isotopes.TH_230, 6.31e14)
                .build());

        list.add(BindingDefinition.builder("storage_blocks_thorium")
                .itemTag(cTag("storage_blocks/thorium"))
                .isotope(Isotopes.TH_232, 6.31e21)
                .isotope(Isotopes.TH_230, 6.31e16)
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

        list.add(BindingDefinition.builder("corium_fluid")
                .fluidTag(TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "corium")))
                .isotope(Isotopes.CS_137, 4.4e19)
                .isotope(Isotopes.SR_90, 3.0e19)
                .isotope(Isotopes.PU_239, 3.57e18)
                .build());

        list.add(BindingDefinition.builder("corium_block")
                .itemTag(cTag("storage_blocks/corium"))
                .isotope(Isotopes.CS_137, 4.4e17)
                .isotope(Isotopes.SR_90, 3.0e17)
                .isotope(Isotopes.PU_239, 3.57e17)
                .build());

        list.add(BindingDefinition.builder("fallout_dust")
                .item(rec("nuclear_radiation:fallout_dust"))
                .isotope(Isotopes.CS_137, 11.0e15)
                .isotope(Isotopes.SR_90, 2.0e15)
                .isotope(Isotopes.I_131, 10.0e13)
                .build());

        list.add(BindingDefinition.builder("fallout_dust_block_item")
                .item(rec("nuclear_radiation:fallout_dust_block"))
                .isotope(Isotopes.CS_137, 12.0e15)
                .isotope(Isotopes.SR_90, 2.0e15)
                .isotope(Isotopes.I_131, 20.0e13)
                .build());

        list.add(BindingDefinition.builder("fallout_dust_block")
                .block(rec("nuclear_radiation:fallout_dust_block"))
                .isotope(Isotopes.CS_137, 12.0e15)
                .isotope(Isotopes.SR_90, 2.0e15)
                .isotope(Isotopes.I_131, 20.0e13)
                .build());

        list.add(BindingDefinition.builder("cnt_plutonium_239_ingot")
                .item(rec("createnucleartech:plutonium_239_ingot"))
                .isotope(Isotopes.PU_239, 3.57e21)
                .build());

        list.add(BindingDefinition.builder("cnt_plutonium_core")
                .item(rec("createnucleartech:plutonium_core"))
                .isotope(Isotopes.PU_239, 3.57e21)
                .isotope(Isotopes.PU_241, 5.95e21)
                .isotope(Isotopes.AM_241, 19.72e19)
                .build());

        list.add(BindingDefinition.builder("cnt_natural_uranium_fuel_rod")
                .item(rec("createnucleartech:natural_uranium_fuel_rod"))
                .isotope(Isotopes.U_235, 1.5e19)
                .isotope(Isotopes.U_238, 11.72e21)
                .build());

        list.add(BindingDefinition.builder("cnt_enriched_uranium_fuel_rod")
                .item(rec("createnucleartech:enriched_uranium_fuel_rod"))
                .isotope(Isotopes.U_235, 1.72e21)
                .isotope(Isotopes.U_238, 11.72e21)
                .build());

        list.add(BindingDefinition.builder("cnt_military_uranium_fuel_rod")
                .item(rec("createnucleartech:military_uranium_fuel_rod"))
                .isotope(Isotopes.U_235, 19.72e21)
                .isotope(Isotopes.U_238, 11.72e22)
                .build());

        list.add(BindingDefinition.builder("cnt_mox_fuel_rod")
                .item(rec("createnucleartech:mox_fuel_rod"))
                .isotope(Isotopes.PU_238, 21.72e19)
                .isotope(Isotopes.PU_239, 3.57e21)
                .isotope(Isotopes.U_238, 11.72e20)
                .build());

        list.add(BindingDefinition.builder("cnt_plutonium_fuel_rod")
                .item(rec("createnucleartech:plutonium_fuel_rod"))
                .isotope(Isotopes.PU_238, 21.72e20)
                .isotope(Isotopes.PU_239, 3.57e21)
                .isotope(Isotopes.U_238, 11.72e20)
                .build());

        list.add(BindingDefinition.builder("cnt_reactor_plutonium_fuel_rod")
                .item(rec("createnucleartech:reactor_plutonium_fuel_rod"))
                .isotope(Isotopes.PU_239, 3.57e21)
                .isotope(Isotopes.PU_241, 5.95e20)
                .isotope(Isotopes.AM_241, 19.72e19)
                .build());

        list.add(BindingDefinition.builder("cnt_thorium_fuel_rod")
                .item(rec("createnucleartech:thorium_fuel_rod"))
                .isotope(Isotopes.TH_232, 6.31e20)
                .isotope(Isotopes.TH_230, 6.31e16)
                .build());

        list.add(BindingDefinition.builder("cnt_spent_fuel_rod")
                .item(rec("createnucleartech:spent_fuel_rod"))
                .isotope(Isotopes.PU_239, 1.72e19)
                .isotope(Isotopes.AM_241, 0.72e20)
                .isotope(Isotopes.CS_137, 4.4e19)
                .isotope(Isotopes.SR_90, 3.0e20)
                .build());

        list.add(BindingDefinition.builder("cnt_spent_natural_uranium_fuel_rod")
                .item(rec("createnucleartech:spent_natural_uranium_fuel_rod"))
                .isotope(Isotopes.U_238, 1.93e14)
                .isotope(Isotopes.PU_239, 1.72e18)
                .isotope(Isotopes.CS_137, 4.5e19)
                .isotope(Isotopes.SR_90, 3.0e20)
                .build());

        list.add(BindingDefinition.builder("cnt_spent_enriched_uranium_fuel_rod")
                .item(rec("createnucleartech:spent_enriched_uranium_fuel_rod"))
                .isotope(Isotopes.U_238, 1.93e14)
                .isotope(Isotopes.PU_239, 1.72e19)
                .isotope(Isotopes.CS_137, 7.4e19)
                .isotope(Isotopes.SR_90, 3.0e20)
                .build());

        list.add(BindingDefinition.builder("cnt_spent_military_uranium_fuel_rod")
                .item(rec("createnucleartech:spent_military_uranium_fuel_rod"))
                .isotope(Isotopes.U_238, 1.93e14)
                .isotope(Isotopes.PU_239, 1.72e19)
                .isotope(Isotopes.CS_137, 8.4e19)
                .isotope(Isotopes.SR_90, 3.0e20)
                .build());

        list.add(BindingDefinition.builder("cnt_spent_mox_fuel_rod")
                .item(rec("createnucleartech:spent_mox_fuel_rod"))
                .isotope(Isotopes.PU_239, 1.72e19)
                .isotope(Isotopes.AM_241, 0.72e20)
                .isotope(Isotopes.CS_137, 1.4e20)
                .isotope(Isotopes.SR_90, 3.0e20)
                .build());

        list.add(BindingDefinition.builder("cnt_spent_plutonium_fuel_rod")
                .item(rec("createnucleartech:spent_plutonium_fuel_rod"))
                .isotope(Isotopes.AM_241, 0.72e20)
                .isotope(Isotopes.AM_243, 1.15e19)
                .isotope(Isotopes.CS_137, 9.4e19)
                .isotope(Isotopes.SR_90, 3.0e20)
                .build());

        list.add(BindingDefinition.builder("cnt_spent_reactor_plutonium_fuel_rod")
                .item(rec("createnucleartech:spent_reactor_plutonium_fuel_rod"))
                .isotope(Isotopes.PU_239, 1.72e19)
                .isotope(Isotopes.AM_241, 0.72e20)
                .isotope(Isotopes.CS_137, 4.4e20)
                .isotope(Isotopes.SR_90, 3.0e20)
                .build());

        list.add(BindingDefinition.builder("cnt_spent_thorium_fuel_rod")
                .item(rec("createnucleartech:spent_thorium_fuel_rod"))
                .isotope(Isotopes.U_233, 5.54e20)
                .isotope(Isotopes.CS_137, 4.4e218)
                .isotope(Isotopes.SR_90, 3.0e20)
                .build());

        list.add(BindingDefinition.builder("cnt_plutonium_240_ingot")
                .item(rec("createnucleartech:plutonium_240_ingot"))
                .isotope(Isotopes.PU_240, 8.4e20)
                .build());

        list.add(BindingDefinition.builder("cnt_cobalt_60_source")
                .item(rec("createnucleartech:cobalt_60_source"))
                .isotope(Isotopes.CO_60, 6.50e22)
                .build());

        list.add(BindingDefinition.builder("cnt_iridium_192_source")
                .item(rec("createnucleartech:iridium_192_source"))
                .isotope(Isotopes.IR_192, 5.30e23)
                .build());

        return list;
    }

    public static ResourceLocation rec(String s) {
        return  ResourceLocation.tryParse(s);
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

    private static BindingDefinition isotopeBinding(String path, String isotope, double atoms) {
        return BindingDefinition.builder("isotopes/" + path)
                .itemTag(isotopeTag(path))
                .isotope(isotope, atoms)
                .build();
    }

    // Per-isotope atom counts reused from the isotope bindings above so activity balance matches.
    private static final double A_U233 = 5.54e20, A_U235 = 1.24e18, A_U238 = 1.93e14;
    private static final double A_NP236 = 5.66e19, A_NP237 = 4.05e19;
    private static final double A_PU238 = 9.85e20, A_PU239 = 3.57e21, A_PU241 = 5.95e21, A_PU242 = 2.27e17;
    private static final double A_AM241 = 19.72e19, A_AM242 = 4.65e17, A_AM243 = 1.15e19;
    private static final double A_CM243 = 2.91e21, A_CM245 = 1.02e19, A_CM246 = 1.77e19, A_CM247 = 5.34e15;
    private static final double A_BK247 = 6.03e19, A_BK248 = 2.76e20;
    private static final double A_CF249 = 2.35e20, A_CF250 = 6.29e21, A_CF251 = 9.12e19, A_CF252 = 3.08e22;
    private static final double A_TH232 = 6.31e15, A_SR90 = 3.0e20, A_CS137 = 4.4e20;

    private record Part(String isotope, double base, int parts) {}

    private static Part p(String isotope, double base, int parts) {
        return new Part(isotope, base, parts);
    }

    // Fresh fission fuel: fissile + fertile blend, parts out of 9 (LE = 1:8, HE = 3:6).
    // Compositions from NuclearCraft: Neohaul ModRecipeProvider.fuel(...).
    private static void addFissionFuels(List<BindingDefinition> list) {
        fuel(list, "americium/hea_242", p(Isotopes.AM_242, A_AM242, 3), p(Isotopes.AM_243, A_AM243, 2));
        fuel(list, "americium/lea_242", p(Isotopes.AM_242, A_AM242, 1), p(Isotopes.AM_243, A_AM243, 1));

        fuel(list, "berkelium/heb_248", p(Isotopes.BK_248, A_BK248, 3), p(Isotopes.BK_247, A_BK247, 6));
        fuel(list, "berkelium/leb_248", p(Isotopes.BK_248, A_BK248, 1), p(Isotopes.BK_247, A_BK247, 8));

        fuel(list, "californium/hecf_249", p(Isotopes.CF_249, A_CF249, 3), p(Isotopes.CF_252, A_CF252, 6));
        fuel(list, "californium/lecf_249", p(Isotopes.CF_249, A_CF249, 1), p(Isotopes.CF_252, A_CF252, 8));
        fuel(list, "californium/hecf_251", p(Isotopes.CF_251, A_CF251, 3), p(Isotopes.CF_252, A_CF252, 6));
        fuel(list, "californium/hecm_243", p(Isotopes.CM_243, A_CM243, 3), p(Isotopes.CM_246, A_CM246, 6));
        fuel(list, "californium/lecm_243", p(Isotopes.CM_243, A_CM243, 1), p(Isotopes.CM_246, A_CM246, 8));

        fuel(list, "curium/lecm_243", p(Isotopes.CM_243, A_CM243, 3), p(Isotopes.CM_246, A_CM246, 1));
        fuel(list, "curium/hecm_245", p(Isotopes.CM_245, A_CM245, 3), p(Isotopes.CM_246, A_CM246, 6));
        fuel(list, "curium/hecm_245", p(Isotopes.CM_245, A_CM245, 3), p(Isotopes.CM_246, A_CM246, 6));
        fuel(list, "curium/lecm_245", p(Isotopes.CM_245, A_CM245, 1), p(Isotopes.CM_246, A_CM246, 8));
        fuel(list, "curium/hecm_247", p(Isotopes.CM_247, A_CM247, 3), p(Isotopes.CM_246, A_CM246, 6));
        fuel(list, "curium/lecm_247", p(Isotopes.CM_247, A_CM247, 1), p(Isotopes.CM_246, A_CM246, 8));

        fuel(list, "neptunium/hen_236", p(Isotopes.NP_236, A_NP236, 3), p(Isotopes.NP_237, A_NP237, 6));
        fuel(list, "neptunium/len_236", p(Isotopes.NP_236, A_NP236, 1), p(Isotopes.NP_237, A_NP237, 8));

        fuel(list, "plutonium/hep_239", p(Isotopes.PU_239, A_PU239, 3), p(Isotopes.PU_242, A_PU242, 6));
        fuel(list, "neptunium/lep_239", p(Isotopes.PU_239, A_PU239, 1), p(Isotopes.PU_242, A_PU242, 8));
        fuel(list, "plutonium/hep_241", p(Isotopes.PU_241, A_PU241, 3), p(Isotopes.PU_242, A_PU242, 4));
        fuel(list, "plutonium/lep_241", p(Isotopes.PU_241, A_PU241, 1), p(Isotopes.PU_242, A_PU242, 8));

        fuel(list, "thorium/tbu", p(Isotopes.TH_232, A_TH232, 9), p(Isotopes.TH_230, A_TH232, 1));

        fuel(list, "uranium/heu_233", p(Isotopes.U_233, A_U233, 9), p(Isotopes.U_238, A_U238, 6));
        fuel(list, "uranium/leu_233", p(Isotopes.U_233, A_U233, 3), p(Isotopes.U_238, A_U238, 8));
        fuel(list, "uranium/heu_235", p(Isotopes.U_235, A_U235, 9), p(Isotopes.U_238, A_U238, 6));
        fuel(list, "uranium/leu_235", p(Isotopes.U_235, A_U235, 3), p(Isotopes.U_238, A_U238, 8));

        fuel(list, "mixed/mix_239", p(Isotopes.PU_239, A_PU239, 2), p(Isotopes.U_238, A_U238, 8));
        fuel(list, "mixed/mix_241", p(Isotopes.PU_241, A_PU241, 2), p(Isotopes.U_238, A_U238, 8));
    }

    // Depleted (spent) fission fuel: reprocessor output actinides + Sr-90/Cs-137 waste.
    // Compositions from NuclearCraft: Neohaul FuelReprocessorProvider. Other fission
    // products (Mo, Pm-147, Ru-106, Eu-155) have no NR isotope, so they are omitted.
    private static void addDepletedFissionFuels(List<BindingDefinition> list) {
        fuel(list, "depleted_thorium/tbu", p(Isotopes.U_233, A_U233, 1), p(Isotopes.U_238, A_U238, 5), p(Isotopes.NP_236, A_NP236, 1), p(Isotopes.NP_237, A_NP237, 1), p(Isotopes.SR_90, A_SR90, 1), p(Isotopes.CS_137, A_CS137, 1));

        fuel(list, "depleted_uranium/leu_233", p(Isotopes.U_238, A_U238, 5), p(Isotopes.PU_241, A_PU241, 1), p(Isotopes.PU_242, A_PU242, 1), p(Isotopes.AM_243, A_AM243, 1), p(Isotopes.SR_90, A_SR90, 1), p(Isotopes.CS_137, A_CS137, 1));
        fuel(list, "depleted_uranium/heu_233", p(Isotopes.U_235, A_U235, 1), p(Isotopes.U_238, A_U238, 2), p(Isotopes.PU_242, A_PU242, 3), p(Isotopes.AM_243, A_AM243, 1), p(Isotopes.SR_90, A_SR90, 1), p(Isotopes.CS_137, A_CS137, 1));
        fuel(list, "depleted_uranium/leu_235", p(Isotopes.U_238, A_U238, 4), p(Isotopes.PU_239, A_PU239, 1), p(Isotopes.PU_242, A_PU242, 2), p(Isotopes.AM_243, A_AM243, 1), p(Isotopes.CS_137, A_CS137, 1));
        fuel(list, "depleted_uranium/heu_235", p(Isotopes.U_238, A_U238, 3), p(Isotopes.NP_236, A_NP236, 1), p(Isotopes.PU_242, A_PU242, 2), p(Isotopes.AM_243, A_AM243, 1), p(Isotopes.CS_137, A_CS137, 1));

        fuel(list, "depleted_neptunium/len_236", p(Isotopes.U_238, A_U238, 4), p(Isotopes.NP_237, A_NP237, 1), p(Isotopes.PU_241, A_PU241, 1), p(Isotopes.PU_242, A_PU242, 2), p(Isotopes.CS_137, A_CS137, 1));
        fuel(list, "depleted_neptunium/hen_236", p(Isotopes.U_238, A_U238, 4), p(Isotopes.PU_238, A_PU238, 1), p(Isotopes.PU_241, A_PU241, 1), p(Isotopes.PU_242, A_PU242, 1), p(Isotopes.CS_137, A_CS137, 1));

        fuel(list, "depleted_plutonium/lep_239", p(Isotopes.PU_242, A_PU242, 5), p(Isotopes.AM_242, A_AM242, 1), p(Isotopes.AM_243, A_AM243, 1), p(Isotopes.CM_246, A_CM246, 1), p(Isotopes.SR_90, A_SR90, 1));
        fuel(list, "depleted_plutonium/hep_239", p(Isotopes.PU_241, A_PU241, 1), p(Isotopes.AM_242, A_AM242, 1), p(Isotopes.AM_243, A_AM243, 1), p(Isotopes.CM_243, A_CM243, 1));
        fuel(list, "depleted_plutonium/lep_241", p(Isotopes.PU_242, A_PU242, 5), p(Isotopes.AM_243, A_AM243, 1), p(Isotopes.CM_246, A_CM246, 1), p(Isotopes.BK_247, A_BK247, 1), p(Isotopes.SR_90, A_SR90, 1));
        fuel(list, "depleted_plutonium/hep_241", p(Isotopes.AM_241, A_AM241, 1), p(Isotopes.AM_242, A_AM242, 1), p(Isotopes.AM_243, A_AM243, 1), p(Isotopes.CM_246, A_CM246, 1));

        fuel(list, "depleted_mixed/mix_239", p(Isotopes.U_238, A_U238, 4), p(Isotopes.PU_241, A_PU241, 1), p(Isotopes.PU_242, A_PU242, 2), p(Isotopes.AM_243, A_AM243, 1), p(Isotopes.SR_90, A_SR90, 1));
        fuel(list, "depleted_mixed/mix_241", p(Isotopes.U_238, A_U238, 3), p(Isotopes.PU_241, A_PU241, 1), p(Isotopes.PU_242, A_PU242, 3), p(Isotopes.AM_243, A_AM243, 1), p(Isotopes.SR_90, A_SR90, 1));

        fuel(list, "depleted_americium/lea_242", p(Isotopes.AM_243, A_AM243, 3), p(Isotopes.CM_245, A_CM245, 1), p(Isotopes.CM_246, A_CM246, 3), p(Isotopes.BK_248, A_BK248, 1));
        fuel(list, "depleted_americium/hea_242", p(Isotopes.AM_243, A_AM243, 3), p(Isotopes.CM_243, A_CM243, 1), p(Isotopes.CM_246, A_CM246, 2), p(Isotopes.BK_247, A_BK247, 1));

        fuel(list, "depleted_curium/lecm_243", p(Isotopes.CM_246, A_CM246, 4), p(Isotopes.CM_247, A_CM247, 1), p(Isotopes.BK_247, A_BK247, 2), p(Isotopes.BK_248, A_BK248, 1));
        fuel(list, "depleted_curium/hecm_243", p(Isotopes.CM_245, A_CM245, 1), p(Isotopes.CM_246, A_CM246, 3), p(Isotopes.BK_247, A_BK247, 2), p(Isotopes.BK_248, A_BK248, 1));
        fuel(list, "depleted_curium/lecm_245", p(Isotopes.CM_246, A_CM246, 4), p(Isotopes.CM_247, A_CM247, 1), p(Isotopes.BK_247, A_BK247, 2), p(Isotopes.CF_249, A_CF249, 1));
        fuel(list, "depleted_curium/hecm_245", p(Isotopes.CM_246, A_CM246, 3), p(Isotopes.CM_247, A_CM247, 1), p(Isotopes.BK_247, A_BK247, 2), p(Isotopes.CF_249, A_CF249, 1));
        fuel(list, "depleted_curium/lecm_247", p(Isotopes.CM_246, A_CM246, 5), p(Isotopes.BK_247, A_BK247, 1), p(Isotopes.BK_248, A_BK248, 1), p(Isotopes.CF_249, A_CF249, 1));
        fuel(list, "depleted_curium/hecm_247", p(Isotopes.BK_247, A_BK247, 4), p(Isotopes.BK_248, A_BK248, 1), p(Isotopes.CF_249, A_CF249, 1), p(Isotopes.CF_251, A_CF251, 1));

        fuel(list, "depleted_berkelium/leb_248", p(Isotopes.BK_247, A_BK247, 5), p(Isotopes.BK_248, A_BK248, 1), p(Isotopes.CF_249, A_CF249, 1), p(Isotopes.CF_251, A_CF251, 1));
        fuel(list, "depleted_berkelium/heb_248", p(Isotopes.BK_248, A_BK248, 1), p(Isotopes.CF_249, A_CF249, 1), p(Isotopes.CF_251, A_CF251, 2), p(Isotopes.CF_252, A_CF252, 3));

        fuel(list, "depleted_californium/lecf_249", p(Isotopes.CF_252, A_CF252, 29), p(Isotopes.EU_155, A_CF250, 1));
        fuel(list, "depleted_californium/hecf_249", p(Isotopes.CF_250, A_CF250, 1), p(Isotopes.CF_252, A_CF252, 6), p(Isotopes.EU_155, A_CF250, 1));
        fuel(list, "depleted_californium/lecf_251", p(Isotopes.CF_252, A_CF252, 19), p(Isotopes.EU_155, A_CF250, 1));
        fuel(list, "depleted_californium/hecf_251", p(Isotopes.CF_252, A_CF252, 7), p(Isotopes.EU_155, A_CF250, 1));
    }

    private static void fuel(List<BindingDefinition> list, String path, Part... parts) {
        BindingDefinition.Builder b = BindingDefinition.builder("fission_fuel/" + path)
                .itemTag(cTag("fission_fuel/" + path));
        for (Part part : parts) {
            b.isotope(part.isotope(), part.base() * part.parts() / 9.0);
        }
        list.add(b.build());
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Path base = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(NuclearRadiation.MODID)
                .resolve(RadiationBindingsReloadListener.FOLDER);

        List<CompletableFuture<?>> futures = new ArrayList<>(bindings.size());
        for (BindingDefinition def : bindings) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", def.type);
            obj.addProperty("target", def.tag ? "#" + def.target : def.target.toString());

            JsonObject isotopes = new JsonObject();
            for (Map.Entry<String, Double> e : def.isotopes.entrySet()) {
                JsonObject v = new JsonObject();
                v.addProperty("atoms", e.getValue());
                isotopes.add(e.getKey(), v);
            }
            obj.add("isotopes", isotopes);

            Path p = base.resolve(def.fileId + ".json");
            futures.add(DataProvider.saveStable(cachedOutput, obj, p));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Nuclear Radiation Bindings";
    }
}
