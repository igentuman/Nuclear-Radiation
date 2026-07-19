package igentuman.nr.registry;

import igentuman.nr.NuclearRadiation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static igentuman.nr.NuclearRadiation.rl;

public final class NRSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, NuclearRadiation.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> VOMIT = registerSound("vomit");

    public static final DeferredHolder<SoundEvent, SoundEvent> COUGH = registerSound("cough");

    public static final DeferredHolder<SoundEvent, SoundEvent> INJECT = registerSound("inject");

    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER_TICK = registerSound("geiger_tick");

    private NRSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(rl(name)));
    }

    public static void register(IEventBus modBus) { SOUND_EVENTS.register(modBus); }
}
