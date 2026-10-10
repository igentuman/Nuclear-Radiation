package igentuman.nr.api.isotope;

import igentuman.nr.api.RadiationQuality;

import java.util.Optional;

public final class IsotopeImpl implements Isotope {
    private final String id;
    private final float xRayStrength;
    private final float alphaStrength;
    private final float betaStrength;
    private final float neutronStrength;
    private final long halfLifeTicks;
    private final String decaysToId;
    private final RadiationQuality quality;

    public IsotopeImpl(String id,
                       float xRayStrength,
                       float alphaStrength,
                       float betaStrength,
                       float neutronStrength,
                       long halfLifeTicks,
                       String decaysToId,
                       RadiationQuality quality) {
        this.id = id;
        this.xRayStrength = xRayStrength;
        this.alphaStrength = alphaStrength;
        this.betaStrength = betaStrength;
        this.neutronStrength = neutronStrength;
        this.halfLifeTicks = halfLifeTicks;
        this.decaysToId = decaysToId;
        this.quality = quality == null ? RadiationQuality.DEFAULT : quality;
    }

    @Override public String id() { return id; }
    @Override public float xRayStrength() { return xRayStrength; }
    @Override public float alphaStrength() { return alphaStrength; }
    @Override public float betaStrength() { return betaStrength; }
    @Override public float neutronStrength() { return neutronStrength; }
    @Override public long halfLifeTicks() { return halfLifeTicks; }
    @Override public RadiationQuality quality() { return quality; }

    public String decaysToId() { return decaysToId; }

    @Override
    public Optional<Isotope> decaysTo() {
        if (decaysToId == null) return Optional.empty();
        return Optional.ofNullable(IsotopeRegistry.get(decaysToId));
    }
}
