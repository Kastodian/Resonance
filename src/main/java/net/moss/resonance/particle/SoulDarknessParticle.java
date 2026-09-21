package net.moss.resonance.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;

@Environment(EnvType.CLIENT)
public class SoulDarknessParticle extends SpriteBillboardParticle {
	static final Random RANDOM = Random.create();
	protected int lightLevel;
	protected BlockPos blockPos;

	SoulDarknessParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
		super(world, x, y, z);
		this.lightLevel = world.getDimension().monsterSpawnBlockLightLimit();
		this.blockPos = BlockPos.ofFloored(this.getBoundingBox().getMinPos());
		this.maxAge = 500;
		this.alpha = 0;
	}

	@Override
	public void tick() {
		if(this.age++ >= this.maxAge) {
			this.markDead();
		}
		if (this.random.nextFloat() < 0.05F && this.world.getLightLevel(LightType.BLOCK, this.blockPos) <= this.lightLevel) {
			this.world.addParticle(ParticleTypes.SOUL, blockPos.getX() + 1.5, blockPos.getY() + 0.1, blockPos.getZ() + 1.5, random.nextGaussian() * 0.01, 0, random.nextGaussian() * 0.01);
		}
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.NO_RENDER;
	}

	@Environment(EnvType.CLIENT)
	public record Factory(SpriteProvider sprites) implements ParticleFactory<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velX, double velY, double velZ) {
			return new SoulDarknessParticle(world, x, y, z, velX, velY, velZ);
		}
	}
}