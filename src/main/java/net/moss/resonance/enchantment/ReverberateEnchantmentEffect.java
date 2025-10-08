package net.moss.resonance.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.moss.resonance.ResonanceEvents;

public record ReverberateEnchantmentEffect(EnchantmentLevelBasedValue strength) implements EnchantmentEntityEffect {
	public static final MapCodec<ReverberateEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			EnchantmentLevelBasedValue.CODEC.fieldOf("strength").forGetter(ReverberateEnchantmentEffect::strength)
	).apply(instance, ReverberateEnchantmentEffect::new));

	public ReverberateEnchantmentEffect(EnchantmentLevelBasedValue strength) { this.strength = strength; }
	@Override public MapCodec<ReverberateEnchantmentEffect> getCodec() { return CODEC; }

	@Override
	public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
		ResonanceEvents.REVERBERATE.invoker().onResonate(world, pos, pos.toVector3f(), null, null, this.strength.getValue(level), false);
	}
}