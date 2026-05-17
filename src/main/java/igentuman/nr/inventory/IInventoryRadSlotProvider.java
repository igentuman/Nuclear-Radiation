package igentuman.nr.inventory;

import net.minecraft.world.entity.LivingEntity;

import java.util.stream.Stream;

public interface IInventoryRadSlotProvider {
    Stream<RadiatedSlot> slots(LivingEntity entity);
}
