package igentuman.nr.tools;

import igentuman.nr.simulation.ChunkRadVector;
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
        if (bq <= 0.0) return;

        float clickRate = (float) Math.min(2.0, 0.3 + Math.log10(1.0 + bq) * 0.2);
        server.playSound(null, living.blockPosition(), SoundEvents.NOTE_BLOCK_HAT.value(),
                SoundSource.PLAYERS, 0.4f, clickRate);
    }

    public static double readBq(ServerLevel level, LivingEntity entity) {
        ChunkPos cp = entity.chunkPosition();
        ChunkRadVector v = RadiationSimulator.get().getChunkVector(level, cp);
        if (v == null) return 0.0;
        double dx = entity.getX() - (cp.x * 16.0 + 8.0);
        double dz = entity.getZ() - (cp.z * 16.0 + 8.0);
        double bqXRay = Math.max(0.0, v.centerScalarXRay + v.gradientXRay.x * dx + v.gradientXRay.z * dz);
        double bqNeutron = Math.max(0.0, v.centerScalarNeutron + v.gradientNeutron.x * dx + v.gradientNeutron.z * dz);
        return bqXRay + bqNeutron;
    }
}
