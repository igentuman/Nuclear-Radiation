package igentuman.nr.integration.kubejs;

import igentuman.nr.particle.MeltdownParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * KubeJS global binding {@code NRServerUtils}. Exposes server-side Nuclear Radiation utilities
 * to server scripts.
 *
 * <p>Accessible in any server script as the global variable {@code NRServerUtils}.</p>
 */
public final class NRServerUtils {

    private NRServerUtils() {}

    /**
     * Registers a radiation plume at the given block position for the default duration
     * ({@value MeltdownParticles#DEFAULT_DURATION_TICKS} ticks).
     *
     * @param level the server level
     * @param x     block X coordinate
     * @param y     block Y coordinate
     * @param z     block Z coordinate
     */
    public static void emitMeltdown(ServerLevel level, int x, int y, int z) {
        MeltdownParticles.emit(level, new BlockPos(x, y, z));
    }

    /**
     * Registers a radiation plume at the given block position for a custom duration.
     *
     * @param level         the server level
     * @param x             block X coordinate
     * @param y             block Y coordinate
     * @param z             block Z coordinate
     * @param durationTicks how many ticks the plume should last
     */
    public static void emitMeltdown(ServerLevel level, int x, int y, int z, int durationTicks) {
        MeltdownParticles.emit(level, new BlockPos(x, y, z), durationTicks);
    }
}
