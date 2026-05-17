package igentuman.nr.containers;

import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IRadiatingContainer {

    BlockEntity blockEntity();

    Container container();

    default double containerAttenuation() { return 0.0; }

    default boolean contaminatesArea() { return false; }
}
