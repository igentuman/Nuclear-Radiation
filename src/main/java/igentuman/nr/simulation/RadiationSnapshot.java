package igentuman.nr.simulation;

import igentuman.nr.api.IPointRadiationSource;

import java.util.List;
import java.util.UUID;

public final class RadiationSnapshot {

    public record SourceData(UUID id,
                             double x, double y, double z,
                             double radius,
                             double xRayBq,
                             double neutronBq,
                             double alphaBq,
                             double betaBq) {}

    public record EntityData(int entityId,
                             double x, double y, double z) {}

    public final List<SourceData> sources;
    public final List<EntityData> entities;
    public final long tick;

    public RadiationSnapshot(List<SourceData> sources, List<EntityData> entities, long tick) {
        this.sources = sources;
        this.entities = entities;
        this.tick = tick;
    }

    public static SourceData of(IPointRadiationSource s) {
        var c = s.emissionCenter();
        return new SourceData(s.getId(), c.x, c.y, c.z, s.radius(), s.xRayBq(), s.neutronBq(), s.alphaBq(), s.betaBq());
    }
}
