package net.moss.resonance.util.interfaces;

import net.minecraft.item.ItemStack;

public interface ScarfInterface {
	default public int getSpecial(ItemStack stack) { return 0; }
	String getTexture();
}