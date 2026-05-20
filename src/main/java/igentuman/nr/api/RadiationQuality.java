package igentuman.nr.api;

public final class RadiationQuality {
    public static final RadiationQuality DEFAULT = new RadiationQuality(1.0f, 1.0f, 20.0f, 10.0f);

    public final float qXRay;
    public final float qBeta;
    public final float qAlpha;
    public final float qNeutron;

    public RadiationQuality(float qXRay, float qBeta, float qAlpha, float qNeutron) {
        this.qXRay = qXRay;
        this.qBeta = qBeta;
        this.qAlpha = qAlpha;
        this.qNeutron = qNeutron;
    }
}
