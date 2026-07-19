package igentuman.nr.registry;

import igentuman.nr.api.isotope.IsotopeRegistry;

public final class Isotopes {
    private Isotopes() {}

    public static final String U_238   = "nr:u_238";
    public static final String EU_155   = "nr:eu_155";
    public static final String PM_147 = "nr:pm_147";
    public static final String PA_233 = "nr:pa_233";
    public static final String RU_106 = "nr:ru_106";
    public static final String U_235   = "nr:u_235";
    public static final String PU_239  = "nr:pu_239";
    public static final String CS_55  = "nr:cs_55";
    public static final String CS_137  = "nr:cs_137";
    public static final String I_131   = "nr:i_131";
    public static final String SR_90   = "nr:sr_90";
    public static final String Y_90    = "nr:y_90";
    public static final String CO_60   = "nr:co_60";
    public static final String CF_252  = "nr:cf_252";
    public static final String H_3     = "nr:h_3";
    public static final String PO_210  = "nr:po_210";
    public static final String XE_133  = "nr:xe_133";
    public static final String KR_85   = "nr:kr_85";
    public static final String TH_230  = "nr:th_230";
    public static final String TH_232  = "nr:th_232";
    public static final String U_233   = "nr:u_233";
    public static final String U_234   = "nr:u_234";
    public static final String NP_236  = "nr:np_236";
    public static final String NP_237  = "nr:np_237";
    public static final String PU_238  = "nr:pu_238";
    public static final String PU_240  = "nr:pu_240";
    public static final String PU_241  = "nr:pu_241";
    public static final String PU_242  = "nr:pu_242";
    public static final String AM_241  = "nr:am_241";
    public static final String AM_242  = "nr:am_242";
    public static final String AM_243  = "nr:am_243";
    public static final String CM_243  = "nr:cm_243";
    public static final String CM_245  = "nr:cm_245";
    public static final String CM_246  = "nr:cm_246";
    public static final String CM_247  = "nr:cm_247";
    public static final String BK_247  = "nr:bk_247";
    public static final String BK_248  = "nr:bk_248";
    public static final String CF_249  = "nr:cf_249";
    public static final String CF_250  = "nr:cf_250";
    public static final String CF_251  = "nr:cf_251";
    public static final String NA_22   = "nr:na_22";
    public static final String CA_48   = "nr:ca_48";
    public static final String BE_7    = "nr:be_7";
    public static final String IR_192  = "nr:ir_192";
    public static final String CN_291  = "nr:cn_291";
    public static final String AC_225  = "nr:ac_225";
    public static final String PA_91  = "nr:pa_91";

    public static void bootstrap() {
        if (IsotopeRegistry.contains(U_238)) return;
        DefaultIsotopes.registerDefaults();
    }
}
