package net.moss.resonance.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class CustomGlowParticle extends SpriteBillboardParticle {
	static final Random RANDOM = Random.create();
    private final SpriteProvider sprites;

    CustomGlowParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider sprites) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.velocityMultiplier = 0.6f;
        //this.field_28787 = true;
        this.sprites = sprites;
        this.scale *= 0.75f;
        this.collidesWithWorld = false;
        this.setSpriteForAge(sprites);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

	@Override
	public void tick() {
        super.tick();
		this.setSpriteForAge(sprites);
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

    public record SparkFactory(SpriteProvider sprites) implements ParticleFactory<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velX, double velY, double velZ) {
			CustomGlowParticle sparkParticle = new CustomGlowParticle(world, x, y, z, velX, velY, velZ, sprites);
            sparkParticle.setVelocity(velX * 0.25, velY * 0.25, velZ * 0.25);
            sparkParticle.setMaxAge(world.random.nextInt(2) + 2);
			return sparkParticle;
		}
	}

    public record AmethystFactory(SpriteProvider sprites) implements ParticleFactory<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velX, double velY, double velZ) {
			CustomGlowParticle amethystParticle = new CustomGlowParticle(world, x, y, z, velX, velY, velZ, sprites);
            amethystParticle.setColor(0.7f, 0.56f, 0.95f);
            amethystParticle.setVelocity(velX * 0.25, velY * 0.25, velZ * 0.25);
            amethystParticle.setMaxAge(world.random.nextInt(2) + 2);
			return amethystParticle;
		}
	}

    public record GoldenGlitter(SpriteProvider sprites) implements ParticleFactory<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velX, double velY, double velZ) {
			CustomGlowParticle glitterParticle = new CustomGlowParticle(world, x, y, z, velX, velY, velZ, sprites);
            glitterParticle.setColor(1f, 1f, 0.6f);
            glitterParticle.setVelocity(0, 0, 0);
            glitterParticle.setMaxAge(world.random.nextInt(2) + 20);
			return glitterParticle;
		}
	}
}