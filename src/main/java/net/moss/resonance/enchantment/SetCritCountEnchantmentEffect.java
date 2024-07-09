package net.moss.resonance.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.moss.resonance.util.interfaces.CritStoringInterface;

public record SetCritCountEnchantmentEffect(EnchantmentLevelBasedValue critCount) implements EnchantmentEntityEffect {
    public static final MapCodec<SetCritCountEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(EnchantmentLevelBasedValue.CODEC.fieldOf("crit_count").forGetter(SetCritCountEnchantmentEffect::critCount)).apply(instance, SetCritCountEnchantmentEffect::new);
    });

    public SetCritCountEnchantmentEffect(EnchantmentLevelBasedValue critCount) {
        this.critCount = critCount;
    }

    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        if (context.stack().getItem() instanceof CritStoringInterface interface1) { interface1.setStoredCrits(context.stack(), (int) this.critCount.getValue(level)); }
    }

    public MapCodec<SetCritCountEnchantmentEffect> getCodec() {
        return CODEC;
    }

    public EnchantmentLevelBasedValue critCount() {
        return this.critCount;
    }
}
