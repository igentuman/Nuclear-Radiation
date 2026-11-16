package igentuman.nr.compat.ntm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "com.hbm.inventory.container.MenuCrate", remap = false)
public interface HeldCrateMenuAccessor {
    @Accessor("heldSlot")
    int nr$heldSlot();
}
