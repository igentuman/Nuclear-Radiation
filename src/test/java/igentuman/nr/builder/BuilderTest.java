package igentuman.nr.builder;

import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.registry.Isotopes;
import igentuman.nr.api.RadiationProfileBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuilderTest {

    @BeforeAll
    static void setup() {
        IsotopeRegistry.clear();
        Isotopes.bootstrap();
    }

    @Test
    void isotopeBuilderRegisters() {
        Isotope u238 = IsotopeRegistry.get(Isotopes.U_238);
        assertNotNull(u238);
        assertEquals(Isotopes.U_238, u238.id());
        assertTrue(u238.halfLifeTicks() > 0);
    }

    @Test
    void allBuiltInIsotopesRegistered() {
        for (String id : new String[]{
                Isotopes.U_238, Isotopes.U_235, Isotopes.PU_239, Isotopes.CS_137,
                Isotopes.I_131, Isotopes.SR_90, Isotopes.CO_60, Isotopes.CF_252
        }) {
            assertNotNull(IsotopeRegistry.get(id), "missing isotope " + id);
        }
    }

    @Test
    void profileBuilderProducesNonEmpty() {
        RadiationProfile p = RadiationProfileBuilder.create()
                .isotope(Isotopes.U_238, 1.0e18)
                .build();
        assertFalse(p.isEmpty());
        assertTrue(p.totalActivityBq() > 0.0);
    }

    @Test
    void cf252HasNeutronEmission() {
        Isotope cf = IsotopeRegistry.get(Isotopes.CF_252);
        assertTrue(cf.neutronStrength() > 0.0f);
    }

    @Test
    void unknownIsotopeIgnoredByProfileBuilder() {
        RadiationProfile p = RadiationProfileBuilder.create()
                .isotope("nr:nonexistent", 1.0e10)
                .build();
        assertTrue(p.isEmpty());
    }
}
