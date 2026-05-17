package igentuman.nr.binding;

import igentuman.nr.builder.RadiationBindingBuilder;
import igentuman.nr.registry.Isotopes;

public final class DefaultBindings {
    private DefaultBindings() {}

    public static void registerDefaults() {
        RadiationBindingBuilder.create()
                .itemTag(RadiationTags.ITEM_URANIUM_ORE)
                .isotope(Isotopes.U_238, 9.93e17)
                .isotope(Isotopes.U_235, 7.2e15)
                .register();

        RadiationBindingBuilder.create()
                .blockTag(RadiationTags.BLOCK_URANIUM_ORE)
                .isotope(Isotopes.U_238, 9.93e17)
                .isotope(Isotopes.U_235, 7.2e15)
                .register();

        RadiationBindingBuilder.create()
                .itemTag(RadiationTags.ITEM_SPENT_FUEL)
                .isotope(Isotopes.CS_137, 5.0e15)
                .isotope(Isotopes.SR_90, 5.0e15)
                .isotope(Isotopes.PU_239, 1.0e14)
                .isotope(Isotopes.U_238, 5.0e17)
                .register();

        RadiationBindingBuilder.create()
                .itemTag(RadiationTags.ITEM_LOW)
                .isotope(Isotopes.U_238, 1.0e16)
                .register();

        RadiationBindingBuilder.create()
                .itemTag(RadiationTags.ITEM_MEDIUM)
                .isotope(Isotopes.CS_137, 5.0e14)
                .register();

        RadiationBindingBuilder.create()
                .itemTag(RadiationTags.ITEM_HIGH)
                .isotope(Isotopes.CO_60, 1.0e15)
                .isotope(Isotopes.CS_137, 5.0e15)
                .register();

        RadiationBindingBuilder.create()
                .blockTag(RadiationTags.BLOCK_LOW)
                .isotope(Isotopes.U_238, 1.0e16)
                .register();
        RadiationBindingBuilder.create()
                .blockTag(RadiationTags.BLOCK_MEDIUM)
                .isotope(Isotopes.CS_137, 5.0e14)
                .register();
        RadiationBindingBuilder.create()
                .blockTag(RadiationTags.BLOCK_HIGH)
                .isotope(Isotopes.CO_60, 1.0e15)
                .isotope(Isotopes.CS_137, 5.0e15)
                .register();

        RadiationBindingBuilder.create()
                .fluidTag(RadiationTags.FLUID_RADIOACTIVE)
                .isotope(Isotopes.CS_137, 1.0e13)
                .register();
    }
}
