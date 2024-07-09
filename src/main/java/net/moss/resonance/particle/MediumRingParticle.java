package net.moss.resonance.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class MediumRingParticle extends SpriteBillboardParticle {
	static final Random RANDOM = Random.create();
    private final SpriteProvider sprites;

    MediumRingParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider sprites) {
    	super(world, x, y, z);
		this.maxAge = 5;
		this.scale = 0f;
		this.sprites = sprites;
		this.setSpriteForAge(sprites);
	}

	@Override
	public void tick() {
		this.scale = (float) MathHelper.lerp(0.3, scale, 2);
		this.setAlpha(1.0F - (float) this.age / (float) this.maxAge);
		if(this.age++ >= this.maxAge) {
			this.markDead();
		}
		else {
			this.setSpriteForAge(sprites);
		}
	}

	@Override
    public int getBrightness(float tint) {
        float f = ((float)this.age + tint) / (float)this.maxAge;
        f = MathHelper.clamp(f, 0.0f, 1.0f);
        int i = super.getBrightness(tint);
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        if ((j += (int)(f * 15.0f * 16.0f)) > 240) {
            j = 240;
        }
        return j | k << 16;
    }
    
    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Factory(SpriteProvider sprites) implements ParticleFactory<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velX, double velY, double velZ) {
			MediumRingParticle ringParticle = new MediumRingParticle(world, x, y, z, velX, velY, velZ, sprites);
            ringParticle.setVelocity(0, 0, 0);
			return ringParticle;
		}
	}
}