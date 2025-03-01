package net.moss.resonance.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.AbstractDustParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;

@Environment(EnvType.CLIENT)
public class VentSmokeParticle extends AbstractDustParticle<VentSmoke> {
	protected VentSmokeParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, boolean thrown, VentSmoke parameters, SpriteProvider spriteProvider) {
		super(world, x, y, z, velocityX, velocityY, velocityZ, parameters, spriteProvider);
		float f = this.random.nextFloat() * 0.4F + 0.6F;
		this.red = this.darken(parameters.getColor().x(), f);
		this.green = this.darken(parameters.getColor().y(), f);
		this.blue = this.darken(parameters.getColor().z(), f);
		this.alpha = 1;
		this.scale(parameters.getThrown() ? 2 : 1.5F);
		this.setBoundingBoxSpacing(0.25F, 0.25F);
		this.maxAge = this.random.nextInt(50) + 280;

		this.gravityStrength = parameters.getThrown() ? 3.0E-3F : 3.0E-6F;
		this.velocityX = velocityX;
		this.velocityY = velocityY + (parameters.getThrown() ? 0 : (double)(this.random.nextFloat() / 500.0F));
		this.velocityZ = velocityZ;
	}

	@Override
	public void tick() {
		this.prevPosX = this.x;
		this.prevPosY = this.y;
		this.prevPosZ = this.z;
		if (this.age++ < this.maxAge && !(this.alpha <= 0.0F)) {
			this.velocityX = this.velocityX + (double)(this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
			this.velocityZ = this.velocityZ + (double)(this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
			this.scale += 0.003;
			this.velocityY = this.velocityY - (double)this.gravityStrength;
			this.move(this.velocityX, this.velocityY, this.velocityZ);
			if (this.age >= this.maxAge - 60 && this.alpha > 0.02F) {
				this.alpha -= 0.015F;
			}
		} else {
			this.markDead();
		}
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Environment(EnvType.CLIENT)
	public static class Factory implements ParticleFactory<VentSmoke> {
		private final SpriteProvider spriteProvider;

		public Factory(SpriteProvider spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		public Particle createParticle(VentSmoke dustParticleEffect, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
			VentSmokeParticle ventSmokeParticle = new VentSmokeParticle(clientWorld, d, e, f, g, h, i, false, dustParticleEffect, this.spriteProvider);
			ventSmokeParticle.setSprite(this.spriteProvider);
			return ventSmokeParticle;
		}
	}
}