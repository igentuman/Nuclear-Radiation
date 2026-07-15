package igentuman.nr.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VomitParticle extends TextureSheetParticle {

    private static final float MIN_QUAD_SIZE = 0.14F;
    private static final float MAX_QUAD_SIZE = 0.27F;

    protected VomitParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.gravity = 1.5F;
        this.hasPhysics = true;
        this.lifetime = 60 + this.random.nextInt(20);
        this.quadSize = MIN_QUAD_SIZE;
        this.roll = 90;
        this.oRoll = this.roll;
        this.xd = (this.random.nextDouble() - 0.5) * 0.01;
        this.yd = (this.random.nextDouble() - 0.5) * 0.01;
        this.zd = (this.random.nextDouble() - 0.5) * 0.01;
        this.setSprite(sprites.get(this.random));
    }

    @Override
    public void tick() {
        this.oRoll = this.roll;
        super.tick();
        float t = (float) this.age / (float) this.lifetime;
        this.quadSize = Mth.lerp(t, MIN_QUAD_SIZE, MAX_QUAD_SIZE);
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
            VomitParticle p = new VomitParticle(level, x, y, z, this.sprites);
            if (xSpeed != 0.0 || ySpeed != 0.0 || zSpeed != 0.0) {
                p.setColor((float) xSpeed, (float) ySpeed, (float) zSpeed);
            }
            return p;
        }
    }
}
