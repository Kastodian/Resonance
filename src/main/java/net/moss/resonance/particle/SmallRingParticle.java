package net.moss.resonance.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class SmallRingParticle extends SpriteBillboardParticle {
	private final SpriteProvider sprites;
	private static Quaternionf QUATERNION = new Quaternionf(0F, 0F, 0F, 1F);

	private SmallRingParticle(ClientWorld world, double x, double y, double z, double velX, double velY, double velZ, SpriteProvider sprites) {
		super(world, x, y, z);
		this.maxAge = 7;
		this.scale = 0f;

		velX = Math.sqrt(velX * velX);

        SmallRingParticle.QUATERNION.x = (float) (Math.sin(velZ/2) * Math.cos(velY/2) * Math.cos(velX/2) - Math.cos(velZ/2) * Math.sin(velY/2) * Math.sin(velX/2));
        SmallRingParticle.QUATERNION.y = (float) (Math.cos(velZ/2) * Math.sin(velY/2) * Math.cos(velX/2) + Math.sin(velZ/2) * Math.cos(velY/2) * Math.sin(velX/2));
        SmallRingParticle.QUATERNION.z = (float) (Math.cos(velZ/2) * Math.cos(velY/2) * Math.sin(velX/2) - Math.sin(velZ/2) * Math.sin(velY/2) * Math.cos(velX/2));
        SmallRingParticle.QUATERNION.w = (float) (Math.cos(velZ/2) * Math.cos(velY/2) * Math.cos(velX/2) + Math.sin(velZ/2) * Math.sin(velY/2) * Math.sin(velX/2));

		this.sprites = sprites;
		this.setSpriteForAge(sprites);
	}

	@Override
	public void tick() {
		this.scale = (float) MathHelper.lerp(0.1, scale, 2);
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
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        this.alpha = 1.0f - MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge, 0.0f, 1.0f);
        this.buildGeometry(vertexConsumer, camera, tickDelta, QUATERNION);
        this.buildGeometry(vertexConsumer, camera, tickDelta, QUATERNION.rotateX(3.1415f));
    }

    private void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta, Quaternionf rotator) {
        Vec3d vec3 = camera.getPos();
		float x = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3.getX());
		float y = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3.getY());
		float z = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3.getZ());

		Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
		float f4 = this.getSize(tickDelta);

		for(int i = 0; i < 4; ++i) {
			Vector3f vector3f = vector3fs[i];
			vector3f.rotate(rotator);
			vector3f.mul(f4);
			vector3f.add(x, y, z);
		}

		float f7 = this.getMinU();
		float f8 = this.getMaxU();
		float f5 = this.getMinV();
		float f6 = this.getMaxV();
		int light = this.getBrightness(tickDelta);
		vertexConsumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).texture(f8, f6).color(this.red, this.green, this.blue, this.alpha).light(light);
		vertexConsumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).texture(f8, f5).color(this.red, this.green, this.blue, this.alpha).light(light);
		vertexConsumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).texture(f7, f5).color(this.red, this.green, this.blue, this.alpha).light(light);
		vertexConsumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).texture(f7, f6).color(this.red, this.green, this.blue, this.alpha).light(light);
    }

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Environment(EnvType.CLIENT)
	public record Factory(SpriteProvider sprites) implements ParticleFactory<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velX, double velY, double velZ) {
			return new SmallRingParticle(world, x, y, z, velX, velY, velZ, sprites);
		}
	}
}