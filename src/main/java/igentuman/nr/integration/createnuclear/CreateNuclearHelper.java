package igentuman.nr.integration.createnuclear;

import igentuman.nr.api.RadiationProfile;
import igentuman.nr.client.particle.MeltdownParticles;
import igentuman.nr.integration.createnucleartech.CreateNTHelper;
import igentuman.nr.radiation.source.LeftOverRadSource;
import igentuman.nr.radiation.source.WorldSourceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

/** Bridges Create Nuclear's meltdown footprint to persistent NR world sources. */
public final class CreateNuclearHelper {
    private static final int FALLOUT_SPACING = 48;

    private CreateNuclearHelper() {}

    public static void registerMeltdownFallout(ServerLevel level, BlockPos center, int radius) {
        int effectiveRadius = Math.max(0, radius);
        List<BlockPos> positions = new ArrayList<>();
        positions.add(center.immutable());
        for (int x = -effectiveRadius; x <= effectiveRadius; x += FALLOUT_SPACING) {
            for (int z = -effectiveRadius; z <= effectiveRadius; z += FALLOUT_SPACING) {
                if (x == 0 && z == 0 || (long) x * x + (long) z * z > (long) effectiveRadius * effectiveRadius) continue;
                positions.add(center.offset(x, 0, z));
            }
        }

        long now = level.getGameTime();
        // The explosion size is radius / 30; distribute one fission-product inventory
        // over the affected area instead of multiplying it by the number of sources.
        double size = Math.max(1.0, effectiveRadius / 30.0);
        RadiationProfile fallout = CreateNTHelper.buildFalloutProfile(size * 450.0, 4, now);
        WorldSourceRegistry sources = WorldSourceRegistry.get(level);
        for (BlockPos pos : positions) {
            RadiationProfile share = new RadiationProfile();
            share.mergeAtoms(fallout, 1.0 / positions.size(), now);
            sources.register(new LeftOverRadSource(level, pos, share, now, true));
        }
        MeltdownParticles.emit(level, center);
    }
}
