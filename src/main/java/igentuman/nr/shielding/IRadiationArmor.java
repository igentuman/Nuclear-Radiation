package igentuman.nr.shielding;

import net.minecraft.world.entity.EquipmentSlot;

public interface IRadiationArmor {
    double xrayProtection();
    double alphaProtection();
    double betaProtection();
    double neutronProtection();
    boolean gasProtection();
    EquipmentSlot slot();
}
