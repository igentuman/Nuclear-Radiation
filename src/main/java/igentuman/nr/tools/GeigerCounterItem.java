package igentuman.nr.tools;

import igentuman.nr.util.persistence.NRAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static igentuman.nr.NRSounds.GEIGER_TICK;

public class GeigerCounterItem extends Item {

    public GeigerCounterItem(Properties props) {
        super(props.stacksTo(1));
    }

    // Geiger response is driven by dose rate (Sv/h), not raw field activity (Bq):
    // a large Bq field can yield a low dose, so scaling on Bq pegs the meter at benign levels.
    public static final double SILENT_SVH = 1.0e-6;   // below ~1 µSv/h: background, silent
    private static final double RESP_LOG_LO = -6.0;   // 1 µSv/h   -> scale 0
    private static final double RESP_LOG_HI = -2.0;   // 10 mSv/h  -> scale 1 (full)
    public static final double CPM_PER_SVH = 1.75e8;  // ~SBM-20 tube: 1 µSv/h ≈ 175 cpm

    private static final int CLICK_INTERVAL_SLOW = 40;
    private static final int CLICK_INTERVAL_FAST = 1;

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel server)) return;
        if (!(entity instanceof LivingEntity living)) return;
        if (!selected && !(entity instanceof Player p && p.getOffhandItem() == stack)) return;

        double svh = living.getData(NRAttachments.ENTITY_RADIATION.get()).svPerHour();
        if (svh <= SILENT_SVH) return;

        double t = responseT(svh);
        if ((server.getGameTime() % clickIntervalTicks(t, server.getRandom())) != 0) return;
        float randomVariance = (server.getRandom().nextFloat() - 0.5f) * 0.1f;
        randomVariance += (float) (t / 10f);
        server.playSound(null, living.blockPosition(), GEIGER_TICK.get(),
                SoundSource.PLAYERS, 0.4f + randomVariance, clickPitch(t, server.getRandom()));
    }

    /** 0..1 geiger scale from dose rate (Sv/h), log-mapped between RESP_LOG_LO and RESP_LOG_HI. */
    public static double responseT(double svh) {
        if (svh <= 0) return 0.0;
        double t = (Math.log10(svh) - RESP_LOG_LO) / (RESP_LOG_HI - RESP_LOG_LO);
        return t < 0 ? 0 : (t > 1 ? 1 : t);
    }

    public static double svhToCpm(double svh) {
        return svh <= 0 ? 0.0 : svh * CPM_PER_SVH;
    }

    private static int clickIntervalTicks(double t, RandomSource random) {
        int baseInterval = (int) Math.round(CLICK_INTERVAL_SLOW - t * (CLICK_INTERVAL_SLOW - CLICK_INTERVAL_FAST));
        double variance = 0.3;
        int interval = (int) (baseInterval * (1.0 + (random.nextDouble() - 0.5) * 2.0 * variance));
        return Math.max(CLICK_INTERVAL_FAST, interval);
    }

    private static float clickPitch(double t, RandomSource random) {
        float basePitch = (float) (0.7 + t * 1.0001);
        float randomVariance = (random.nextFloat() - 0.5f) * 0.3f;
        return basePitch + randomVariance;
    }
}
