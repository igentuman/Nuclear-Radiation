package igentuman.nr.events;

import igentuman.nr.config.RadiationConfig;
import igentuman.nr.radiation.simulation.inventory.InventoryRadCache;
import igentuman.nr.radiation.simulation.entity.EntityDoseProcessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import com.google.common.collect.Lists;
import java.util.List;

public class EntityExposureEvents {

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        long now = server.getGameTime();
        int interval = RadiationConfig.ENTITY_SIM_INTERVAL_TICKS.get();
        boolean stagger = RadiationConfig.STAGGER_ENTITIES.get() && interval > 1;

        if (!stagger && now % interval != 0) return;
        List<Entity> entities = Lists.newArrayList(server.getAllEntities());

        if (!stagger) {
            for (Entity entity : entities) {
                if (!entity.isAlive()) continue;
                if (!(entity instanceof LivingEntity living)) continue;
                EntityDoseProcessor.tick(server, living, now, interval);
            }
            return;
        }

        int bucket = (int) Math.floorMod(now, interval);
        for (Entity entity : entities) {
            if (!entity.isAlive()) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            int entityBucket = Math.floorMod(entity.getId(), interval);
            if (entityBucket != bucket) continue;
            EntityDoseProcessor.tick(server, living, now, interval);
        }
    }

    @SubscribeEvent
    public void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            InventoryRadCache.drop(living.getUUID());
        }
    }
}
