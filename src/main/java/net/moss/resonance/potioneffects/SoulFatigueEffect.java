package net.moss.resonance.potioneffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.moss.resonance.Resonance;

public class SoulFatigueEffect extends StatusEffect {
  public SoulFatigueEffect() {
    super(StatusEffectCategory.HARMFUL, 0x00DFFF);
  }
  
  @Override
  public boolean canApplyUpdateEffect(int duration, int amplifier) {
    int i = 60 / (amplifier + 1);
    if (i > 0) {
      return duration % i == 0;
    }
    return true;
  }
  
  @Override
  public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
    entity.damage(entity.getDamageSources().wither(), (amplifier / 3) + 1);
    if (entity instanceof PlayerEntity) {
      ((PlayerEntity)entity).addExhaustion((float)(amplifier + 1));
    }
    if (amplifier > 0) {
      entity.addStatusEffect(new StatusEffectInstance(Resonance.SOUL_FATIGUE, 15 * 20, amplifier - 1));
    }
    return true;
  }
}