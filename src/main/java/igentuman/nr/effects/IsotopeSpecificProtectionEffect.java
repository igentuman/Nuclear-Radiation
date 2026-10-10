package igentuman.nr.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class IsotopeSpecificProtectionEffect extends MobEffect {

    private final String targetIsotopeId;

    public IsotopeSpecificProtectionEffect(int color, String targetIsotopeId) {
        super(MobEffectCategory.BENEFICIAL, color);
        this.targetIsotopeId = targetIsotopeId;
    }

    public String targetIsotopeId() { return targetIsotopeId; }
}
