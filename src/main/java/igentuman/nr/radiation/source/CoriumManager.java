package igentuman.nr.radiation.source;

import igentuman.nr.NuclearRadiation;
import igentuman.nr.block.CoriumFluidBlock;
import igentuman.nr.config.CoriumConfig;
import igentuman.nr.radiation.storage.CoriumSavedData;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;

/**
 * Server-side driver for corium hazard behaviour. Iterates tracked corium positions on a
 * throttled interval and, per source block, burns nearby entities, melts the block below
 * (weighted by hardness), and randomly solidifies the source into {@code corium_block}.
 * Positions are supplied by {@link CoriumFluidBlock} via {@link CoriumSavedData}.
 */
@EventBusSubscriber(modid = NuclearRadiation.MODID)
public final class CoriumManager {

    private CoriumManager() {}

    public static void track(ServerLevel level, BlockPos pos) {
        CoriumSavedData.getOrCreate(level).track(pos.asLong());
    }

    public static void untrack(ServerLevel level, BlockPos pos) {
        CoriumSavedData data = CoriumSavedData.get(level);
        if (data != null) {
            data.untrack(pos.asLong());
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        int interval = Math.max(1, CoriumConfig.CORIUM_TICK_INTERVAL.get());
        if (level.getGameTime() % interval != 0) return;

        CoriumSavedData data = CoriumSavedData.get(level);
        if (data == null || data.isEmpty()) return;

        double radius = CoriumConfig.CORIUM_BURN_RADIUS.get();
        int fireSeconds = CoriumConfig.CORIUM_FIRE_SECONDS.get();
        double damage = CoriumConfig.CORIUM_DAMAGE.get();
        double meltRate = CoriumConfig.CORIUM_MELT_RATE.get();
        double flowingMeltMultiplier = CoriumConfig.CORIUM_FLOWING_MELT_MULTIPLIER.get();
        int solidifyTicks = Math.max(interval, CoriumConfig.CORIUM_SOLIDIFY_TICKS.get());
        double solidifyChance = (double) interval / solidifyTicks;
        int coolThreshold = CoriumConfig.CORIUM_WATER_COOLING.get();

        Block solid = Corium.CORIUM_BLOCK.get();
        RandomSource rnd = level.getRandom();
        IntOpenHashSet burned = new IntOpenHashSet();

        for (long key : data.snapshot()) {
            BlockPos pos = BlockPos.of(key);
            if (!level.isLoaded(pos)) continue;

            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof CoriumFluidBlock)) {
                data.untrack(key);
                continue;
            }
            boolean isSource = state.getFluidState().isSource();

            int contacts = coolWithWater(level, pos);
            if (contacts > 0) {
                int cooled = data.addCooling(key, contacts);
                if (solid != Blocks.AIR && cooled >= coolThreshold) {
                    level.setBlock(pos, solid.defaultBlockState(), Block.UPDATE_ALL);
                    continue;
                }
            }

            if (isSource && radius > 0) {
                burnEntities(level, pos, radius, fireSeconds, damage, burned);
            }
            if (meltRate > 0) {
                meltBelow(level, pos, isSource ? meltRate : meltRate * flowingMeltMultiplier, rnd);
            }
            if (isSource && solid != Blocks.AIR && rnd.nextDouble() < solidifyChance) {
                level.setBlock(pos, solid.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private static void burnEntities(ServerLevel level, BlockPos pos, double radius, int fireSeconds, double damage, IntOpenHashSet burned) {
        AABB box = new AABB(pos).inflate(radius);
        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;
        double r2 = radius * radius;
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box);
        for (LivingEntity e : entities) {
            if (e.isSpectator()) continue;
            if (!burned.add(e.getId())) continue;
            if (e.distanceToSqr(cx, cy, cz) > r2) continue;
            if (fireSeconds > 0) e.igniteForSeconds(fireSeconds);
            if (damage > 0) e.hurt(level.damageSources().lava(), (float) damage);
        }
    }

    private static void meltBelow(ServerLevel level, BlockPos pos, double meltRate, RandomSource rnd) {
        BlockPos below = pos.below();
        if (!level.isLoaded(below)) return;

        BlockState state = level.getBlockState(below);
        if (state.isAir()) return;
        if (state.getBlock() instanceof CoriumFluidBlock) return;
        if (!state.getFluidState().isEmpty()) return;

        float hardness = state.getDestroySpeed(level, below);
        if (hardness < 0) return; // unbreakable (bedrock, barrier)

        double chance = meltRate / (hardness*1.5 + 1.0);
        if (rnd.nextDouble() < Math.min(1.0, chance)) {
            level.removeBlock(below, false);
        }
    }

    private static int coolWithWater(ServerLevel level, BlockPos pos) {
        int contacts = 0;
        for (Direction dir : Direction.values()) {
            BlockPos np = pos.relative(dir);
            if (!level.isLoaded(np)) continue;
            if (level.getBlockState(np).is(Blocks.WATER)) {
                level.setBlockAndUpdate(np, Blocks.AIR.defaultBlockState());
                level.sendParticles(ParticleTypes.LARGE_SMOKE,
                        np.getX() + 0.5, np.getY() + 0.5, np.getZ() + 0.5,
                        6, 0.2, 0.2, 0.2, 0.02);
                contacts++;
            }
        }
        if (contacts > 0) {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                    0.6F, 2.4F + level.getRandom().nextFloat() * 0.4F);
        }
        return contacts;
    }
}
