package igentuman.nr.api.shielding;

public interface IShieldingBlock {
    double xrayAttenuationCoeff();
    double neutronAttenuationCoeff();
    default double gyAbsorbed() { return 0.0; }
}
