package igentuman.nr.util.persistence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.api.RadiationProfile;

public class ChunkRadiationData {

    public static final Codec<ChunkRadiationData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            RadiationProfileCodec.CODEC.fieldOf("air").forGetter(d -> d.air),
            RadiationProfileCodec.CODEC.fieldOf("water").forGetter(d -> d.water),
            RadiationProfileCodec.CODEC.fieldOf("soil").forGetter(d -> d.soil),
            Codec.LONG.optionalFieldOf("last_decay_tick", 0L).forGetter(d -> d.lastDecayTick)
    ).apply(inst, ChunkRadiationData::new));

    private RadiationProfile air;
    private RadiationProfile water;
    private RadiationProfile soil;
    private long lastDecayTick;
    private long expiryGameTime = Long.MIN_VALUE;
    private boolean expiryDirty = true;

    public ChunkRadiationData() {
        this(RadiationProfile.empty(), RadiationProfile.empty(), RadiationProfile.empty(), 0L);
    }

    public ChunkRadiationData(RadiationProfile air, RadiationProfile water, RadiationProfile soil, long lastDecayTick) {
        this.air = air;
        this.water = water;
        this.soil = soil;
        this.lastDecayTick = lastDecayTick;
    }

    public RadiationProfile air() { return air; }
    public RadiationProfile water() { return water; }
    public RadiationProfile soil() { return soil; }
    public long lastDecayTick() { return lastDecayTick; }

    public void setAir(RadiationProfile p) { this.air = p; markExpiryDirty(); }
    public void setWater(RadiationProfile p) { this.water = p; markExpiryDirty(); }
    public void setSoil(RadiationProfile p) { this.soil = p; markExpiryDirty(); }
    public void setLastDecayTick(long t) { this.lastDecayTick = t; }

    public void markExpiryDirty() { this.expiryDirty = true; }

    public long expiryGameTime() {
        if (expiryDirty) {
            double floor = RadiationConfig.ACTIVITY_FLOOR_BQ.get();
            long ea = air.isEmpty()   ? Long.MIN_VALUE : air.expiryTick(floor);
            long ew = water.isEmpty() ? Long.MIN_VALUE : water.expiryTick(floor);
            long es = soil.isEmpty()  ? Long.MIN_VALUE : soil.expiryTick(floor);
            expiryGameTime = Math.max(ea, Math.max(ew, es));
            expiryDirty = false;
        }
        return expiryGameTime;
    }

    public boolean isExpired(long now) {
        return !isEmpty() && now >= expiryGameTime();
    }

    public void clearIfExpired(long now) {
        if (!isExpired(now)) return;
        this.air = RadiationProfile.empty();
        this.water = RadiationProfile.empty();
        this.soil = RadiationProfile.empty();
        this.lastDecayTick = now;
        markExpiryDirty();
    }

    public double totalActivityBq() {
        return air.totalActivityBq() + water.totalActivityBq() + soil.totalActivityBq();
    }

    public boolean isEmpty() {
        return air.isEmpty() && water.isEmpty() && soil.isEmpty();
    }
}
