package igentuman.nr.tracking;

import igentuman.nr.core.RadiationProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

import java.lang.ref.WeakReference;
import java.util.UUID;

public final class ItemEntityRadSource extends AbstractWorldRadSource {

    private final WeakReference<ItemEntity> entityRef;
    private final UUID entityUuid;

    public ItemEntityRadSource(ItemEntity entity, RadiationProfile profile, long spawnedTick) {
        super(entity.getUUID(), entity.level().dimension(), entity.blockPosition(), profile, spawnedTick);
        this.entityRef = new WeakReference<>(entity);
        this.entityUuid = entity.getUUID();
    }

    public UUID entityUuid() { return entityUuid; }

    public ItemEntity entity() { return entityRef.get(); }

    @Override
    public BlockPos getPosition() {
        ItemEntity e = entityRef.get();
        if (e != null && !e.isRemoved()) {
            return e.blockPosition();
        }
        return super.getPosition();
    }

    @Override
    public boolean isActive() {
        ItemEntity e = entityRef.get();
        if (e == null || e.isRemoved()) return false;
        return super.isActive();
    }

    @Override
    public boolean contaminatesArea() { return true; }
}
