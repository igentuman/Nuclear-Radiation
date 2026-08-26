package igentuman.nr.radiation.simulation.containers;

import igentuman.nr.api.DecayGraph;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.api.isotope.IsotopeStack;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.radiation.source.ContainerRadSource;
import igentuman.nr.radiation.source.WorldSourceRegistry;
import igentuman.nr.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ContainerRadiationTicker {

    public static final int RESCAN_INTERVAL_TICKS = 40;

    private static final Map<ServerLevel, Set<BlockPos>> TRACKED = new ConcurrentHashMap<>();
    private static final Map<ServerLevel, Set<BlockPos>> DIRTY = new ConcurrentHashMap<>();

    private ContainerRadiationTicker() {}

    public static void track(ServerLevel level, BlockPos pos) {
        BlockPos immutable = pos.immutable();
        TRACKED.computeIfAbsent(level, k -> ConcurrentHashMap.newKeySet()).add(immutable);
        DIRTY.computeIfAbsent(level, k -> ConcurrentHashMap.newKeySet()).add(immutable);
    }

    public static void untrack(ServerLevel level, BlockPos pos) {
        Set<BlockPos> set = TRACKED.get(level);
        if (set != null) set.remove(pos);
        Set<BlockPos> dirty = DIRTY.get(level);
        if (dirty != null) dirty.remove(pos);
    }

    public static void markDirty(ServerLevel level, BlockPos pos) {
        Set<BlockPos> tracked = TRACKED.get(level);
        if (tracked == null || !tracked.contains(pos)) return;
        DIRTY.computeIfAbsent(level, k -> ConcurrentHashMap.newKeySet()).add(pos.immutable());
    }

    public static void unloadLevel(ServerLevel level) {
        TRACKED.remove(level);
        DIRTY.remove(level);
    }

    public static void scanLevel(ServerLevel level) {
        Set<BlockPos> dirty = DIRTY.get(level);
        if (dirty == null || dirty.isEmpty()) return;
        long now = level.getGameTime();
        double floor = RadiationConfig.WORLD_SOURCE_MIN_BQ.get();
        List<BlockPos> batch = new ArrayList<>(dirty);
        dirty.removeAll(batch);
        for (BlockPos pos : batch) {
            if (!level.isLoaded(pos)) continue;
            BlockEntity be = WorldUtil.getBlockEntity(pos, level, false);
            if (be == null) {
                removeIfPresent(level, pos);
                untrack(level, pos);
                continue;
            }
            scan(level, be, now, floor);
        }
    }

    public static void scan(ServerLevel level, BlockEntity be, long now, double floor) {
        Container container = resolveContainer(be);
        if (container == null) return;
        if (hasUnresolvedLootTable(be) || hasUnresolvedLootTable(container)) return;
        if (container.isEmpty()) {
            removeIfPresent(level, be.getBlockPos());
            return;
        }

        RadiationProfile aggregate = new RadiationProfile();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            RadiationProfile p = RadiationBindings.of(stack);
            if (p.isEmpty()) continue;
            for (IsotopeStack s : p.stacks()) {
                IsotopeStack existing = aggregate.get(s.isotope().id());
                if (existing == null) {
                    aggregate.put(new IsotopeStack(s.isotope(), s.atoms() * stack.getCount() / 2, now));
                } else {
                    existing.setAtoms(existing.atoms() + s.atoms() * stack.getCount() / 2);
                }
            }
        }

        if (Math.abs(aggregate.totalActivityBq()) < floor) {
            removeIfPresent(level, be.getBlockPos());
            return;
        }

        double attenuation = resolveAttenuation(be);
        WorldSourceRegistry reg = WorldSourceRegistry.get(level);
        DecayGraph.WorldRadSource existing = reg.atBlock(be.getBlockPos());
        if (existing instanceof ContainerRadSource crs) {
            crs.setContainerAttenuation(attenuation);
            replaceProfile(crs, aggregate, now);
        } else {
            if (existing != null) reg.remove(existing.getId());
            ContainerRadSource src = new ContainerRadSource(
                    UUID.randomUUID(), level.dimension(), be.getBlockPos().immutable(),
                    aggregate, now, attenuation);
            reg.register(src);
        }
    }

    private static void removeIfPresent(ServerLevel level, BlockPos pos) {
        WorldSourceRegistry reg = WorldSourceRegistry.get(level);
        DecayGraph.WorldRadSource existing = reg.atBlock(pos);
        if (existing instanceof ContainerRadSource) reg.remove(existing.getId());
    }

    private static Container resolveContainer(BlockEntity be) {
        if (be instanceof RadiationProfile.IRadiatingContainer rc) return rc.container();
        if (be instanceof Container c) return c;
        return null;
    }

    private static boolean hasUnresolvedLootTable(Object obj) {
        return obj instanceof RandomizableContainer rc && rc.getLootTable() != null;
    }

    private static double resolveAttenuation(BlockEntity be) {
        if (be instanceof RadiationProfile.IRadiatingContainer rc) return rc.containerAttenuation();
        var type = be.getType();
        var id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
        return ContainerAttenuationRegistry.get(type, id);
    }

    private static void replaceProfile(ContainerRadSource crs, RadiationProfile aggregate, long now) {
        RadiationProfile target = crs.getProfile();
        for (IsotopeStack s : aggregate.stacks()) {
            IsotopeStack ex = target.get(s.isotope().id());
            if (ex == null) {
                target.put(new IsotopeStack(s.isotope(), s.atoms(), now));
            } else {
                ex.setAtoms(s.atoms());
                ex.setTimestamp(now);
            }
        }
        Set<String> keep = new HashSet<>();
        for (IsotopeStack s : aggregate.stacks()) keep.add(s.isotope().id());
        target.stacks().removeIf(s -> !keep.contains(s.isotope().id()));
        crs.recomputeExpiry();
    }

    public static Map<ServerLevel, Set<BlockPos>> tracked() { return TRACKED; }
}
