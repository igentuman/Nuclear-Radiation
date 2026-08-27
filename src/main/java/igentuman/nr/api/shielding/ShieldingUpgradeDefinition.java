package igentuman.nr.api.shielding;

import net.minecraft.resources.ResourceLocation;

public final class ShieldingUpgradeDefinition {

    public final String fileId;
    public final ResourceLocation target;
    public final double value;

    public ShieldingUpgradeDefinition(String fileId, ResourceLocation target, double value) {
        this.fileId = fileId;
        this.target = target;
        this.value = value;
    }
}
