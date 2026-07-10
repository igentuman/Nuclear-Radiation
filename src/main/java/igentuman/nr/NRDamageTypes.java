package igentuman.nr;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public final class NRDamageTypes {

    private NRDamageTypes() {}

    public static final ResourceKey<DamageType> LUNG_CANCER =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "lung_cancer"));

    public static final ResourceKey<DamageType> RADIATION =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(NuclearRadiation.MODID, "radiation"));

    public static DamageSource source(ServerLevel level, ResourceKey<DamageType> key) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(key));
    }
}
