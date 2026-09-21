package net.moss.resonance.entity;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveIntoWaterGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BrushItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.Resonance;
import net.moss.resonance.potioneffects.CrushingWeightEffect;

public class YetiCrabEntity extends WaterCreatureEntity implements Bucketable {
	private static final TrackedData<Boolean> FROM_BUCKET = DataTracker.registerData(YetiCrabEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private int hungerTicks;
	@Nullable private UUID lightningId;

	public YetiCrabEntity(EntityType<? extends WaterCreatureEntity> entityType, World world) {
		super(entityType, world);
		this.experiencePoints = 2;
	}

	@Override
	protected void initGoals() {
		super.initGoals();
		this.goalSelector.add(1, new MeleeAttackGoal(this, 0.25F, false));
		this.goalSelector.add(2, new MoveIntoWaterGoal(this));
		this.goalSelector.add(3, new WanderAroundGoal(this, 0.5F));
		this.goalSelector.add(4, new LookAroundGoal(this));
	}

	public void setHunger(int hungerTicks) { this.hungerTicks = hungerTicks; }
	public int getHunger() { return this.hungerTicks; }
	@Override public void setFromBucket(boolean fromBucket) { this.dataTracker.set(FROM_BUCKET, fromBucket); }
	@Override public boolean isFromBucket() { return this.dataTracker.get(FROM_BUCKET); }

	@Override
	public void onDamaged(DamageSource damageSource) {
		if (damageSource.getAttacker() instanceof LivingEntity livingEntity && livingEntity.isAttackable()) { this.setTarget(livingEntity); }
		super.onDamaged(damageSource);
	}

	@Override
	public boolean canSee(Entity entity) {
		return entity.getWorld() == this.getWorld() && entity.getPos().distanceTo(this.getPos()) <= 2.0;
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		if (this.hungerTicks > 0) {
			--this.hungerTicks;
		}
	}

	@Nullable
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
		this.setCanPickUpLoot(true);
		this.getAttributeInstance(EntityAttributes.GENERIC_SCALE).addPersistentModifier(new EntityAttributeModifier(RANDOM_SPAWN_BONUS_MODIFIER_ID, (world.getRandom().nextFloat() * 0.15F) - 0.15F, Operation.ADD_MULTIPLIED_BASE));
		return super.initialize(world, difficulty, spawnReason, entityData);
	}

	@Override
	protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
		if (random.nextFloat() < 0.05F) {
			this.equipStack(EquipmentSlot.MAINHAND, Items.SLIME_BALL.getDefaultStack());
		}
	}

	@Override
	public boolean isInvulnerableTo(DamageSource damageSource) {
		return damageSource.isOf(Resonance.VENT_PLUME_DAMAGE);
	}

	@Override
	public boolean addStatusEffect(StatusEffectInstance effect, Entity source) {
		return !(effect.getEffectType() instanceof CrushingWeightEffect);
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);
		Item item = itemStack.getItem();
		if (!(item instanceof BrushItem) || player.getItemCooldownManager().isCoolingDown(item)) {
			if (item == Items.WATER_BUCKET) {
				this.dropStack(this.getEquippedStack(EquipmentSlot.MAINHAND));
			}
			return Bucketable.tryBucket(player, hand, this).orElse(super.interactMob(player, hand));
		}
		boolean bl = false;
		if (this.getHunger() <= 0) {
			this.setHunger(6000);
			this.dropStack(Items.SLIME_BALL.getDefaultStack());
			bl = true;
		}
		if (!this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
			this.dropStack(this.getEquippedStack(EquipmentSlot.MAINHAND));
			this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
			bl = true;
		}
		if (bl) {
			this.emitGameEvent(GameEvent.ENTITY_INTERACT);
			itemStack.damage(4, player, getSlotForHand(hand));
			this.setTarget(player);
			return ActionResult.success(this.getWorld().isClient());
			// TODO : Sound Effects...
		}
		return ActionResult.PASS;
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(FROM_BUCKET, false);
	}

	public static DefaultAttributeContainer.Builder createVolcanoSnailAttributes() {
		return PathAwareEntity.createLivingAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 4f)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.5f)
				.add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.125f)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f)
				.add(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY, 2f)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 4)
				.add(EntityAttributes.GENERIC_ARMOR, 2.0f)
				.add(EntityAttributes.GENERIC_STEP_HEIGHT, 1f);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void copyDataToStack(ItemStack stack) {
		Bucketable.copyDataToStack(this, stack);
		NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, nbtCompound -> nbtCompound.putInt("Hunger", this.getHunger()));
		NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, nbtCompound -> nbtCompound.putBoolean("FromBucket", this.isFromBucket()));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void copyDataFromNbt(NbtCompound nbt) {
		Bucketable.copyDataFromNbt(this, nbt);
		this.setHunger(nbt.getInt("Hunger"));
		this.setFromBucket(nbt.getBoolean("FromBucket"));
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("FromBucket", this.isFromBucket());
		nbt.putInt("Hunger", this.hungerTicks);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		this.setFromBucket(nbt.getBoolean("FromBucket"));
		this.hungerTicks = nbt.getInt("Hunger");
	}

	@Override
	public ItemStack getBucketItem() {
		return new ItemStack(Resonance.YETI_CRAB_BUCKET);
	}

	@Override
	public SoundEvent getBucketFillSound() {
		return SoundEvents.ITEM_BUCKET_FILL;
	}
}