package net.moss.resonance.potioneffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.ItemStack;
import net.moss.resonance.Resonance;
import net.moss.resonance.event.StaticShockEvent;
import net.moss.resonance.util.Utils;

public class StaticEffect extends StatusEffect {
  public StaticEffect() {
    super(StatusEffectCategory.HARMFUL, 0xBF94E4);
  }
  
  @Override
  public boolean canApplyUpdateEffect(int duration, int amplifier) {
    return true;
  }
  
  @Override
  public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
    if (entity.getSteppingBlockState().isIn(Resonance.CONDUCTIVE_BLOCKS_TAG) || entity.getBlockStateAtPos().isIn(Resonance.CONDUCTIVE_BLOCKS_TAG)) {
      StaticShockEvent.STATIC_SHOCK.invoker().staticShock(entity, amplifier, null);
    } else if (entity.handSwinging || entity.isSprinting()) {
      for (ItemStack itemStack : Utils.getAllEquipped(entity, true)) {
        if (itemStack.isIn(Resonance.CONDUCTIVE_ITEMS_TAG)) {
          StaticShockEvent.STATIC_SHOCK.invoker().staticShock(entity, amplifier, null);
          return true;
        }
      }
    }
    return true;
  }
}