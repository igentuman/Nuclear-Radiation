package igentuman.nr;

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

    public static final DeferredHolder<SoundEvent, SoundEvent> VOMIT =
            SOUND_EVENTS.register("vomit",
                    () -> SoundEvent.createVariableRangeEvent(rl("vomit")));

    public static final DeferredHolder<SoundEvent, SoundEvent> INJECT =
            SOUND_EVENTS.register("inject",
                    () -> SoundEvent.createVariableRangeEvent(rl("inject")));

    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER_TICK =
            SOUND_EVENTS.register("geiger_tick",
                    () -> SoundEvent.createVariableRangeEvent(rl("geiger_tick")));

    private NRSounds() {}

    public static void register(IEventBus modBus) { SOUND_EVENTS.register(modBus); }
}
