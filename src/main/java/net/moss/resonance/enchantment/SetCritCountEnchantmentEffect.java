package net.moss.resonance.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.moss.resonance.Resonance;

public record SetCritCountEnchantmentEffect(EnchantmentLevelBasedValue critCount) implements EnchantmentEntityEffect {
	public static final MapCodec<SetCritCountEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			EnchantmentLevelBasedValue.CODEC.fieldOf("crit_count").forGetter(SetCritCountEnchantmentEffect::critCount)
	).apply(instance, SetCritCountEnchantmentEffect::new));

	public SetCritCountEnchantmentEffect(EnchantmentLevelBasedValue critCount) { this.critCount = critCount; }
	@Override public MapCodec<SetCritCountEnchantmentEffect> getCodec() { return CODEC; }

	@Override
	public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
		if (context.stack().contains(Resonance.STORED_CRITS)) { context.stack().set(Resonance.STORED_CRITS, (int) this.critCount.getValue(level)); }
	}
}