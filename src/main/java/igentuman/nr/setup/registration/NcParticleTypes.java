package igentuman.nr.setup.registration;

import igentuman.nr.registry.ParticleTypeRegistryObject;
import net.minecraft.core.particles.SimpleParticleType;

import static igentuman.nr.setup.registration.Registries.PARTICLE_TYPES;

public class NcParticleTypes {

    private NcParticleTypes() {
    }

    public static final ParticleTypeRegistryObject<SimpleParticleType, SimpleParticleType> RADIATION = PARTICLE_TYPES.registerBasicParticle("radiation");

    public static void init() {
    }
}