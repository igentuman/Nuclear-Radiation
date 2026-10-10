package igentuman.nr.radiation.simulation.entity;

import igentuman.nr.config.RadiationConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class EntityIgnoreFilter {

    private EntityIgnoreFilter() {}

    private static final AtomicReference<Set<Identifier>> CACHE = new AtomicReference<>(null);

    public static boolean shouldSkip(LivingEntity entity) {
        if (entity == null || entity.isRemoved()) return true;
        Set<Identifier> ignored = ignored();
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return ignored.contains(id);
    }

    public static boolean shouldSkipHarm(LivingEntity entity) {
        if (entity instanceof Player p) {
            if (RadiationConfig.IGNORE_CREATIVE.get() && p.isCreative()) return true;
            if (RadiationConfig.IGNORE_SPECTATOR.get() && p.isSpectator()) return true;
        }
        return false;
    }

    private static Set<Identifier> ignored() {
        Set<Identifier> cur = CACHE.get();
        if (cur != null) return cur;
        Set<Identifier> built = new HashSet<>();
        for (String s : RadiationConfig.IGNORED_ENTITIES.get()) {
            try { built.add(Identifier.parse(s)); } catch (Exception ignored) {}
        }
        cur = Collections.unmodifiableSet(built);
        CACHE.set(cur);
        return cur;
    }

    public static void invalidate() { CACHE.set(null); }
}
