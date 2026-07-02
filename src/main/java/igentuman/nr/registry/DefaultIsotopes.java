package igentuman.nr.registry;

import igentuman.nr.api.DecayEdge;
import igentuman.nr.builder.IsotopeBuilder;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nr.registry.Isotopes.*;

public final class DefaultIsotopes {
    private DefaultIsotopes() {}

    private static final long TICKS_PER_YEAR = 631128000L;
    private static final long TICKS_PER_DAY = 24000L;
    private static final long TICKS_PER_HOUR = 1000L;

    public static List<IsotopeDefinition> defaults() {
        List<IsotopeDefinition> list = new ArrayList<>();

        list.add(IsotopeDefinition.create(U_238)
                .alpha(0.95f).beta(0.0f).xray(0.05f).neutron(0.0f)
                .halfLife(saturate(4.468e9 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(U_235)
                .alpha(0.85f).beta(0.0f).xray(0.15f).neutron(0.0f)
                .halfLife(saturate(7.04e8 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(PU_239)
                .alpha(0.95f).beta(0.0f).xray(0.05f).neutron(0.0f)
                .halfLife(saturate(24110.0 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(U_235, 1.0)
                .build());

        list.add(IsotopeDefinition.create(CS_137)
                .alpha(0.0f).beta(0.7f).xray(0.3f).neutron(0.0f)
                .halfLife(saturate(30.17 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(I_131)
                .alpha(0.0f).beta(0.6f).xray(0.4f).neutron(0.0f)
                .halfLife(saturate(8.02 * TICKS_PER_DAY))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(SR_90)
                .alpha(0.0f).beta(0.95f).xray(0.05f).neutron(0.0f)
                .halfLife(saturate(28.79 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(Y_90, 1.0)
                .build());

        list.add(IsotopeDefinition.create(Y_90)
                .alpha(0.0f).beta(1.0f).xray(0.0f).neutron(0.0f)
                .halfLife(saturate(2.667 * TICKS_PER_DAY))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(CO_60)
                .alpha(0.0f).beta(0.2f).xray(0.8f).neutron(0.0f)
                .halfLife(saturate(5.27 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(CF_252)
                .alpha(0.65f).beta(0.0f).xray(0.05f).neutron(0.30f)
                .halfLife(saturate(2.645 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 20.0f)
                .build());

        list.add(IsotopeDefinition.create(H_3)
                .alpha(0.0f).beta(1.0f).xray(0.0f).neutron(0.0f)
                .halfLife(saturate(12.32 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(PO_210)
                .alpha(1.0f).beta(0.0f).xray(0.05f).neutron(0.0f)
                .halfLife(saturate(138.376 * TICKS_PER_DAY))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(XE_133)
                .alpha(0.0f).beta(0.6f).xray(0.4f).neutron(0.0f)
                .halfLife(saturate(5.2475 * TICKS_PER_DAY))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(KR_85)
                .alpha(0.0f).beta(0.9f).xray(0.1f).neutron(0.0f)
                .halfLife(saturate(10.756 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(TH_230)
                .alpha(0.92f).beta(0.0f).xray(0.08f).neutron(0.0f)
                .halfLife(saturate(7.54e4 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(TH_232)
                .alpha(0.95f).beta(0.0f).xray(0.05f).neutron(0.0f)
                .halfLife(saturate(1.405e10 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(U_233)
                .alpha(0.93f).beta(0.0f).xray(0.07f).neutron(0.0f)
                .halfLife(saturate(1.592e5 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(U_234)
                .alpha(0.95f).beta(0.0f).xray(0.05f).neutron(0.0f)
                .halfLife(saturate(2.455e5 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(TH_230, 1.0)
                .build());

        list.add(IsotopeDefinition.create(NP_236)
                .alpha(0.0f).beta(0.5f).xray(0.5f).neutron(0.0f)
                .halfLife(saturate(1.54e5 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        // Np-237 alpha -> Pa-233 (27 d beta-) -> U-233; short Pa-233 stage collapsed.
        list.add(IsotopeDefinition.create(NP_237)
                .alpha(0.90f).beta(0.0f).xray(0.10f).neutron(0.0f)
                .halfLife(saturate(2.144e6 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(U_233, 1.0)
                .build());

        list.add(IsotopeDefinition.create(PU_238)
                .alpha(0.93f).beta(0.0f).xray(0.02f).neutron(0.05f)
                .halfLife(saturate(87.7 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(U_234, 1.0)
                .build());

        list.add(IsotopeDefinition.create(PU_241)
                .alpha(0.0f).beta(0.98f).xray(0.02f).neutron(0.0f)
                .halfLife(saturate(14.329 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(AM_241, 1.0)
                .build());

        list.add(IsotopeDefinition.create(PU_242)
                .alpha(0.94f).beta(0.0f).xray(0.03f).neutron(0.03f)
                .halfLife(saturate(3.75e5 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(U_238, 1.0)
                .build());

        list.add(IsotopeDefinition.create(AM_241)
                .alpha(0.90f).beta(0.0f).xray(0.10f).neutron(0.0f)
                .halfLife(saturate(432.6 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(NP_237, 1.0)
                .build());

        // Am-242 ground state (16.02 h, beta-/EC). EC branch -> Pu-242; Cm-242 daughter not modeled.
        list.add(IsotopeDefinition.create(AM_242)
                .alpha(0.0f).beta(0.80f).xray(0.20f).neutron(0.0f)
                .halfLife(saturate(16.02 * TICKS_PER_HOUR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(PU_242, 1.0)
                .build());

        // Am-243 alpha -> Np-239 (2.36 d beta-) -> Pu-239; short Np-239 stage collapsed.
        list.add(IsotopeDefinition.create(AM_243)
                .alpha(0.88f).beta(0.0f).xray(0.12f).neutron(0.0f)
                .halfLife(saturate(7364.0 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(PU_239, 1.0)
                .build());

        list.add(IsotopeDefinition.create(CM_243)
                .alpha(0.90f).beta(0.0f).xray(0.10f).neutron(0.0f)
                .halfLife(saturate(29.1 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(PU_239, 1.0)
                .build());

        list.add(IsotopeDefinition.create(CM_245)
                .alpha(0.90f).beta(0.0f).xray(0.10f).neutron(0.0f)
                .halfLife(saturate(8250.0 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(PU_241, 1.0)
                .build());

        list.add(IsotopeDefinition.create(CM_246)
                .alpha(0.93f).beta(0.0f).xray(0.02f).neutron(0.05f)
                .halfLife(saturate(4730.0 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 20.0f)
                .branch(PU_242, 1.0)
                .build());

        list.add(IsotopeDefinition.create(CM_247)
                .alpha(0.90f).beta(0.0f).xray(0.10f).neutron(0.0f)
                .halfLife(saturate(1.56e7 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(AM_243, 1.0)
                .build());

        list.add(IsotopeDefinition.create(BK_247)
                .alpha(0.93f).beta(0.0f).xray(0.07f).neutron(0.0f)
                .halfLife(saturate(1380.0 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(AM_243, 1.0)
                .build());

        // Bk-248 ground-state half-life uncertain (>9 y); adopted ~300 y.
        list.add(IsotopeDefinition.create(BK_248)
                .alpha(0.90f).beta(0.0f).xray(0.10f).neutron(0.0f)
                .halfLife(saturate(300.0 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(CF_249)
                .alpha(0.85f).beta(0.0f).xray(0.15f).neutron(0.0f)
                .halfLife(saturate(351.0 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(CM_245, 1.0)
                .build());

        list.add(IsotopeDefinition.create(CF_250)
                .alpha(0.93f).beta(0.0f).xray(0.02f).neutron(0.05f)
                .halfLife(saturate(13.08 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 20.0f)
                .branch(CM_246, 1.0)
                .build());

        list.add(IsotopeDefinition.create(CF_251)
                .alpha(0.95f).beta(0.0f).xray(0.05f).neutron(0.0f)
                .halfLife(saturate(898.0 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(CM_247, 1.0)
                .build());

        list.add(IsotopeDefinition.create(NA_22)
                .alpha(0.0f).beta(0.40f).xray(0.60f).neutron(0.0f)
                .halfLife(saturate(2.602 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        // Ca-48 double-beta, T-half 6.4e19 y -> effectively static (negligible activity).
        list.add(IsotopeDefinition.create(CA_48)
                .alpha(0.0f).beta(1.0f).xray(0.0f).neutron(0.0f)
                .halfLife(saturate(6.4e19 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        // Be-7 decays by electron capture -> 477 keV gamma only, no beta emitted.
        list.add(IsotopeDefinition.create(BE_7)
                .alpha(0.0f).beta(0.0f).xray(1.0f).neutron(0.0f)
                .halfLife(saturate(53.22 * TICKS_PER_DAY))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        list.add(IsotopeDefinition.create(IR_192)
                .alpha(0.0f).beta(0.50f).xray(0.50f).neutron(0.0f)
                .halfLife(saturate(73.827 * TICKS_PER_DAY))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        // Cn-291 superheavy: no confirmed data. Half-life speculative (~30 s placeholder).
        list.add(IsotopeDefinition.create(CN_291)
                .alpha(0.90f).beta(0.0f).xray(0.05f).neutron(0.05f)
                .halfLife(saturate(30.0 * TICKS_PER_HOUR / 3600.0))
                .quality(1.0f, 1.0f, 20.0f, 20.0f)
                .build());

        // Ac-225 alpha -> Fr-221 (short-lived chain); daughter chain not modeled.
        list.add(IsotopeDefinition.create(AC_225)
                .alpha(0.95f).beta(0.0f).xray(0.05f).neutron(0.0f)
                .halfLife(saturate(9.92 * TICKS_PER_DAY))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .build());

        return list;
    }

    public static void registerDefaults() {
        for (IsotopeDefinition def : defaults()) {
            apply(def);
        }
    }

    public static void apply(IsotopeDefinition def) {
        IsotopeBuilder b = IsotopeBuilder.create(def.id)
                .alpha(def.alpha).beta(def.beta).xray(def.xray).neutron(def.neutron)
                .halfLife(def.halfLifeTicks)
                .quality(def.qXray, def.qBeta, def.qAlpha, def.qNeutron);
        for (DecayEdge e : def.branches) {
            b.branch(e.targetIsotopeId(), e.probability());
        }
        b.register();
    }

    static long saturate(double v) {
        if (v >= (double) Long.MAX_VALUE) return Long.MAX_VALUE;
        if (v <= 1.0) return 1L;
        return (long) v;
    }
}
