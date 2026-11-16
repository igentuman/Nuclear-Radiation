package igentuman.nr.compat.ntm;

import igentuman.nr.api.RadiationProfile;
import igentuman.nr.registry.DefaultIsotopes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NtmItemProfilesTest {
    private static final double SV_PER_UNIT = 0.008;

    @BeforeEach
    void registerIsotopes() {
        for (var definition : DefaultIsotopes.defaults()) DefaultIsotopes.apply(definition);
    }

    @Test
    void everyDefaultCompositionParsesAndResolves() {
        var table = NtmItemProfiles.parse(NtmItemProfiles.defaultCompositions());
        assertEquals(NtmItemProfiles.defaultCompositions().size(), table.size());
        table.forEach((material, weights) ->
                assertTrue(NtmItemProfiles.referenceSvPerBq(weights) > 0.0, material));
    }

    @Test
    void parserSplitsNamespacedIsotopeIdsOnLastColon() {
        var table = NtmItemProfiles.parse(List.of("Mix=nr:pu_239:2,nr:pu_240:1,nr:pu_239:1"));
        assertEquals(Map.of("nr:pu_239", 3.0, "nr:pu_240", 1.0), table.get("mix"));
        assertThrows(IllegalArgumentException.class, () -> NtmItemProfiles.parse(List.of("x=nr:u_235:0")));
        assertThrows(IllegalArgumentException.class, () -> NtmItemProfiles.parse(List.of("x=nr:u_235")));
        assertThrows(IllegalArgumentException.class, () -> NtmItemProfiles.parse(List.of("nr:u_235:1")));
    }

    @Test
    void dose_equivalent_magnitude_tracks_native_level_independent_of_isotope_quality() {
        Map<String, Double> alpha = Map.of("nr:po_210", 1.0);
        Map<String, Double> gamma = Map.of(NtmEmissionProfiles.FALLOUT, 1.0);
        for (var weights : List.of(alpha, gamma)) {
            RadiationProfile one = NtmItemProfiles.profile(1.0, weights, SV_PER_UNIT);
            RadiationProfile ten = NtmItemProfiles.profile(10.0, weights, SV_PER_UNIT);
            assertEquals(SV_PER_UNIT, NtmEmissionProfiles.unshieldedSvPerSecond(one), SV_PER_UNIT * 1e-9);
            assertEquals(10 * NtmEmissionProfiles.unshieldedSvPerSecond(one),
                    NtmEmissionProfiles.unshieldedSvPerSecond(ten), SV_PER_UNIT * 1e-8);
        }
        // Fallback default equals the world scale: 8e12 Bq per unit for an X-ray-only quality-1 mixture.
        assertEquals(8.0e12, NtmItemProfiles.profile(1.0, gamma, SV_PER_UNIT).totalActivityBq(), 1.0);
    }

    @Test
    void unusableLevelsOrMixturesYieldEmptyProfiles() {
        Map<String, Double> gamma = Map.of(NtmEmissionProfiles.FALLOUT, 1.0);
        assertTrue(NtmItemProfiles.profile(0.0, gamma, SV_PER_UNIT).isEmpty());
        assertTrue(NtmItemProfiles.profile(1.0e-6, gamma, SV_PER_UNIT).isEmpty());
        assertTrue(NtmItemProfiles.profile(Double.NaN, gamma, SV_PER_UNIT).isEmpty());
        assertTrue(NtmItemProfiles.profile(Double.POSITIVE_INFINITY, gamma, SV_PER_UNIT).isEmpty());
        assertTrue(NtmItemProfiles.profile(1.0, Map.of("nr:missing", 1.0), SV_PER_UNIT).isEmpty());
    }

    @Test
    void materialPrefersTagsThenBoundedLongestItemWords() {
        Set<String> known = NtmItemProfiles.parse(NtmItemProfiles.defaultCompositions()).keySet();
        assertEquals("u235", NtmItemProfiles.material(List.of("nuggets/u235"), "nugget_u235", known));
        assertEquals("pu_mix", NtmItemProfiles.material(List.of(), "billet_pu_mix", known));
        assertEquals("pu239", NtmItemProfiles.material(List.of(), "rbmk_fuel_pu239", known));
        assertEquals("balefire", NtmItemProfiles.material(List.of(), "powder_balefire", known));
        assertNull(NtmItemProfiles.material(List.of(), "nuclear_waste_long", known));
        assertNull(NtmItemProfiles.material(List.of("ingots/uranium"), "waste_uranium", known));
        assertNull(NtmItemProfiles.material(List.of(), "ingot_mox_fuel", known));
        // Word boundaries: "co60" is not found inside "cobalt".
        assertNull(NtmItemProfiles.material(List.of(), "ingot_cobalt", known));
    }

    @Test
    void reacherFollowsNativeCurves() {
        // r = 20 RAD/s / 20 = 1 per tick: sqrt(1 + 1/9) - 1/3.
        double expected = Math.sqrt(1.0 + 1.0 / 9.0) - 1.0 / 3.0;
        assertEquals(expected, NtmItemProfiles.reacherScale(20.0, false), 1e-12);
        assertEquals(1.0 / 49.0, NtmItemProfiles.reacherScale(20.0, true), 1e-12);
        assertEquals(1.0, NtmItemProfiles.reacherScale(0.0, false));
        assertEquals(1.0, NtmItemProfiles.reacherScale(Double.NaN, false));
        assertTrue(NtmItemProfiles.reacherScale(2000.0, false) < NtmItemProfiles.reacherScale(20.0, false));
    }
}
