package igentuman.nr.api.shielding;

import net.minecraft.resources.Identifier;

public final class ShieldingUpgradeDefinition {

    public final String fileId;
    public final Identifier target;
    public final double value;

    public ShieldingUpgradeDefinition(String fileId, Identifier target, double value) {
        this.fileId = fileId;
        this.target = target;
        this.value = value;
    }
}
