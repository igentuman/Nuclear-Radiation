package igentuman.nr.radiation.shielding.world;

public record AttenuationResult(double xrayPass, double neutronPass) {
    public static final AttenuationResult UNATTENUATED = new AttenuationResult(1.0, 1.0);
}
