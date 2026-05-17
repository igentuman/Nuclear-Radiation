package igentuman.nr.inventory;

import igentuman.nr.binding.RadiationBindings;
import igentuman.nr.binding.RadiationComponent;
import igentuman.nr.core.IsotopeStack;
import igentuman.nr.core.RadiationProfile;
import igentuman.nr.core.RadiationQuality;
import igentuman.nr.core.Units;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryRadCache {

    public static final int RESCAN_INTERVAL_TICKS = 100;

    private static final Map<UUID, InventoryRadCache> CACHES = new ConcurrentHashMap<>();

    public static InventoryRadCache get(LivingEntity entity) {
        return CACHES.computeIfAbsent(entity.getUUID(), k -> new InventoryRadCache());
    }

    public static void drop(UUID entityUuid) {
        CACHES.remove(entityUuid);
    }

    private double cachedBqXRay;
    private double cachedBqAlphaBeta;
    private double cachedBqNeutron;
    private double cachedSvXRayPerSec;
    private double cachedSvAlphaBetaPerSec;
    private double cachedSvNeutronPerSec;
    private int cachedInventoryHash;
    private long lastScanTick = Long.MIN_VALUE;

    public double bqXRay() { return cachedBqXRay; }
    public double bqAlphaBeta() { return cachedBqAlphaBeta; }
    public double bqNeutron() { return cachedBqNeutron; }

    public double svXRayPerSecPerGyBq() { return cachedSvXRayPerSec; }
    public double svAlphaBetaPerSecPerGyBq() { return cachedSvAlphaBetaPerSec; }
    public double svNeutronPerSecPerGyBq() { return cachedSvNeutronPerSec; }

    public void rescan(LivingEntity entity, long now) {
        int hash = computeHash(entity);
        if (hash == cachedInventoryHash && now - lastScanTick < RESCAN_INTERVAL_TICKS) {
            return;
        }

        double bqX = 0.0, bqAB = 0.0, bqN = 0.0;
        double qX = 0.0, qAB = 0.0, qN = 0.0;
        for (IInventoryRadSlotProvider provider : SlotProviders.all()) {
            for (var iter = provider.slots(entity).iterator(); iter.hasNext(); ) {
                RadiatedSlot slot = iter.next();
                ItemStack stack = slot.stack();
                if (stack.isEmpty()) continue;

                advanceComponentDecay(stack, now);

                RadiationProfile p = RadiationBindings.of(stack);
                if (p.isEmpty()) continue;
                double count = stack.getCount();
                double f = slot.slotFactor();
                for (IsotopeStack s : p.stacks()) {
                    double bq = s.currentActivityBq() * count * f;
                    if (bq <= 0.0) continue;
                    RadiationQuality q = s.isotope().quality();
                    double bqXi = bq * s.isotope().xRayStrength();
                    double bqABi = bq * s.isotope().alphaBetaStrength();
                    double bqNi  = bq * s.isotope().neutronStrength();
                    bqX  += bqXi;
                    bqAB += bqABi;
                    bqN  += bqNi;
                    qX  += bqXi  * q.qXRay;
                    qAB += bqABi * q.qAlpha;
                    qN  += bqNi  * q.qNeutron;
                }
            }
        }
        this.cachedBqXRay = bqX;
        this.cachedBqAlphaBeta = bqAB;
        this.cachedBqNeutron = bqN;
        this.cachedSvXRayPerSec = qX;
        this.cachedSvAlphaBetaPerSec = qAB;
        this.cachedSvNeutronPerSec = qN;
        this.cachedInventoryHash = hash;
        this.lastScanTick = now;
    }

    private static void advanceComponentDecay(ItemStack stack, long now) {
        RadiationComponent comp = stack.get(RadiationComponent.TYPE.get());
        if (comp == null || comp.isEmpty()) return;
        long delta = now - comp.lastTick();
        if (delta <= 0) return;
        Map<String, Double> updated = new LinkedHashMap<>();
        boolean changed = false;
        for (Map.Entry<String, Double> e : comp.atomsByIsotope().entrySet()) {
            var iso = igentuman.nr.registry.IsotopeRegistry.get(e.getKey());
            if (iso == null) { updated.put(e.getKey(), e.getValue()); continue; }
            double decayed = Units.decayAtoms(e.getValue(), iso.halfLifeTicks(), delta);
            if (Math.abs(decayed - e.getValue()) / Math.max(1.0, e.getValue()) > 1.0e-6) changed = true;
            updated.put(e.getKey(), decayed);
        }
        if (!changed) return;
        stack.set(RadiationComponent.TYPE.get(), new RadiationComponent(updated, now));
    }

    private static int computeHash(LivingEntity entity) {
        int h = 1;
        for (IInventoryRadSlotProvider provider : SlotProviders.all()) {
            for (var iter = provider.slots(entity).iterator(); iter.hasNext(); ) {
                RadiatedSlot slot = iter.next();
                ItemStack s = slot.stack();
                h = 31 * h + (s.isEmpty() ? 0 : (s.getItem().hashCode() * 31 + s.getCount()));
            }
        }
        return h;
    }

    public void markDirty() { lastScanTick = Long.MIN_VALUE; cachedInventoryHash = 0; }

    public static IsotopeStack dummyRef() { return null; }
}
