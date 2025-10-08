package net.moss.resonance.potioneffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.Utils;

public class DoubleJumpEffect extends StatusEffect {
	public DoubleJumpEffect() {
		super(StatusEffectCategory.BENEFICIAL, 0x1FACF3);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
		if (entity.isOnGround()) {
			entity.removeStatusEffect(Resonance.NORMAL_DOUBLE_JUMP);
			entity.removeStatusEffect(Resonance.STRONG_DOUBLE_JUMP);
		}
		return true;
	}

	public static void performDoubleJump(ServerPlayerEntity player) {
		Resonance.DOUBLE_JUMP_CRITERION.trigger(player);
		player.getWorld().playSound(null, player.getBlockPos(), Resonance.DOUBLE_JUMP_SOUND.value(), SoundCategory.PLAYERS, 0.15f, 1);
		RegistryEntry<StatusEffect> effectType = player.hasStatusEffect(Resonance.STRONG_DOUBLE_JUMP) ? Resonance.STRONG_DOUBLE_JUMP : Resonance.NORMAL_DOUBLE_JUMP;
		Utils.decrementStatusEffectAmplifiers(player, effectType, null);
	}
}