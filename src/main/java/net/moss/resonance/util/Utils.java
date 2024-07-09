package net.moss.resonance.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.function.SetEnchantmentsLootFunction;
import net.minecraft.loot.function.SetPotionLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

public class Utils {
	public static DamageSource damageSource(World world, RegistryKey<DamageType> key) { return damageSource(world, key, null);	}
	public static DamageSource damageSource(World world, RegistryKey<DamageType> key, @Nullable LivingEntity attacker) { return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), attacker); }

    public static int[] IntListToArray(List<Integer> list) { return list.stream().mapToInt(Integer::intValue).toArray(); }
    public static List<Integer> ArrayToIntList(int[] array) { return Arrays.stream(array).boxed().collect(Collectors.toList()); }

    public static LootPool lootPoolBuilder(float rolls, float conditionally, Item item, float countMin, float countMax) {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(rolls))
                .conditionally(RandomChanceLootCondition.builder(conditionally))
                .with(ItemEntry.builder(item))
                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(countMin, countMax)).build())
				.build();
    }

    public static LootPool enchantLootPoolBuilder(float rolls, float conditionally, Item item, RegistryEntry<Enchantment> enchant, int level) {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(rolls))
                .conditionally(RandomChanceLootCondition.builder(conditionally))
                .with(ItemEntry.builder(item))
                .apply(new SetEnchantmentsLootFunction.Builder().enchantment(enchant, ConstantLootNumberProvider.create(level)))
				.build();
    }

    public static LootPool potionLootPoolBuilder(float rolls, float conditionally, Item item, float countMin, float countMax, RegistryEntry<Potion> potion) {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(rolls))
                .conditionally(RandomChanceLootCondition.builder(conditionally))
                .with(ItemEntry.builder(item))
                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(countMin, countMax)).build())
				.apply(SetPotionLootFunction.builder(potion))
				.build();
    }

	public static boolean isItemEquipped(LivingEntity entity, Item item, boolean checkHands) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.isArmorSlot() || checkHands) {
				if (entity.getEquippedStack(slot).getItem() == item) { return true; }
			}
		}
		
		// TODO: EMI SUPPORT
		// if (Resonance.isTrinketsInstalled) {
		// 	Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(entity);
		// 	if (optional.isPresent()) {
		// 		if (optional.get().isEquipped(item)) { return true; }
		// 	}
		// }
		return false;
	}

	public static TypedActionResult<ItemStack> trinketHotswap(World world, PlayerEntity user, Hand hand, Item item) {
		// TODO: EMI SUPPORT
		// if (Resonance.isTrinketsInstalled) {
		//     ItemStack stack = user.getStackInHand(hand);
		//     if (TrinketItem.equipItem(user, stack)) { TypedActionResult.success(stack, world.isClient()); }
		// }
		if (item instanceof Equipment equipment) { return equipment.equipAndSwap(item, world, user, hand); }
		return null;
	}

	public static List<ItemStack> getAllEquipped(LivingEntity entity, boolean checkHands) {
		List<ItemStack> items = new ArrayList<>();
		if (!checkHands) { entity.getAllArmorItems().forEach(item -> items.add(item)); }
		else { entity.getEquippedItems().forEach(item -> items.add(item)); }
			
		// TODO: EMI SUPPORT
		// if (Resonance.isTrinketsInstalled) {
		// 	Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(entity);
		// 	if (optional.isPresent()) {
		// 		List<Pair<SlotReference, ItemStack>> equipped = optional.get().getAllEquipped();
		// 		for (Pair<SlotReference,ItemStack> pair : equipped) {
		// 			items.add(pair.getRight());
		// 		}
		// 	}
		// }
		return items;
	}

	public static void sumStatusEffectAmplifiers(LivingEntity entity, StatusEffectInstance additionalEffect, int cap) {
		RegistryEntry<StatusEffect> effectType = additionalEffect.getEffectType();
		if (entity.hasStatusEffect(effectType)) {
			int newAmplifier = entity.getStatusEffect(effectType).getAmplifier() + additionalEffect.getAmplifier() + 1;
			if (newAmplifier <= cap) {
				StatusEffectInstance resultEffect = new StatusEffectInstance(effectType, additionalEffect.getDuration(), newAmplifier);
				entity.addStatusEffect(resultEffect);
			}
		} else { entity.addStatusEffect(additionalEffect); }
	}
	
	public static void decrementStatusEffectAmplifiers(LivingEntity entity, RegistryEntry<StatusEffect> effectType, @Nullable Integer duration) {
		if (entity.hasStatusEffect(effectType)) {
			StatusEffectInstance effectInstance = entity.getStatusEffect(effectType);
			int newAmplifier = effectInstance.getAmplifier() -1;
			entity.removeStatusEffect(effectType);
			if (newAmplifier >= 0 && newAmplifier < 256) {
				if (duration == null) { duration = effectInstance.getDuration(); }
				StatusEffectInstance resultEffect = new StatusEffectInstance(effectType, duration, newAmplifier, effectInstance.isAmbient(), effectInstance.shouldShowParticles(), effectInstance.shouldShowIcon());
				entity.addStatusEffect(resultEffect);	
			}
		}
	}

	public static boolean isAttackCritical(PlayerEntity player, Entity target) {
		return player.getAttackCooldownProgress(0.5f) > 0.9f && player.fallDistance > 0.0f && !player.isOnGround() && !player.isClimbing() && !player.isTouchingWater() && !player.hasStatusEffect(StatusEffects.BLINDNESS) && !player.hasVehicle() && target instanceof LivingEntity && !player.isSprinting();
	}

	public static void customKnockback(LivingEntity entity, Vec3d source, float strength, Vec3d plus, Vec3d dampenMultipliers, @Nullable Float clamp) {
		strength *= 1.0 - entity.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
		if (strength <= 0.0) { return; }
		entity.velocityDirty = true;
		Vec3d vec3d = entity.getPos().add(0, entity.getHeight()/2, 0).subtract(source).normalize().multiply(strength).add(plus).multiply(dampenMultipliers).add(entity.getVelocity());
		if (clamp != null && vec3d.length() > clamp) { vec3d = vec3d.normalize().multiply(clamp); }
		entity.fallDistance /= Math.max(entity.getVelocity().getY() / vec3d.getY(), 1);
		entity.setVelocity(vec3d);
		if (entity instanceof ServerPlayerEntity player) { player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player)); }
	}

	public static void customKnockback(LivingEntity entity, Vec3d source, float strength) { customKnockback(entity, source, strength, new Vec3d(0, 0, 0), new Vec3d(1, 1, 1), null); }
	public static void customKnockbackPlus(LivingEntity entity, Vec3d source, float strength, Vec3d plus) { customKnockback(entity, source, strength, plus, new Vec3d(1, 1, 1), null); }
	public static void customKnockbackClamp(LivingEntity entity, Vec3d source, float strength, @Nullable Float clamp) {	customKnockback(entity, source, strength, new Vec3d(0, 0, 0), new Vec3d(1, 1, 1), clamp); }
	public static void customKnockbackDampen(LivingEntity entity, Vec3d source, float strength, Vec3d dampenMultipliers) { customKnockback(entity, source, strength, new Vec3d(0, 0, 0), dampenMultipliers, null); }

    public static EntityHitResult entityRaycast(PlayerEntity player, float distance) {
        float f = player.getPitch();
        float g = player.getYaw();
        Vec3d vec3d = player.getEyePos();
        float j = -MathHelper.cos(-f * 0.017453292F);
        float k = MathHelper.sin(-f * 0.017453292F);
        float l = MathHelper.sin(-g * 0.017453292F - 3.1415927F) * j;
        float n = MathHelper.cos(-g * 0.017453292F - 3.1415927F) * j;
        Vec3d vec3d2 = vec3d.add(l * distance, k * distance, n * distance);
        return ProjectileUtil.raycast(player, vec3d, vec3d2, new Box(vec3d, vec3d2), (entity) -> { return !entity.isSpectator() && entity.canHit(); }, (double) distance);
    }

    public static BlockHitResult blockRaycast(World world, PlayerEntity player, float distance, RaycastContext.FluidHandling fluidHandling) {
        Vec3d vec3d = player.getEyePos();
        Vec3d vec3d2 = vec3d.add(player.getRotationVector(player.getPitch(), player.getYaw()).multiply(distance));
        return world.raycast(new RaycastContext(vec3d, vec3d2, ShapeType.OUTLINE, fluidHandling, player));
    }

    public static HitResult raycast(World world, PlayerEntity player, float distance, RaycastContext.FluidHandling fluidHandling) {
        EntityHitResult entityHitResult = entityRaycast(player, distance);
        if (entityHitResult != null && entityHitResult.getEntity() != null) { return entityHitResult; }
        BlockHitResult blockHitResult = blockRaycast(world, player, distance, fluidHandling);
        return blockHitResult;
    }

    public static HitResult raycast(World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling) { return raycast(world, player, (float) player.getBlockInteractionRange(), fluidHandling); }
    public static HitResult raycast(World world, PlayerEntity player, float distance) { return raycast(world, player, distance, RaycastContext.FluidHandling.ANY); }
    public static HitResult raycast(World world, PlayerEntity player) { return raycast(world, player, (float) player.getBlockInteractionRange(), RaycastContext.FluidHandling.ANY); }

	public static List<Entity> entitiesInRadius(World world, Vec3d pos, @Nullable Entity except, float searchRadius, float searchHeight, Predicate<? super Entity> predicate) {
		return world.getOtherEntities(except, new Box(pos.getX() - searchRadius, pos.getY() - searchHeight, pos.getZ() - searchRadius, pos.getX() + searchRadius, pos.getY() + searchHeight, pos.getZ() + searchRadius), predicate);
	}

	public static boolean canMobSpawn(World world, BlockPos pos) {
		if (!world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP)) { return false; }
        int i = world.getDimension().monsterSpawnBlockLightLimit();
        if (world.getLightLevel(LightType.BLOCK, pos) > i) { return false; }
		return true;
	}

	public static final class CustomWindChargeBehavior extends ExplosionBehavior {
        private final Float knockback;
		public CustomWindChargeBehavior(Float knockback) { this.knockback = knockback; }
        @Override public float getKnockbackModifier(Entity entity) { return knockback; }
        @Override public boolean shouldDamage(Explosion explosion, Entity entity) { return false; }
        @Override public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
            if (blockState.isIn(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS)) { return Optional.of(Float.valueOf(3600000.0f)); }
            return Optional.empty();
        }
    }
}