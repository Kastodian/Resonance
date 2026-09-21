package net.moss.resonance.util.interfaces;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.moss.resonance.Resonance;

public interface CritStoringInterface {
	default public int maxCritCount(ItemStack stack) { return 255; }
	default public void critDeath(PlayerEntity attacker, Entity victim, int critStreak, Hand hand, ItemStack stack) { }

	default public void onCrit(PlayerEntity attacker, Entity victim, int critStreak, Hand hand, ItemStack stack) {
		stack.set(Resonance.STORED_CRITS, Math.min(stack.getOrDefault(Resonance.STORED_CRITS, 0) + 1, this.maxCritCount(stack)));
	}
}