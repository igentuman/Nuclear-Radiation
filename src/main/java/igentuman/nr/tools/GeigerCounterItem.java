package igentuman.nr.tools;

import igentuman.nr.simulation.SubChunkRadVector;
import igentuman.nr.simulation.RadiationSimulator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class GeigerCounterItem extends Item {

    public GeigerCounterItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel server)) return;
        if (!(entity instanceof LivingEntity living)) return;
        if (!selected && !(entity instanceof Player p && p.getOffhandItem() == stack)) return;
        if ((server.getGameTime() % 20) != 0) return;

        double bq = readBq(server, living);
        if (bq < 1e-9) return;

        float clickRate = (float) Math.min(2.0, 0.3 + Math.log10(1.0 + bq) * 0.2);
        server.playSound(null, living.blockPosition(), SoundEvents.NOTE_BLOCK_HAT.value(),
                SoundSource.PLAYERS, 0.4f, clickRate);
    }

    public static double readBq(ServerLevel level, LivingEntity entity) {
        return readBq(level, entity, 1.0, 1.0);
    }

    public static double readBq(ServerLevel level, LivingEntity entity,
                                double xrayPass, double neutronPass) {
        ChunkPos cp = entity.chunkPosition();
        int cy = entity.blockPosition().getY() >> 4;
        SubChunkRadVector v = RadiationSimulator.get().getChunkVector(level, cp, cy);
        if (v == null || v.isEmpty()) return 0.0;
        double ex = entity.getX();
        double ey = entity.getY();
        double ez = entity.getZ();
        double bq = 0.0;
        for (int d = 0; d < SubChunkRadVector.DIR_COUNT; d++) {
            double apexX = v.xRayBq[d];
            double apexN = v.neutronBq[d];
            if (apexX <= 0 && apexN <= 0) continue;
            double dx = v.tip[d].x - ex;
            double dy = v.tip[d].y - ey;
            double dz = v.tip[d].z - ez;
            double tipEyeDist2 = dx * dx + dy * dy + dz * dz;
            double scale = (v.tipApexDist2[d] + 1.0) / (tipEyeDist2 + 1.0);
            bq += (apexX * xrayPass + apexN * neutronPass) * scale;
        }
        return bq;
    }
}
