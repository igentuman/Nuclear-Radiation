package igentuman.nr.util.persistence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class LungPollutionData {

    public static final Codec<LungPollutionData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.optionalFieldOf("pollution", 0.0).forGetter(d -> d.pollution)
    ).apply(inst, LungPollutionData::new));

    private double pollution;

    public LungPollutionData() { this(0.0); }

    public LungPollutionData(double pollution) {
        this.pollution = clamp(pollution);
    }

    public double pollution() { return pollution; }

    public void setPollution(double v) { this.pollution = clamp(v); }

    public void add(double delta) { setPollution(this.pollution + delta); }

    private static double clamp(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }
}
