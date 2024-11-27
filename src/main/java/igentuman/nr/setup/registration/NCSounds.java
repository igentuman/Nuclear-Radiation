package igentuman.nr.setup.registration;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;
import java.util.HashMap;
import java.util.List;
import static igentuman.nr.NuclearRadiation.rl;
import static igentuman.nr.setup.registration.Registries.SOUND_EVENTS;

public final class NCSounds {

    private NCSounds() {
    }

    public static final List<RegistryObject<SoundEvent>> GEIGER_SOUNDS = initGeigerSounds();

    private static List<RegistryObject<SoundEvent>> initGeigerSounds() {
        return List.of(
                SOUND_EVENTS.register("geiger_1", () -> SoundEvent.createVariableRangeEvent(rl( "geiger_1"))),
                SOUND_EVENTS.register("geiger_2", () -> SoundEvent.createVariableRangeEvent(rl( "geiger_2"))),
                SOUND_EVENTS.register("geiger_3", () -> SoundEvent.createVariableRangeEvent(rl( "geiger_3"))),
                SOUND_EVENTS.register("geiger_4", () -> SoundEvent.createVariableRangeEvent(rl( "geiger_4"))),
                SOUND_EVENTS.register("geiger_5", () -> SoundEvent.createVariableRangeEvent(rl( "geiger_5")))
        );
    }

    public static void init() {

    }
}