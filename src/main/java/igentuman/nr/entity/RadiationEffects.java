package igentuman.nr.entity;

import igentuman.nr.NRSounds;
import igentuman.nr.config.GeneralConfig;
import igentuman.nr.config.RadiationConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import static net.minecraft.core.particles.ParticleTypes.SNEEZE;
import static net.minecraft.core.particles.ParticleTypes.SPIT;

public final class RadiationEffects {

    private RadiationEffects() {}

    /**
     * Dose stage (radiation "phase") for the given rates, 0 = below the mild threshold up to
     * 4 = lethal. Factors in accumulated career dose the same way {@link #apply} does, so callers
     * (e.g. the KubeJS dose-phase event) see the exact stage that drives harm effects.
     */
    public static int computeStage(double svPerHour, double svTotalCareer) {
        double mild = RadiationConfig.THRESHOLD_MILD.get();
        double mod  = RadiationConfig.THRESHOLD_MODERATE.get();
        double sev  = RadiationConfig.THRESHOLD_SEVERE.get();
        double leth = RadiationConfig.THRESHOLD_LETHAL.get();
        double k = RadiationConfig.TOTAL_SV_SCALE_K.get();

        double ratio = svTotalCareer / k;
        double effectiveSvPerHour = svPerHour * (1.0 + ratio * ratio);

        if (effectiveSvPerHour >= leth) return 4;
        if (effectiveSvPerHour >= sev) return 3;
        if (effectiveSvPerHour >= mod) return 2;
        if (effectiveSvPerHour >= mild) return 1;
        return 0;
    }

    public static void apply(LivingEntity entity, double svPerHour, double svTotalCareer) {
        if (EntityIgnoreFilter.shouldSkipHarm(entity)) return;
        boolean isPlayer = entity instanceof Player;
        if (isPlayer ? !GeneralConfig.RADIATION_HARM_EFFECTS_PLAYERS.get()
                     : !GeneralConfig.RADIATION_HARM_EFFECTS_MOBS.get()) return;

        int stage = computeStage(svPerHour, svTotalCareer);
        if (stage <= 0) return;

        if (stage >= 1) {
            addEffect(entity, MobEffects.CONFUSION, 10, 0);
            addEffect(entity, MobEffects.DIG_SLOWDOWN, 200, 1);
        }
        if (stage >= 2) {
            addEffect(entity, MobEffects.WEAKNESS, 10, 0);
            addEffect(entity, MobEffects.MOVEMENT_SLOWDOWN, 10, 0);
            entity.hurt(entity.damageSources().magic(), 0.5f);
        }
        if (stage >= 3) {
            addEffect(entity, MobEffects.BLINDNESS, 10, 0);
            entity.hurt(entity.damageSources().magic(), 1.5f);
        }
        if (stage >= 4) {
            addEffect(entity, MobEffects.WITHER, 10, 0);
            entity.hurt(entity.damageSources().magic(), 20.0f);
        }

        triggerVomit(entity, stage);
    }

    private static final String VOMIT_COOLDOWN_KEY = "nr_next_vomit_tick";
    private static final String VOMIT_BURST_KEY = "nr_vomit_burst_remaining";
    private static final String VOMIT_STAGE_KEY = "nr_vomit_burst_stage";
    private static final int VOMIT_INTERVAL_STAGE_1 = 12000;
    private static final int VOMIT_INTERVAL_STAGE_2 = 6000;
    private static final int VOMIT_INTERVAL_STAGE_3_4 = 2400;
    private static final int VOMIT_BURST_TICKS = 5;
    private static final int VOMIT_PARTICLES_PER_TICK = 2;

    private static void addEffect(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
                                  int duration, int amplifier) {
        entity.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, false));
    }

    private static void triggerVomit(LivingEntity entity, int stage) {
        Level level = entity.level();
        if (!(level instanceof ServerLevel server)) return;

        long now = server.getGameTime();
        CompoundTag data = entity.getPersistentData();

        int burstRemaining = data.getInt(VOMIT_BURST_KEY);
        if (burstRemaining > 0) {
            int burstStage = data.getInt(VOMIT_STAGE_KEY);
            spawnVomitParticles(server, entity, burstStage, VOMIT_PARTICLES_PER_TICK);
            data.putInt(VOMIT_BURST_KEY, burstRemaining - 1);
            return;
        }

        long next = data.getLong(VOMIT_COOLDOWN_KEY);
        if (now < next) return;

        int interval = switch (stage) {
            case 1 -> VOMIT_INTERVAL_STAGE_1;
            case 2 -> VOMIT_INTERVAL_STAGE_2;
            default -> VOMIT_INTERVAL_STAGE_3_4;
        };
        data.putLong(VOMIT_COOLDOWN_KEY, now + interval);
        data.putInt(VOMIT_BURST_KEY, VOMIT_BURST_TICKS - 1);
        data.putInt(VOMIT_STAGE_KEY, stage);

        spawnVomitParticles(server, entity, stage, VOMIT_PARTICLES_PER_TICK);
        server.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                NRSounds.VOMIT.get(), entity.getSoundSource(),
                1.0f, 0.9f + entity.getRandom().nextFloat() * 0.2f);
    }

    private static void spawnVomitParticles(ServerLevel server, LivingEntity entity, int stage, int count) {
        Vec3 eye = entity.getEyePosition();
        Vec3 look = entity.getLookAngle();
        double ox = eye.x + look.x * 0.45;
        double oy = eye.y - 0.1;
        double oz = eye.z + look.z * 0.45;
        var rand = entity.getRandom();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
        for (int i = 0; i < 1; i++) {
            ParticleOptions particle = stage >= 3
                    ? new DustParticleOptions(new Vector3f(0.75f, 0.05f, 0.05f), 1.4f)
                    : new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ROTTEN_FLESH));
            double spreadR = (rand.nextDouble() - 0.45) * 5.5;
            double spreadU = (rand.nextDouble() - 0.45) * 5.5;
            double speedMul = 0.07 + rand.nextDouble() * 0.01;
            double vx = (look.x + right.x * spreadR) * speedMul;
            double vy = (look.y + spreadU) * speedMul - 0.25;
            double vz = (look.z + right.z * spreadR) * speedMul;
            double px = ox + (rand.nextDouble() - 0.5) * 0.17;
            double py = oy - (rand.nextDouble() - 0.5) * 0.05;
            double pz = oz + (rand.nextDouble() - 0.5) * 0.17;
            sendParticles(server, entity, particle, px, py, pz, 0, vx, vy, vz, rand.nextDouble() - 0.5);
        }
    }

    private static final double PARTICLE_BROADCAST_RADIUS = 32.0;
    private static final double PARTICLE_BROADCAST_RADIUS_SQ = PARTICLE_BROADCAST_RADIUS * PARTICLE_BROADCAST_RADIUS;

    public static <T extends ParticleOptions> void sendParticles(ServerLevel level, LivingEntity entity, T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {
        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(type, true, posX, posY, posZ, (float)xOffset, (float)yOffset, (float)zOffset, (float)speed, particleCount);
        double ex = entity.getX();
        double ey = entity.getY();
        double ez = entity.getZ();
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(ex, ey, ez) <= PARTICLE_BROADCAST_RADIUS_SQ) {
                player.connection.send(packet);
            }
        }
    }



    @SuppressWarnings("unused")
    private static DamageSource source(LivingEntity entity) {
        return entity.damageSources().magic();
    }
}
