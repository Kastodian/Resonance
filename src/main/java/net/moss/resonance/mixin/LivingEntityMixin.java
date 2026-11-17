package net.moss.resonance.mixin;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.ResonanceEvents;
import net.moss.resonance.util.EnchantUtils;
import net.moss.resonance.util.Utils;
import net.moss.resonance.util.interfaces.PlayerEntityInterface;
import net.moss.resonance.entity.LostSoulEntity;
import net.moss.resonance.entity.ModEntities;
import net.moss.resonance.item.CustomShield;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	private boolean echoScarfDamageHalf = false;

	@Inject(at = @At(value = "HEAD"), method = "getPreferredEquipmentSlot", cancellable = true)
	private void getPreferredEquipmentSlot(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
		if(stack.getItem() instanceof CustomShield) {
			cir.setReturnValue(EquipmentSlot.OFFHAND);
		}
	}

	@ModifyConstant(method = "isBlocking", constant = @Constant(intValue = 5))
	private int isBlocking(int value) {
		return 0;
	}

	@Inject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V", shift = At.Shift.AFTER))
	private void parry(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity defender = (LivingEntity)(Object) this;
		ItemStack activeItem = defender.getActiveItem();
		if (defender.getItemUseTimeLeft() >= activeItem.getItem().getMaxUseTime(activeItem, defender) - 8) {
			ResonanceEvents.SHIELD_PARRY.invoker().parry(defender, source, amount, defender.getActiveHand(), activeItem);
		}
	}

	@Inject(at = @At("HEAD"), method = "damage")
	private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity entity = (LivingEntity)(Object) this;
		if (entity.hurtTime > 0) { return; }
		Random random = entity.getRandom();
		World world = entity.getWorld();
		Optional<LivingEntity> attacker = (source.getAttacker() instanceof ProjectileEntity projectileEntity && projectileEntity.getOwner() instanceof LivingEntity ownerEntity) ? Optional.of(ownerEntity) : (source.getAttacker() instanceof LivingEntity livingEntity ? Optional.of(livingEntity) : Optional.empty());

		if (attacker.isPresent()) {
			for (ItemStack itemStack : Utils.getAllEquipped(entity, false)) {
				EnchantUtils.recoilCheck(entity, null, itemStack, 0.3f, 0.8f, false);
			}
		}

		if (source.isOf(DamageTypes.FALLING_ANVIL)) {
			entity.addStatusEffect(new StatusEffectInstance(Resonance.CRUSHING_WEIGHT, 30, 2));
		}

		if (!source.isOf(Resonance.REVERBERATION_DAMAGE) && Utils.isItemEquipped(entity, Resonance.ECHO_SCARF, false)) {
			ResonanceEvents.REVERBERATE.invoker().onResonate(world, entity.getEyePos(), new Vector3f(0f, entity.getHeadYaw() * MathHelper.DEGREES_PER_RADIAN, 0f), entity, entity, 2f, false);
		}

		if (world instanceof ServerWorld serverWorld && entity.hasStatusEffect(StatusEffects.WIND_CHARGED) && random.nextInt(1 + entity.getStatusEffect(StatusEffects.WIND_CHARGED).getAmplifier()) != 0) {
			double d = entity.getX();
			double e = entity.getY() + entity.getHeight() / 2.0f;
			double f = entity.getZ();
			float g = 3.0f + entity.getStatusEffect(StatusEffects.WIND_CHARGED).getAmplifier() * 2.0f;
			serverWorld.createExplosion(entity, null, AbstractWindChargeEntity.EXPLOSION_BEHAVIOR, d, e, f, g, false, World.ExplosionSourceType.TRIGGER, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, SoundEvents.ENTITY_BREEZE_WIND_BURST);
		}

		if (entity.hasStatusEffect(Resonance.SPARKING) && !source.isOf(Resonance.SPARKING_DAMAGE)) {
			world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), Resonance.ELECTRIC_SPARK_SOUND.value(), SoundCategory.BLOCKS, 0.1f, 1f);
			for (ItemStack itemStack : Utils.getAllEquipped(entity, true)) {
				if (itemStack.isIn(Resonance.CONDUCTIVE_ITEMS_TAG) || entity.getSteppingBlockState().isIn(Resonance.CONDUCTIVE_BLOCKS_TAG) || entity.getBlockStateAtPos().isIn(Resonance.CONDUCTIVE_BLOCKS_TAG) || entity.getActiveItem().isIn(Resonance.CONDUCTIVE_ITEMS_TAG)) {
					ResonanceEvents.STATIC_SHOCK.invoker().staticShock(entity, entity.getStatusEffect(Resonance.SPARKING).getAmplifier(), attacker.orElse(null));
					break;
				}
			}
			if (attacker.isPresent()) {
				for (ItemStack itemStack : attacker.get().getHandItems()) {
					if (itemStack.isIn(Resonance.CONDUCTIVE_ITEMS_TAG)) {
						ResonanceEvents.STATIC_SHOCK.invoker().staticShock(entity, entity.getStatusEffect(Resonance.SPARKING).getAmplifier(), attacker.get());
						break;
					}
				}
			}
		}
	}

	@ModifyVariable(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvulnerableTo(Lnet/minecraft/entity/damage/DamageSource;)Z"), argsOnly = true)
	private DamageSource modifyDamageSource(DamageSource source) {
		echoScarfDamageHalf = source.isOf(Resonance.REVERBERATION_DAMAGE) || (source.getAttacker() != null ? source.getAttacker().getType().isIn(Resonance.SOUND_SENSITIVE_TAG) : false);
		return source;
	}

	@ModifyVariable(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvulnerableTo(Lnet/minecraft/entity/damage/DamageSource;)Z"), argsOnly = true)
	private float modifyDamage(float amount) {
		LivingEntity entity = (LivingEntity)(Object) this;
		if (Utils.isItemEquipped(entity, Resonance.ECHO_SCARF, false) && echoScarfDamageHalf) { return amount /= 1.5; }
		if (Utils.isItemEquipped(entity, Resonance.SPIRIT_SCARF, false)) {
			float health = entity.getHealth();
			return health - amount > 1 || amount > health * 5 ? amount : (float) Math.clamp(1, (int) health - 1, (int) amount);
		}
		return amount;
	}

	@Inject(at = @At("HEAD"), method = "fall")
	private void staticShockFall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity)(Object) this;
		if (entity.getWorld() instanceof ServerWorld serverWorld && onGround) {
			if (entity.fallDistance > 0.0f && entity.hasStatusEffect(Resonance.SPARKING)) {
				for (ItemStack itemStack : Utils.getAllEquipped(entity, true)) {
					if (itemStack.isIn(Resonance.CONDUCTIVE_ITEMS_TAG)) {
						ResonanceEvents.STATIC_SHOCK.invoker().staticShock(entity, entity.getStatusEffect(Resonance.SPARKING).getAmplifier(), null);
						break;
					}
				}
			}
			if (entity instanceof PlayerEntity player) {
				((PlayerEntityInterface)player).endGroundPound(serverWorld, player, null);
			}
		}
	}

	@Inject(at = @At(value = "HEAD"), method = "canFreeze", cancellable = true)
	private void canFreeze(CallbackInfoReturnable<Boolean> cir) {
		if (Utils.isItemEquipped((LivingEntity)(Object) this, Resonance.SCARF, false)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(at = @At(value = "HEAD"), method = "getArmorVisibility", cancellable = true)
	private void getArmorVisibility(CallbackInfoReturnable<Float> cir) {
		LivingEntity entity = (LivingEntity)(Object) this;
		if (Utils.isItemEquipped(entity, Resonance.PHANTOM_SCARF, false) && entity.hasStatusEffect(StatusEffects.INVISIBILITY)) {
			cir.setReturnValue(0.0F);
		}
	}

	@Inject(at = @At(value = "HEAD"), method = "getJumpBoostVelocityModifier", cancellable = true)
	protected void getJumpBoostVelocityModifier(CallbackInfoReturnable<Float> cir) {
		LivingEntity entity = (LivingEntity)(Object) this;
		if (entity.hasStatusEffect(Resonance.STRONG_DOUBLE_JUMP)) { cir.setReturnValue(0.3F); }
		else if (entity.hasStatusEffect(Resonance.NORMAL_DOUBLE_JUMP)) { cir.setReturnValue(0.1F); }
	}

	@Inject(at = @At(value = "HEAD"), method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", cancellable = true)
	private void addStatusEffect(StatusEffectInstance effect, @Nullable Entity source, CallbackInfoReturnable<Void> ret) {
		if (source instanceof ServerPlayerEntity player) {
			Resonance.INFLICT_POTION_EFFECT_CRITERION.trigger(player, (LivingEntity)(Object) this, effect);
		}
	}

	@Inject(at = @At(value = "HEAD"), method = "onDeath")
	protected void onDeath(DamageSource damageSource, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity)(Object) this;
		if ((entity.getType().isIn(EntityTypeTags.SKELETONS) && entity.hasStatusEffect(Resonance.SOUL_FATIGUE)) || entity instanceof WitherSkeletonEntity) {
			World world = entity.getWorld();
			if (entity.getRandom().nextInt(5) - world.getDifficulty().getId() <= 0) {
				LostSoulEntity lostSoulEntity = new LostSoulEntity(ModEntities.LOST_SOUL, world);
				lostSoulEntity.setPosition(entity.getEyePos());
				lostSoulEntity.setVelocity(0, 0.1, 0);
				world.spawnEntity(lostSoulEntity);
			}
		}
	}
}