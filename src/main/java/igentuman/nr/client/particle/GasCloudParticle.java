package igentuman.nr.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GasCloudParticle extends TextureSheetParticle {

    private static final float FULL_QUAD_SIZE = 1.85F;
    private final float targetSize;

    protected GasCloudParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.gravity = 0.001F;
        this.hasPhysics = false;
        this.lifetime = 80 + this.random.nextInt(20);
        this.targetSize = FULL_QUAD_SIZE;
        this.quadSize = FULL_QUAD_SIZE;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.alpha = 0.47F;
        this.xd = (this.random.nextDouble() - 0.5) * 0.02;
        this.yd = (this.random.nextDouble() - 0.5) * 0.02;
        this.zd = (this.random.nextDouble() - 0.5) * 0.02;
        this.setSprite(sprites.get(this.random));
    }

    @Override
    public void tick() {
        this.oRoll = this.roll;
        super.tick();

    }

    @Override
    public float getQuadSize(float scaleFactor) {
        return this.quadSize;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
            SimpleParticleType type,
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed
        ) {
            return new GasCloudParticle(level, x, y, z, this.sprites);
        }
    }
}
