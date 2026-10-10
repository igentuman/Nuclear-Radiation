package igentuman.nr.compat.megaton;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.api.shielding.ArmorProtectionRegistry;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.radiation.storage.EntityRadiationData;
import igentuman.nr.radiation.storage.NRAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Bridges MEGATON 1.5.2's persistent fallout zones and one-off doses into NR. */
public final class MegatonRadiationBridge {
    // MEGATON's "rad" is a gameplay value, not a measured absorbed dose. NR also
    // applies a 72x display/effect gain to continuous exposure. Keep these two
    // conversions separate so ordinary fallout does not instantly become lethal.
    private static final double FALLOUT_SV_PER_GAME_RAD = 1.0e-6;
    private static final double DIRECT_SV_PER_GAME_RAD = 1.0e-4;
    private static final double RAD_DISPLAY_PER_SV = 50.0; // 400 on MEGATON's meter at NR's 8 Sv lethal career threshold.
    private static final boolean PRESENT = ModList.get().isLoaded("nuclearcraft");
    private static final Method DATA_GET;
    private static final Method DOSE_AT;
    private static final Method CONFIG_FLAG;
    private static final Object RADIATION_FLAG;
    private static volatile boolean falloutDisabled;

    static {
        Method dataGet = null;
        Method doseAt = null;
        Method configFlag = null;
        Object radiationFlag = null;
        if (PRESENT) {
            try {
                Class<?> data = Class.forName("com.nuclearcraft.system.RadiationData");
                Class<?> config = Class.forName("com.nuclearcraft.NuclearConfig");
                Class<?> booleanValue = Class.forName("com.nuclearcraft.config.ConfigSpec$BooleanValue");
                dataGet = data.getMethod("get", ServerLevel.class);
                doseAt = data.getMethod("doseAt", Vec3.class, long.class);
                configFlag = config.getMethod("flag", booleanValue);
                Field field = config.getField("RADIATION");
                radiationFlag = field.get(null);
            } catch (ReflectiveOperationException ex) {
                NuclearRadiation.LOGGER.warn("MEGATON radiation API differs from 1.5.2; fallout bridge disabled", ex);
                dataGet = null;
                doseAt = null;
                configFlag = null;
                radiationFlag = null;
            }
        }
        DATA_GET = dataGet;
        DOSE_AT = doseAt;
        CONFIG_FLAG = configFlag;
        RADIATION_FLAG = radiationFlag;
    }

    private MegatonRadiationBridge() {}

    /** Returns MEGATON's zone rate only. NR binds its carried items separately. */
    public static double zoneRate(ServerLevel level, Vec3 position) {
        if (DATA_GET == null || falloutDisabled || !RadiationConfig.masterEnabled()) return 0.0;
        try {
            if (!((Boolean) CONFIG_FLAG.invoke(null, RADIATION_FLAG))) return 0.0;
            Object data = DATA_GET.invoke(null, level);
            float rate = (Float) DOSE_AT.invoke(data, position, level.getGameTime());
            return Float.isFinite(rate) ? Math.max(0.0, rate) : 0.0;
        } catch (ReflectiveOperationException | ClassCastException ex) {
            NuclearRadiation.LOGGER.warn("Unable to sample MEGATON fallout", ex);
            falloutDisabled = true;
            return 0.0;
        }
    }

    public static double falloutSvPerGameRad() { return FALLOUT_SV_PER_GAME_RAD; }

    /** MEGATON uses this for blast, tsunami and irradiated-ground contact doses. */
    public static void directDose(LivingEntity entity, float rads) {
        if (!RadiationConfig.masterEnabled() || !Float.isFinite(rads) || rads <= 0.0F || entity.level().isClientSide()) return;
        double protection = ArmorProtectionRegistry.summed(entity).xray();
        EntityRadiationData data = entity.getData(NRAttachments.ENTITY_RADIATION.get());
        data.addSv(rads * DIRECT_SV_PER_GAME_RAD * (1.0 - protection));
    }

    /** Keep MEGATON's Geiger display linked to the surviving NR dose store. */
    public static float displayedRads(LivingEntity entity) {
        double sv = entity.getData(NRAttachments.ENTITY_RADIATION.get()).svTotalCareer();
        return (float) Math.min(400.0, Math.max(0.0, sv * RAD_DISPLAY_PER_SV));
    }
}
