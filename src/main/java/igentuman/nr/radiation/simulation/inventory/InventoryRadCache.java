package igentuman.nr.radiation.simulation.inventory;

import igentuman.nr.api.*;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.api.binding.RadiationComponent;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.api.isotope.IsotopeStack;
import igentuman.nr.util.Units;
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
    private double cachedBqAlpha;
    private double cachedBqBeta;
    private double cachedBqNeutron;
    private double cachedSvXRayPerSec;
    private double cachedSvAlphaPerSec;
    private double cachedSvBetaPerSec;
    private double cachedSvNeutronPerSec;
    private int cachedInventoryHash;
    private long lastScanTick = Long.MIN_VALUE;

    public double bqXRay()   { return cachedBqXRay; }
    public double bqAlpha()  { return cachedBqAlpha; }
    public double bqBeta()   { return cachedBqBeta; }
    public double bqNeutron() { return cachedBqNeutron; }

    public double svXRayPerSecPerGyBq()    { return cachedSvXRayPerSec; }
    public double svAlphaPerSecPerGyBq()   { return cachedSvAlphaPerSec; }
    public double svBetaPerSecPerGyBq()    { return cachedSvBetaPerSec; }
    public double svNeutronPerSecPerGyBq() { return cachedSvNeutronPerSec; }

    public void rescan(LivingEntity entity, long now) {
        int hash = computeHash(entity);
        if (hash == cachedInventoryHash && now - lastScanTick < RESCAN_INTERVAL_TICKS) {
            return;
        }

        double bqX = 0.0, bqA = 0.0, bqB = 0.0, bqN = 0.0;
        double qX = 0.0, qA = 0.0, qB = 0.0, qN = 0.0;
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
                    if (bq == 0.0) continue;
                    RadiationQuality q = s.isotope().quality();
                    double bqXi = bq * s.isotope().xRayStrength();
                    double bqAi = bq * s.isotope().alphaStrength();
                    double bqBi = bq * s.isotope().betaStrength();
                    double bqNi = bq * s.isotope().neutronStrength();
                    bqX += bqXi;
                    bqA += bqAi;
                    bqB += bqBi;
                    bqN += bqNi;
                    qX += bqXi * q.qXRay;
                    qA += bqAi * q.qAlpha;
                    qB += bqBi * q.qBeta;
                    qN += bqNi * q.qNeutron;
                }
            }
        }
        this.cachedBqXRay = bqX;
        this.cachedBqAlpha = bqA;
        this.cachedBqBeta = bqB;
        this.cachedBqNeutron = bqN;
        this.cachedSvXRayPerSec = qX;
        this.cachedSvAlphaPerSec = qA;
        this.cachedSvBetaPerSec = qB;
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
            var iso = IsotopeRegistry.get(e.getKey());
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
