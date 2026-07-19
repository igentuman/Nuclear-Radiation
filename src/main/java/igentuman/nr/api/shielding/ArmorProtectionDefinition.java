package igentuman.nr.api.shielding;

import net.minecraft.resources.ResourceLocation;

public final class ArmorProtectionDefinition {

    public final String fileId;
    public final ResourceLocation target;
    public final double xray;
    public final double alpha;
    public final double beta;
    public final double neutron;
    public final boolean protectsFromGas;

    public ArmorProtectionDefinition(String fileId, ResourceLocation target,
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
