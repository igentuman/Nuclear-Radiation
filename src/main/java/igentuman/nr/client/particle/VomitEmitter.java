package igentuman.nr.client.particle;

import igentuman.nr.NuclearRadiation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = NuclearRadiation.MODID, value = Dist.CLIENT)
public final class VomitEmitter {

    private VomitEmitter() {}

    private static final int BURST_TICKS = 3;
    private static final int PARTICLES_PER_TICK = 3;

    private static final List<Emission> ACTIVE = new ArrayList<>();

    private static final class Emission {
        final int entityId;
        final int stage;
        int ticksLeft;
        Emission(int entityId, int stage, int ticksLeft) {
            this.entityId = entityId;
            this.stage = stage;
            this.ticksLeft = ticksLeft;
        }
    }

    public static void add(int entityId, int stage) {
        ACTIVE.add(new Emission(entityId, stage, BURST_TICKS));
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            ACTIVE.clear();
            return;
        }
        Iterator<Emission> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Emission e = it.next();
            Entity entity = level.getEntity(e.entityId);
            if (!(entity instanceof LivingEntity living)) {
                it.remove();
                continue;
            }
            emit(level, living, e.stage);
            if (--e.ticksLeft <= 0) it.remove();
        }
    }

    private static void emit(ClientLevel level, LivingEntity entity, int stage) {
        Vec3 eye = entity.getEyePosition();
        Vec3 look = entity.getLookAngle();
        double ox = eye.x + look.x * 0.45;
        double oy = eye.y - 0.1;
        double oz = eye.z + look.z * 0.45;
        var rand = entity.getRandom();
        float[] color = stageColor(stage);
        for (int i = 0; i < PARTICLES_PER_TICK; i++) {
            double px = ox + (rand.nextDouble() - 0.5) * 0.17;
            double py = oy - (rand.nextDouble() - 0.5) * 0.65;
            double pz = oz + (rand.nextDouble() - 0.5) * 0.17;
            float f = (1.0f + rand.nextFloat()) / 2f;
            level.addParticle(NuclearRadiation.VOMIT_PARTICLE.get(),
                    px, py, pz, color[0] * f, color[1] * f, color[2] * f);
        }
    }

    private static float[] stageColor(int stage) {
        return switch (stage) {
            case 1  -> new float[]{0.70f, 0.80f, 0.10f};
            case 2  -> new float[]{0.55f, 0.65f, 0.05f};
            case 3  -> new float[]{0.65f, 0.35f, 0.05f};
            default -> new float[]{0.55f, 0.10f, 0.05f};
        };
    }
}
