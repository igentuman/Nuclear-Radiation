package igentuman.nr.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.nuclearteam.createnuclear.content.explosion.NuclearExplosionEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = NuclearExplosionEntity.class, remap = false)
public abstract class CreateNuclearExplosionMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean nr$useBlastDamage(LivingEntity entity, DamageSource radiation, float amount) {
        return entity.hurt(entity.damageSources().explosion((Entity) (Object) this, null), amount);
    }
}
