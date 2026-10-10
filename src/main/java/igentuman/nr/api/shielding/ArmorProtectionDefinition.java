package igentuman.nr.api.shielding;

import net.minecraft.resources.Identifier;

public final class ArmorProtectionDefinition {

    public final String fileId;
    public final Identifier target;
    public final double xray;
    public final double alpha;
    public final double beta;
    public final double neutron;
    public final boolean protectsFromGas;

    public ArmorProtectionDefinition(String fileId, Identifier target,
                                     double xray, double alpha, double beta, double neutron, boolean protectsFromGas) {
        this.fileId = fileId;
        this.target = target;
        this.xray = xray;
        this.alpha = alpha;
        this.beta = beta;
        this.neutron = neutron;
        this.protectsFromGas = protectsFromGas;
    }
}
