package net.moss.resonance.particle;

import org.joml.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.dynamic.Codecs;

public class VentSmoke extends AbstractDustParticleEffect {
	private final Vector3f color;
	private final boolean thrown;
	protected static final Codec<Boolean> THROWN_CODEC = Codec.BOOL.validate(thrown -> thrown != null ? DataResult.success(thrown) : DataResult.error(() -> "Value musn't be null: " + thrown));
	public static final MapCodec<VentSmoke> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codecs.VECTOR_3F.fieldOf("color").forGetter(effect -> effect.color),
			SCALE_CODEC.fieldOf("scale").forGetter(AbstractDustParticleEffect::getScale),
			THROWN_CODEC.fieldOf("thrown").forGetter(effect -> effect.thrown)
	).apply(instance, VentSmoke::new));
	public static final PacketCodec<RegistryByteBuf, VentSmoke> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.VECTOR3F, effect -> effect.color,
			PacketCodecs.FLOAT, AbstractDustParticleEffect::getScale,
			PacketCodecs.BOOL, effect -> effect.thrown,
			VentSmoke::new);

	public VentSmoke(Vector3f color, float scale, boolean thrown) {
		super(scale);
		this.color = color;
		this.thrown = thrown;
	}

	@Override public ParticleType<?> getType() { return ModParticles.VENT_SMOKE; }
	public Vector3f getColor() { return this.color; }
	public Boolean getThrown() { return this.thrown; }
}