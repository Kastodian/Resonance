package net.moss.resonance.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.AbstractDustParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class VentSmokeParticle extends AbstractDustParticle<VentSmoke> {
	protected final boolean waterbound;
	protected VentSmokeParticle(ClientWorld world, double x, double y, double z, double velX, double velY, double velZ, VentSmoke parameters, SpriteProvider spriteProvider) {
		super(world, x, y, z, velX, velY, velZ, parameters, spriteProvider);
		float f = this.random.nextFloat() * 0.4F + 0.6F;
		this.red = this.darken(parameters.getColor().x(), f);
		this.green = this.darken(parameters.getColor().y(), f);
		this.blue = this.darken(parameters.getColor().z(), f);
		this.alpha = 1;
		this.scale(parameters.getThrown() ? 2 : 1.5F);
		this.setBoundingBoxSpacing(0.25F, 0.25F);
		this.maxAge = this.random.nextInt(50) + 280;
		this.waterbound = parameters.getWaterbound();
		this.gravityStrength = parameters.getThrown() ? 3.0E-3F : 3.0E-6F;
		this.velocityX = velX;
		this.velocityY = velY + (parameters.getThrown() ? 0 : this.random.nextDouble() / 500.0);
		this.velocityZ = velZ;
	}

	@Override
	public void tick() {
		if (this.alpha <= 0.02F) { this.markDead(); return; }
		if (this.age++ >= this.maxAge - 60) { this.alpha -= 0.015F; }
		this.prevPosX = this.x;
		this.prevPosY = this.y;
		this.prevPosZ = this.z;
		this.velocityX += this.random.nextDouble() / 5000.0 - 0.0001;
		this.velocityY -= (double)this.gravityStrength;
		this.velocityZ += this.random.nextDouble() / 5000.0 - 0.0001;
		this.scale += 0.003;
		this.move(this.velocityX, this.velocityY, this.velocityZ);

		if (this.waterbound && this.age % 10 == 0 && !this.world.getFluidState(BlockPos.ofFloored(this.x, this.y, this.z)).isIn(FluidTags.WATER)) {
			this.velocityY = 0;
		}
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Environment(EnvType.CLIENT)
	public record Factory(SpriteProvider sprites) implements ParticleFactory<VentSmoke> {
		public Particle createParticle(VentSmoke ventSmoke, ClientWorld clientWorld, double x, double y, double z, double velX, double velY, double velZ) {
			VentSmokeParticle ventSmokeParticle = new VentSmokeParticle(clientWorld, x, y, z, velX, velY, velZ, ventSmoke, sprites);
			ventSmokeParticle.setSprite(sprites);
			return ventSmokeParticle;
		}
	}
}