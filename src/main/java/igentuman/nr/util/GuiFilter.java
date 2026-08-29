package igentuman.nr.util;

import igentuman.nr.config.RadiationConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;


public final class GuiFilter {

    // High-performance caches to avoid string operations during ticking
    private static final ConcurrentHashMap<Class<?>, Boolean> MENU_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, Boolean> SLOT_CACHE = new ConcurrentHashMap<>();

    private GuiFilter() {}

    public static boolean isMenuIgnored(AbstractContainerMenu menu) {
        ResourceLocation id = null;
        try {
            // Can throw UnsupportedOperationException if the menu has no registered type
            MenuType<?> type = menu.getType();
            id = BuiltInRegistries.MENU.getKey(type);
        } catch (UnsupportedOperationException ignored) {}

        // Check user-defined config
        if (id != null && RadiationConfig.isMenuIgnored(id)) {
            return true;
        }

        // Check heuristics with caching
        return MENU_CACHE.computeIfAbsent(menu.getClass(), clazz -> {
            String name = clazz.getSimpleName().toLowerCase(Locale.ROOT);
            return name.contains("term")
                    || name.contains("grid")
                    || name.contains("browser")
                    || name.contains("network")
                    || name.contains("wireless");
        });
    }


    public static boolean isSlotIgnored(Slot slot) {
        return SLOT_CACHE.computeIfAbsent(slot.getClass(), clazz -> {
            String name = clazz.getSimpleName().toLowerCase(Locale.ROOT);
            return name.contains("phantom")
                    || name.contains("ghost")
                    || name.contains("fake")
                    || name.contains("filter")
                    || name.contains("pattern")
                    || name.contains("config")
                    || name.contains("target");
        });
    }
}