package igentuman.nr.compat.ntm;

import com.hbm.config.GeneralConfig;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.hazard.helper.HazardHelper;
import com.hbm.hazard.type.HazardTypeRadiation;
import com.hbm.inventory.container.ContainerCrateBase;
import com.hbm.inventory.container.ContainerLeadBox;
import com.hbm.inventory.container.ContainerToolBox;
import com.hbm.inventory.container.MenuCrate;
import com.hbm.inventory.container.MenuItemBox;
import igentuman.nr.compat.ntm.mixin.CrateBaseMenuAccessor;
import igentuman.nr.compat.ntm.mixin.HeldCrateMenuAccessor;
import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.config.NtmCompatConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pinned-NTM item adapter. Only reached through NtmCompatibility.enabled(). Native levels are queried
 * live, so transformers, fuel/RTG/RBMK modifiers, Space stack hooks and data-map reloads apply directly.
 */
final class NtmItemBridge {
    private NtmItemBridge() {}

    private record Table(List<? extends String> source, List<? extends String> fallbackSource,
                         Map<String, Map<String, Double>> materials, Map<String, Double> fallback) {}

    private static volatile Table table;
    private static final Map<Item, Map<String, Double>> COMPOSITIONS = new ConcurrentHashMap<>();

    /** Tags, isotopes or config changed: recompute material matches lazily. */
    static void invalidate() {
        COMPOSITIONS.clear();
        table = null;
    }

    /** Native radiation of one item of this stack, including stack-level hooks spread over the count. */
    static double nativeLevel(ItemStack stack) {
        if (stack.isEmpty()) return 0.0;
        double level = HazardSystem.getHazardLevelFromStack(stack, HazardRegistry.RADIATION);
        level += HazardTypeRadiation.stackRadiation(stack) / Math.max(1, stack.getCount());
        return Double.isFinite(level) ? level : 0.0;
    }

    private static final ThreadLocal<int[]> NATIVE_RETAINED = ThreadLocal.withInitial(() -> new int[1]);

    /** Native paths NR does not model (block contact, armor mods) keep their item radiation. */
    static void retainNative(Runnable nativeCall) {
        int[] depth = NATIVE_RETAINED.get();
        depth[0]++;
        try {
            nativeCall.run();
        } finally {
            depth[0]--;
        }
    }

    static boolean nativeRetained() {
        return NATIVE_RETAINED.get()[0] > 0;
    }

    static boolean hasReacher(LivingEntity entity) {
        return HazardHelper.hasReacher(entity);
    }

    static double reacherScale(ItemStack stack) {
        return NtmItemProfiles.reacherScale(nativeLevel(stack) * stack.getCount(), GeneralConfig.enable528);
    }

    /** Held item-container menus: the carried container item's level already includes these contents. */
    static boolean carriedItemMenu(AbstractContainerMenu menu) {
        if (menu instanceof ContainerLeadBox || menu instanceof ContainerToolBox || menu instanceof MenuItemBox) return true;
        if (menu instanceof MenuCrate) return ((HeldCrateMenuAccessor) menu).nr$heldSlot() >= 0;
        if (menu instanceof ContainerCrateBase) return ((CrateBaseMenuAccessor) menu).nr$heldSlot() >= 0;
        return false;
    }

    /** NTM caches tag-resolved base data per item and never refreshes it on tag reload. */
    static void tagsUpdated() {
        invalidate();
        HazardSystem.clearCaches();
    }

    static RadiationProfile profile(ItemStack stack) {
        double level = nativeLevel(stack);
        if (!(level > 1.0e-5)) return RadiationProfile.empty();
        return NtmItemProfiles.profile(level, composition(stack.getItem()),
                NtmCompatConfig.SV_PER_ITEM_RADIATION_UNIT.get());
    }

    /** Curated or fallback isotope weights for an item; also used for placed NTM hazard blocks. */
    static Map<String, Double> composition(Item item) {
        Table current = table();
        Map<String, Double> cached = COMPOSITIONS.get(item);
        if (cached != null) return cached;
        List<String> tagPaths = new ArrayList<>();
        item.builtInRegistryHolder().tags().forEach(tag -> {
            if ("c".equals(tag.location().getNamespace())) tagPaths.add(tag.location().getPath());
        });
        String material = NtmItemProfiles.material(tagPaths,
                BuiltInRegistries.ITEM.getKey(item).getPath(), current.materials().keySet());
        Map<String, Double> weights = material == null ? null : current.materials().get(material);
        // Isotope data may be missing on a client/data pack; never lose activity, fall back instead.
        if (weights == null || !(NtmItemProfiles.referenceSvPerBq(weights) > 0.0)) weights = current.fallback();
        COMPOSITIONS.put(item, weights);
        return weights;
    }

    private static Table table() {
        List<? extends String> source = NtmCompatConfig.ITEM_COMPOSITIONS.get();
        List<? extends String> fallbackSource = NtmCompatConfig.FALLBACK_COMPOSITION.get();
        Table current = table;
        if (current != null && current.source() == source && current.fallbackSource() == fallbackSource) return current;
        Map<String, Map<String, Double>> materials;
        try {
            materials = NtmItemProfiles.parse(source);
        } catch (IllegalArgumentException invalid) {
            NuclearRadiation.LOGGER.error("Invalid NTM item_compositions; using fallback_composition for all NTM items", invalid);
            materials = Map.of();
        }
        Map<String, Double> fallback;
        try {
            fallback = NtmEmissionProfiles.configured().composition();
        } catch (IllegalArgumentException invalid) {
            NuclearRadiation.LOGGER.error("Invalid NTM fallback_composition; items use {}", NtmEmissionProfiles.FALLOUT, invalid);
            fallback = Map.of(NtmEmissionProfiles.FALLOUT, 1.0);
        }
        current = new Table(source, fallbackSource, materials, fallback);
        COMPOSITIONS.clear();
        table = current;
        return current;
    }
}
