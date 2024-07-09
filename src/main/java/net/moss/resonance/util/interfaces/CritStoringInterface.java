package net.moss.resonance.util.interfaces;

import net.minecraft.item.ItemStack;
import net.moss.resonance.Resonance;

public interface CritStoringInterface {
    default public int getStoredCrits(ItemStack stack) {
        if (!stack.contains(Resonance.STORED_CRITS)) { return 0; }
        return stack.get(Resonance.STORED_CRITS);
    }
    
    default public void setStoredCrits(ItemStack stack, int critCount) {
        stack.set(Resonance.STORED_CRITS, critCount);
    }
    
    default public void critStreak(ItemStack stack, int critCount) {
        setStoredCrits(stack, critCount);
    }

    default public void critDeath(ItemStack stack, int critCount) { }
}
