package igentuman.nr.compat.ntm;

import igentuman.nr.config.NtmCompatConfig;
import igentuman.nr.config.RadiationConfig;
import igentuman.nr.api.shielding.ArmorProtectionRegistry.Protection;
import igentuman.nr.radiation.storage.EntityRadiationData;
import igentuman.nr.radiation.storage.NRAttachments;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.binding.RadiationBindings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

/** Common entry point. No NTM classes are linked when the optional mod is absent. */
public final class NtmCompatibility {
    private static volatile Boolean configuredEnabled;

    private NtmCompatibility() {}

    public static boolean enabled() {
        if (!ModList.get().isLoaded("hbm") || !NtmCompatConfig.SPEC.isLoaded()) return false;
        Boolean value = configuredEnabled;
        if (value == null) {
            synchronized (NtmCompatibility.class) {
                if (configuredEnabled == null) configuredEnabled = NtmCompatConfig.ENABLED.get();
                value = configuredEnabled;
            }
        }
        return value;
    }

    public static void migrate(LivingEntity entity, double nativeDose) {
        if (entity.level().isClientSide()) return;
        double legacyDose = state(entity).migrate(nativeDose);
        data(entity).addSv(NtmUnits.doseToSv(legacyDose, NtmCompatConfig.SV_PER_DOSE_UNIT.get()));
    }

    public static double dose(LivingEntity entity) {
        return NtmUnits.doseFromSv(data(entity).svTotalCareer(), NtmCompatConfig.SV_PER_DOSE_UNIT.get());
    }

    public static void setDose(LivingEntity entity, double dose) {
        if (entity.level().isClientSide() || !Double.isFinite(dose)) return;
        data(entity).setSvTotalCareer(NtmUnits.doseToSv(dose, NtmCompatConfig.SV_PER_DOSE_UNIT.get()));
    }

    public static void incrementDose(LivingEntity entity, double amount) {
        if (entity.level().isClientSide() || !Double.isFinite(amount)) return;
        // Keep treatment available with the master switch off, but do not add exposure.
        if (amount > 0.0 && !RadiationConfig.masterEnabled()) return;
        double sv = NtmUnits.incrementToSv(amount, NtmCompatConfig.SV_PER_DOSE_UNIT.get(),
                NtmCompatConfig.SV_PER_EXPOSURE_UNIT.get());
        data(entity).addSv(sv);
        state(entity).recordExposure(sv);
    }

    public static double exposureRate(LivingEntity entity) {
        return NtmUnits.exposureFromDisplayedRate(data(entity).svPerHourAmbient(),
                NtmCompatConfig.SV_PER_EXPOSURE_UNIT.get());
    }

    /** Already added by the NTM API: include in rate display, never in dose a second time. */
    public static double drainExposure(LivingEntity entity) {
        return enabled() ? state(entity).drainExposure() : 0.0;
    }

    public static double drainAmbientExposure(LivingEntity entity) {
        return enabled() ? state(entity).drainAmbientExposure() : 0.0;
    }

    public static void recordAmbientExposure(LivingEntity entity, double raw, double absorbed) {
        if (entity.level().isClientSide() || !RadiationConfig.masterEnabled()) return;
        double difference = raw - absorbed;
        if (difference > 0.0 && Double.isFinite(difference)) {
            state(entity).recordAmbientCorrection(difference * NtmCompatConfig.SV_PER_EXPOSURE_UNIT.get());
        }
    }

    public static Protection armorProtection(ItemStack stack) {
        return enabled() ? NtmArmorAdapter.piece(stack) : Protection.NONE;
    }

    public static double additionalProtection(LivingEntity entity) {
        return enabled() ? NtmArmorAdapter.additionalProtection(entity) : 0.0;
    }

    public static boolean radiationImmune(LivingEntity entity) {
        return enabled() && NtmArmorAdapter.immune(entity);
    }

    public static double claddingProtection(ItemStack stack) {
        return enabled() ? NtmArmorAdapter.claddingProtection(stack) : 0.0;
    }

    /** Imported NTM item radiation for stacks without an explicit NR component or binding. */
    public static RadiationProfile itemProfile(ItemStack stack) {
        return enabled() ? NtmItemBridge.profile(stack) : RadiationProfile.empty();
    }

    public static void invalidateItemProfiles() {
        if (enabled()) NtmItemBridge.invalidate();
    }

    public static void itemTagsUpdated() {
        if (enabled()) NtmItemBridge.tagsUpdated();
    }

    public static void retainNativeItemRadiation(Runnable nativeCall) {
        if (enabled()) NtmItemBridge.retainNative(nativeCall);
        else nativeCall.run();
    }

    public static boolean nativeItemRadiationRetained() {
        return NtmItemBridge.nativeRetained();
    }

    /** Native reacher reduction for imported item profiles; 1 for explicit NR profiles or without a reacher. */
    public static boolean hasReacher(LivingEntity entity) {
        return enabled() && NtmItemBridge.hasReacher(entity);
    }

    public static double reacherScale(ItemStack stack) {
        return enabled() && !RadiationBindings.explicit(stack) ? NtmItemBridge.reacherScale(stack) : 1.0;
    }

    public static boolean carriedItemMenu(AbstractContainerMenu menu) {
        return enabled() && NtmItemBridge.carriedItemMenu(menu);
    }

    /** Imported NTM placed hazard-block radiation for blocks without an explicit NR binding. */
    public static RadiationProfile blockProfile(BlockState state) {
        return enabled() ? NtmBlockBridge.profile(state) : RadiationProfile.empty();
    }

    /**
     * NR emits this block's radiation (explicit binding, or imported hazard block with NR block radiation
     * enabled), so native per-block world releases must not also run.
     */
    public static boolean nrOwnsBlockEmission(BlockState state) {
        if (RadiationBindings.explicit(state)) return true;
        return enabled() && RadiationConfig.blockRadiationEnabled() && !NtmBlockBridge.profile(state).isEmpty();
    }

    /** Native BlockHazard area exposure; retained natively because it is not part of the imported profile. */
    public static float nativeBlockAreaRadiation(BlockState state) {
        return enabled() ? NtmBlockBridge.areaRadiation(state.getBlock()) : 0.0F;
    }

    public static RadiationProfile worldProfile(ServerLevel level, BlockPos pos) {
        return enabled() ? NtmWorldBridge.profile(level, pos) : RadiationProfile.empty();
    }

    public static void tickWorld(ServerLevel level) {
        if (enabled()) NtmWorldBridge.tick(level);
    }

    public static boolean worldExposureAllowed(LivingEntity entity) {
        return enabled() && NtmWorldBridge.mayExpose(entity);
    }

    public static void observeWorldExposure(LivingEntity entity, double rawSv, double absorbedSv) {
        if (enabled() && absorbedSv > 0.0) NtmWorldBridge.observeExposure(entity, rawSv, absorbedSv);
    }

    /**
     * Ends native NTM timed contamination (NR medicine, clear command, respawn). Native storage and ticking
     * stay authoritative otherwise. Returns the number of removed effects.
     */
    public static int clearTimedContamination(LivingEntity entity) {
        if (!enabled() || entity.level().isClientSide()) return 0;
        return NtmContamination.clear(entity);
    }

    public static EntityRadiationData data(LivingEntity entity) {
        return entity.getData(NRAttachments.ENTITY_RADIATION.get());
    }

    private static NtmEntityState state(LivingEntity entity) {
        return entity.getData(NRAttachments.NTM_ENTITY_STATE.get());
    }
}
