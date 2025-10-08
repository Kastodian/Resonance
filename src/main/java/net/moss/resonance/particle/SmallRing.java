package net.moss.resonance.particle;

import org.joml.Vector3f;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.dynamic.Codecs;

public class SmallRing extends AbstractDustParticleEffect {
	private final Vector3f rotation;
	public static final MapCodec<SmallRing> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codecs.VECTOR_3F.fieldOf("rotation").forGetter(effect -> effect.rotation),
			SCALE_CODEC.fieldOf("scale").forGetter(AbstractDustParticleEffect::getScale)
	).apply(instance, SmallRing::new));
	public static final PacketCodec<RegistryByteBuf, SmallRing> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.VECTOR3F, effect -> effect.rotation,
			PacketCodecs.FLOAT, AbstractDustParticleEffect::getScale,
			SmallRing::new);

	public SmallRing(Vector3f rotation, float scale) {
		super(scale);
		this.rotation = rotation;
	}

	@Override public ParticleType<?> getType() { return ModParticles.SMALL_RING_PARTICLE; }
	public Vector3f getRotation() { return this.rotation; }
}