package igentuman.nr.registry;

import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public class SoundEventRegistryObject<SOUND extends SoundEvent> extends WrappedRegistryObject<SOUND> {

    private final String translationKey;

    public SoundEventRegistryObject(RegistryObject<SOUND> registryObject) {
        super(registryObject);
        translationKey = Util.makeDescriptionId("sound_event", this.registryObject.getId());
    }

}