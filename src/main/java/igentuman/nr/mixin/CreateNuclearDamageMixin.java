package igentuman.nr.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class CreateNuclearDamageMixin {
    private static final ResourceKey<DamageType> RADIATION = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("createnuclear", "radiation"));
    private static final ResourceKey<DamageType> FAN_RADIATION = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("createnuclear", "fan_radiation"));
    private static final ResourceLocation RADIATION_EFFECT =
            ResourceLocation.fromNamespaceAndPath("createnuclear", "radiation");

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void nr$suppressCreateNuclearDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.is(RADIATION) || source.is(FAN_RADIATION)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), cancellable = true)
    private void nr$suppressCreateNuclearEffect(MobEffectInstance effect, Entity source,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (RADIATION_EFFECT.equals(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()))) {
            cir.setReturnValue(false);
        }
    }
}
