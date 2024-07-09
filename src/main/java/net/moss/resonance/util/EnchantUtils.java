package net.moss.resonance.util;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.enchantment.effect.TargetedEnchantmentEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;

public class EnchantUtils {
	public static void recoilCheck(LivingEntity entity, @Nullable DamageSource damageSource, ItemStack stack, float strength, @Nullable Float clamp, boolean modifyCheck) {
		if (EnchantmentHelper.hasAnyEnchantmentsWith(stack, Resonance.HAS_RECOIL) && entity.getWorld() instanceof ServerWorld world) {
			Vec3d source = entity.getEyePos().add(entity.getRotationVec(1.0f));
			if (damageSource == null) { damageSource = Utils.damageSource(world, DamageTypes.INDIRECT_MAGIC, entity); }
			if (modifyCheck) { strength += 0.2f * EnchantmentHelper.modifyKnockback(world, stack, entity, damageSource, (float) entity.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK)); }
			Utils.customKnockbackClamp(entity, source, strength, clamp);
		}
	}

    public static void setCooldownTime(ItemStack stack, PlayerEntity user, float baseCooldown) {
        MutableFloat mutableFloat = new MutableFloat(baseCooldown);
        forEachEnchantment(user, (enchantment, level, context) -> modifyCooldownDuration(enchantment.value(), user.getRandom(), level, mutableFloat));
        user.getItemCooldownManager().set(stack.getItem(), MathHelper.floor(Math.max(0.0F, mutableFloat.floatValue()) * 20.0F));
    }

    public static float getWeightValue(ItemStack stack, PlayerEntity user, float baseWeight) {
        MutableFloat mutableFloat = new MutableFloat(baseWeight);
        forEachEnchantment(user, (enchantment, level, context) -> modifyWeight(enchantment.value(), user.getRandom(), level, mutableFloat));
        return Math.max(0.0F, mutableFloat.floatValue());
    }
    
	public static void onTargetCritDamaged(ServerWorld world, Entity target, DamageSource damageSource, @Nullable ItemStack weapon) {
        if (target instanceof LivingEntity livingEntity) {
            forEachEnchantment(livingEntity, (enchantment, level, context) -> postCrit(enchantment.value(), world, level, context, EnchantmentEffectTarget.VICTIM, target, damageSource));
        }
        if (weapon != null && damageSource.getAttacker() instanceof LivingEntity livingEntity) {
            forEachEnchantment(weapon, EquipmentSlot.MAINHAND, livingEntity, (enchantment, level, context) -> postCrit(enchantment.value(), world, level, context, EnchantmentEffectTarget.ATTACKER, target, damageSource));
        }
	}

	public static void onStackUsed(ServerWorld world, ItemStack stack, LivingEntity user, EquipmentSlot slot, java.util.function.Consumer<Item> onBreak) {
        EnchantmentEffectContext enchantmentEffectContext = new EnchantmentEffectContext(stack, slot, user, onBreak);
        forEachEnchantment(stack, EquipmentSlot.MAINHAND, user, (enchantment, level, context) -> {
            postUse(enchantment.value(), world, level, enchantmentEffectContext, user);
        });
	}



    /**
     * @deprecated This function needs to be removed or avoided at all costs
     */
    @Deprecated
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static RegistryEntry<Enchantment> getEnchant(World world, RegistryKey<Enchantment> enchant) {
		RegistryEntryLookup registryEntryLookup = world.getRegistryManager().createRegistryLookup().getOrThrow(RegistryKeys.ENCHANTMENT);
		return registryEntryLookup.getOrThrow(enchant);
	}





    private static void forEachEnchantment(LivingEntity entity, ContextAwareConsumer contextAwareConsumer) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            forEachEnchantment(entity.getEquippedStack(equipmentSlot), equipmentSlot, entity, contextAwareConsumer);
        }
    }

	@SuppressWarnings({ "rawtypes", "unchecked" })
    private static void forEachEnchantment(ItemStack stack, EquipmentSlot slot, LivingEntity entity, ContextAwareConsumer contextAwareConsumer) {
        if (stack.isEmpty()) { return; }
        ItemEnchantmentsComponent itemEnchantmentsComponent = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (itemEnchantmentsComponent == null || itemEnchantmentsComponent.isEmpty()) { return; }
        EnchantmentEffectContext enchantmentEffectContext = new EnchantmentEffectContext(stack, slot, entity);
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : itemEnchantmentsComponent.getEnchantmentEntries()) {
            RegistryEntry registryEntry = (RegistryEntry)entry.getKey();
            if (!((Enchantment)registryEntry.value()).slotMatches(slot)) continue;
            contextAwareConsumer.accept(registryEntry, entry.getIntValue(), enchantmentEffectContext);
        }
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void postCrit(Enchantment enchantment, ServerWorld world, int level, EnchantmentEffectContext context, EnchantmentEffectTarget target, Entity user, DamageSource damageSource) {
        for (TargetedEnchantmentEffect targetedEnchantmentEffect : enchantment.getEffect(Resonance.POST_CRIT)) {
            if (target != targetedEnchantmentEffect.enchanted()) continue;
            Enchantment.applyTargetedEffect(targetedEnchantmentEffect, world, level, context, user, damageSource);
        }
    }

	private static void postUse(Enchantment enchantment, ServerWorld world, int level, EnchantmentEffectContext context, Entity user) {
        applyEffects(enchantment.getEffect(Resonance.POST_USE), createEnchantedEntityLootContext(world, level, user, user.getPos()), (effect) -> {
            effect.apply(world, level, context, user, user.getPos());
        });
    }

    private static void modifyCooldownDuration(Enchantment enchantment, Random random, int level, MutableFloat baseCooldown) {
        enchantment.modifyValue(Resonance.COOLDOWN_TIME, random, level, baseCooldown);
    }

    private static void modifyWeight(Enchantment enchantment, Random random, int level, MutableFloat baseCooldown) {
        enchantment.modifyValue(Resonance.WEIGHT_VALUE, random, level, baseCooldown);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T> void applyEffects(List<EnchantmentEffectEntry<T>> entries, LootContext lootContext, Consumer<T> effectConsumer) {
        Iterator var3 = entries.iterator();
        while(var3.hasNext()) {
            EnchantmentEffectEntry<T> enchantmentEffectEntry = (EnchantmentEffectEntry)var3.next();
            if (enchantmentEffectEntry.test(lootContext)) {
                effectConsumer.accept(enchantmentEffectEntry.effect());
            }
        }
    }

    private static LootContext createEnchantedEntityLootContext(ServerWorld world, int level, Entity entity, Vec3d pos) {
        LootContextParameterSet lootContextParameterSet = (new LootContextParameterSet.Builder(world)).add(LootContextParameters.THIS_ENTITY, entity).add(LootContextParameters.ENCHANTMENT_LEVEL, level).add(LootContextParameters.ORIGIN, pos).build(LootContextTypes.ENCHANTED_ENTITY);
        return (new LootContext.Builder(lootContextParameterSet)).build(Optional.empty());
    }

    @FunctionalInterface
    static interface ContextAwareConsumer {
        public void accept(RegistryEntry<Enchantment> var1, int var2, EnchantmentEffectContext var3);
    }
}
