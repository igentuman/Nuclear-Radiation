package igentuman.nr.api;

import igentuman.nr.radiation.simulation.inventory.RadiatedSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.stream.Stream;

public interface IInventoryRadSlotProvider {
    Stream<RadiatedSlot> slots(LivingEntity entity);
}
