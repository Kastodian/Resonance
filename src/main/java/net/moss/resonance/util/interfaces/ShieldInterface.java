package net.moss.resonance.util.interfaces;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public interface ShieldInterface {
	int getCoolDownTicks();
	default public void onParry(ItemStack stack, LivingEntity user, @Nullable DamageSource source, float amount, Hand hand) { return; };
}