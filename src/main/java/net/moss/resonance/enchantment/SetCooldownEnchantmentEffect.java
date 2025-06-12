package net.moss.resonance.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.moss.resonance.util.EnchantUtils;

public record SetCooldownEnchantmentEffect(EnchantmentLevelBasedValue cooldown) implements EnchantmentEntityEffect {
    public static final MapCodec<SetCooldownEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(EnchantmentLevelBasedValue.CODEC.fieldOf("cooldown").forGetter(SetCooldownEnchantmentEffect::cooldown)).apply(instance, SetCooldownEnchantmentEffect::new);
    });

    public SetCooldownEnchantmentEffect(EnchantmentLevelBasedValue cooldown) {
        this.cooldown = cooldown;
    }

    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        if (user instanceof PlayerEntity playerEntity) { EnchantUtils.setCooldownTime(context.stack(), playerEntity, this.cooldown.getValue(level)); }
    }

    public MapCodec<SetCooldownEnchantmentEffect> getCodec() {
        return CODEC;
    }

    public EnchantmentLevelBasedValue cooldown() {
        return this.cooldown;
    }
}
