package igentuman.nr.registry;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.effects.IsotopeSpecificProtectionEffect;
import igentuman.nr.effects.RadiationProtectionEffect;
import igentuman.nr.effects.RadiationPurgeEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NREffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, NuclearRadiation.MODID);

    public static final DeferredHolder<MobEffect, RadiationProtectionEffect> PROTECTION =
            EFFECTS.register("radiation_protection", RadiationProtectionEffect::new);

    public static final DeferredHolder<MobEffect, RadiationPurgeEffect> PURGE =
            EFFECTS.register("radiation_purge", RadiationPurgeEffect::new);

    public static final DeferredHolder<MobEffect, IsotopeSpecificProtectionEffect> IODINE_PROTECTION =
            EFFECTS.register("iodine_protection",
                    () -> new IsotopeSpecificProtectionEffect(0xE57373, Isotopes.I_131));

    public static final DeferredHolder<MobEffect, IsotopeSpecificProtectionEffect> CESIUM_PURGE =
            EFFECTS.register("cesium_purge",
                    () -> new IsotopeSpecificProtectionEffect(0x4527A0, Isotopes.CS_137));

    private NREffects() {}

    public static void register(IEventBus modBus) { EFFECTS.register(modBus); }
}
