package igentuman.nr.radiation.simulation.containers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class OpenContainerRegistry {

    private static final Map<UUID, AbstractContainerMenu> OPEN = new ConcurrentHashMap<>();

    private OpenContainerRegistry() {}

    public static void open(ServerPlayer player, AbstractContainerMenu menu) {
        if (menu == null) return;
        OPEN.put(player.getUUID(), menu);
    }

    public static void close(ServerPlayer player) {
        OPEN.remove(player.getUUID());
    }

    public static AbstractContainerMenu current(UUID playerId) {
        return OPEN.get(playerId);
    }
}
