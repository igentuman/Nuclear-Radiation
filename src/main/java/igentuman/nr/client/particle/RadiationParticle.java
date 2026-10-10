package igentuman.nr.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class RadiationParticle extends SingleQuadParticle {

    private static final float FULL_QUAD_SIZE = 0.45F;
    private final float targetSize;

    protected RadiationParticle(ClientLevel level, double x, double y, double z, net.minecraft.client.renderer.texture.TextureAtlasSprite sprite) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sprite);
        this.gravity = -0.2F;
        this.hasPhysics = false;
        this.lifetime = 40 + this.random.nextInt(20);
        this.targetSize = FULL_QUAD_SIZE * (0.6F + this.random.nextFloat() * 0.4F);
        this.quadSize = 0.11F;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.alpha = 0.9F;
        this.xd = (this.random.nextDouble() - 0.5) * 0.02;
        this.yd = (this.random.nextDouble() - 0.5) * 0.02;
        this.zd = (this.random.nextDouble() - 0.5) * 0.02;
    }

    @Override
    public void tick() {
        this.oRoll = this.roll;
        super.tick();
        float progress = (float) this.age / (float) this.lifetime;
        if (progress < 0.5F) {
            this.quadSize = Mth.lerp(progress * 2.0F, 0.01F, this.targetSize);
        }
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        return this.quadSize;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

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
            double zSpeed,
            net.minecraft.util.RandomSource random
        ) {
            return new RadiationParticle(level, x, y, z, this.sprites.get(random));
        }
    }
}
