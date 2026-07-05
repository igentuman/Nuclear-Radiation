package igentuman.nr.util.tracking;

import igentuman.nr.api.RadiationProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

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

    @Override public double activityBq() { return super.activityBq() * entity().getItem().getCount() * 10; }
    @Override public double xRayBq()    { return super.xRayBq() * entity().getItem().getCount() * 10; }
    @Override public double alphaBq()   { return super.alphaBq() * entity().getItem().getCount() * 10; }
    @Override public double betaBq()    { return super.betaBq() * entity().getItem().getCount() * 10; }
    @Override public double neutronBq() { return super.neutronBq() * entity().getItem().getCount() * 10; }
}
