package igentuman.nr.registry;

import igentuman.nr.builder.IsotopeBuilder;

public final class Isotopes {
    private Isotopes() {}

    private static final long TICKS_PER_YEAR = 631128000L;
    private static final long TICKS_PER_DAY = 24000L;

    public static final String U_238   = "nr:u_238";
    public static final String U_235   = "nr:u_235";
    public static final String PU_239  = "nr:pu_239";
    public static final String CS_137  = "nr:cs_137";
    public static final String I_131   = "nr:i_131";
    public static final String SR_90   = "nr:sr_90";
    public static final String Y_90    = "nr:y_90";
    public static final String CO_60   = "nr:co_60";
    public static final String CF_252  = "nr:cf_252";

    public static void bootstrap() {
        if (IsotopeRegistry.contains(U_238)) return;

        IsotopeBuilder.create(U_238)
                .alpha(0.95f).beta(0.0f).xray(0.05f).neutron(0.0f)
                .halfLife(saturate(4.468e9 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .register();

        IsotopeBuilder.create(U_235)
                .alpha(0.85f).beta(0.0f).xray(0.15f).neutron(0.0f)
                .halfLife(saturate(7.04e8 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .register();

        IsotopeBuilder.create(PU_239)
                .alpha(0.95f).beta(0.0f).xray(0.05f).neutron(0.0f)
                .halfLife(saturate(24110.0 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(U_235, 1.0)
                .register();

        IsotopeBuilder.create(CS_137)
                .alpha(0.0f).beta(0.7f).xray(0.3f).neutron(0.0f)
                .halfLife(saturate(30.17 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .register();

        IsotopeBuilder.create(I_131)
                .alpha(0.0f).beta(0.6f).xray(0.4f).neutron(0.0f)
                .halfLife(saturate(8.02 * TICKS_PER_DAY))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .register();

        IsotopeBuilder.create(SR_90)
                .alpha(0.0f).beta(0.95f).xray(0.05f).neutron(0.0f)
                .halfLife(saturate(28.79 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .branch(Y_90, 1.0)
                .register();

        IsotopeBuilder.create(Y_90)
                .alpha(0.0f).beta(1.0f).xray(0.0f).neutron(0.0f)
                .halfLife(saturate(2.667 * TICKS_PER_DAY))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .register();

        IsotopeBuilder.create(CO_60)
                .alpha(0.0f).beta(0.2f).xray(0.8f).neutron(0.0f)
                .halfLife(saturate(5.27 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 10.0f)
                .register();

        IsotopeBuilder.create(CF_252)
                .alpha(0.65f).beta(0.0f).xray(0.05f).neutron(0.30f)
                .halfLife(saturate(2.645 * TICKS_PER_YEAR))
                .quality(1.0f, 1.0f, 20.0f, 20.0f)
                .register();
    }

    private static long saturate(double v) {
        if (v >= (double) Long.MAX_VALUE) return Long.MAX_VALUE;
        if (v <= 1.0) return 1L;
        return (long) v;
    }
}
