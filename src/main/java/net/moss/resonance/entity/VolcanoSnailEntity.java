package net.moss.resonance.entity;

import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.MoveIntoWaterGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BrushItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.Resonance;

public class VolcanoSnailEntity extends WaterCreatureEntity implements Bucketable {
	private static final TrackedData<Byte> SNAIL_FLAGS = DataTracker.registerData(VolcanoSnailEntity.class, TrackedDataHandlerRegistry.BYTE);
	private static final TrackedData<String> TYPE = DataTracker.registerData(VolcanoSnailEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<Boolean> HUNGRY = DataTracker.registerData(VolcanoSnailEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Byte> COLOR = DataTracker.registerData(VolcanoSnailEntity.class, TrackedDataHandlerRegistry.BYTE);
	private static final TrackedData<Boolean> FROM_BUCKET = DataTracker.registerData(VolcanoSnailEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	@Nullable private UUID lightningId;

	public VolcanoSnailEntity(EntityType<? extends WaterCreatureEntity> entityType, World world) {
		super(entityType, world);
		this.experiencePoints = 2;
	}

	@Override
	public float getPathfindingFavor(BlockPos pos, WorldView world) {
		return world.getBlockState(pos.down()).isIn(Resonance.VOLCANO_SNAIL_PREFERED_TAG) || world.getBlockState(pos).isIn(Resonance.VOLCANO_SNAIL_PREFERED_TAG) ? 15.0F : world.getBiome(pos) == Resonance.BATHYAL_ZONE ? 10.0F : world.getPhototaxisFavor(pos);
	}

	@Override
	protected void initGoals() {
		super.initGoals();
		this.goalSelector.add(1, new MoveIntoWaterGoal(this));
		this.goalSelector.add(2, new EscapeDangerGoal(this, 0.6f));
		this.goalSelector.add(3, new EatChemicalsGoal(this, 0.5f, 16));
		this.goalSelector.add(3, new WanderAroundGoal(this, 0.5f));
		this.goalSelector.add(4, new LookAroundGoal(this));
	}

	public void setVariant(VolcanoSnailEntity.Type type) { this.dataTracker.set(TYPE, type.name); }
	public void setHungry(Boolean hungry) { this.dataTracker.set(HUNGRY, hungry); }
	public void setColor(DyeColor color) { this.dataTracker.set(COLOR, (byte)(this.dataTracker.get(COLOR) & 240 | color.getId() & 15)); }
	public VolcanoSnailEntity.Type getVariant() { return VolcanoSnailEntity.Type.fromName(this.dataTracker.get(TYPE)); }
	public Boolean getHungry() { return this.dataTracker.get(HUNGRY); }
	public DyeColor getColor() { return DyeColor.byId(this.dataTracker.get(COLOR) & 15); }
	@Override public void setFromBucket(boolean fromBucket) { this.dataTracker.set(FROM_BUCKET, fromBucket); }
	@Override public boolean isFromBucket() { return this.dataTracker.get(FROM_BUCKET); }

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putString("Type", this.getVariant().asString());
		nbt.putBoolean("Hungry", this.dataTracker.get(HUNGRY));
		nbt.putBoolean("FromBucket", this.isFromBucket());
		nbt.putByte("Color", (byte)this.getColor().getId());
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		this.setVariant(VolcanoSnailEntity.Type.fromName(nbt.getString("Type")));
		this.setHungry(nbt.getBoolean("Hungry"));
		this.setFromBucket(nbt.getBoolean("FromBucket"));
		this.setColor(DyeColor.byId(nbt.getByte("Color")));
	}

	@Override
	public boolean isInvulnerableTo(DamageSource damageSource) {
		return super.isInvulnerableTo(damageSource) || damageSource.isIn(DamageTypeTags.IS_FIRE);
	}

	public static DyeColor generateDefaultColor(Random random) {
		int i = random.nextInt(10);
		if (i > 4) { return DyeColor.GRAY; }
		else if (i > 2) { return DyeColor.BLACK; }
		else { return DyeColor.LIGHT_GRAY; }
	}

	@Nullable
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
		Random random = world.getRandom();
		this.setColor(generateDefaultColor(random));
		this.getAttributeInstance(EntityAttributes.GENERIC_SCALE).addPersistentModifier(new EntityAttributeModifier(RANDOM_SPAWN_BONUS_MODIFIER_ID, (random.nextFloat() * 0.3F) - 0.15F, Operation.ADD_MULTIPLIED_BASE));
		return super.initialize(world, difficulty, spawnReason, entityData);
	}
	
	@Override protected void playStepSound(BlockPos pos, BlockState state) { return; }

	@Override
	public boolean addStatusEffect(StatusEffectInstance effect, Entity source) {
		this.heal(effect.getAmplifier() + 1);
		return false;
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);
		Item item = itemStack.getItem();
		if (item instanceof BrushItem && !player.getItemCooldownManager().isCoolingDown(item) && !this.getHungry()) {
			this.dataTracker.set(HUNGRY, true);
			this.dropStack(this.getVariant().getMetalState().copy());
			this.emitGameEvent(GameEvent.ENTITY_INTERACT);
			itemStack.damage(4, player, getSlotForHand(hand));
			// TODO : Sound Effects...
		} else if (item instanceof DyeItem dyeItem) {
			this.setColor(dyeItem.getColor());
			this.emitGameEvent(GameEvent.ENTITY_INTERACT);
			itemStack.decrementUnlessCreative(1, player);
			this.playSound(SoundEvents.ITEM_DYE_USE, 1.0F, 1.0F);
		} else {
			return Bucketable.tryBucket(player, hand, this).orElse(super.interactMob(player, hand));
		}
		return ActionResult.success(this.getWorld().isClient());
	}

	@Override
	public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
		UUID uUID = lightning.getUuid();
		if (!uUID.equals(this.lightningId)) {
			this.setVariant(this.getVariant() == VolcanoSnailEntity.Type.IRON ? VolcanoSnailEntity.Type.COPPER : VolcanoSnailEntity.Type.IRON);
			this.lightningId = uUID;
			// TODO : Sound Effects... this.playSound(SoundEvents.ENTITY_MOOSHROOM_CONVERT, 2.0F, 1.0F);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.getWorld().isClient()) {
			this.setClimbingWall(this.horizontalCollision);
		}
	}

	@Override
	public boolean isClimbing() {
		return this.isClimbingWall();
	}

	public boolean isClimbingWall() {
		return ((Byte)this.dataTracker.get(SNAIL_FLAGS) & 1) != 0;
	}

	public void setClimbingWall(boolean climbing) {
		byte b = (Byte)this.dataTracker.get(SNAIL_FLAGS);
		if (climbing) { b = (byte)(b | 1); }
		else { b &= -2; }
		this.dataTracker.set(SNAIL_FLAGS, b);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(TYPE, VolcanoSnailEntity.Type.IRON.name);
		builder.add(HUNGRY, false);
		builder.add(FROM_BUCKET, false);
		builder.add(COLOR, (byte)0);
		builder.add(SNAIL_FLAGS, (byte)0);
	}

	public static DefaultAttributeContainer.Builder createVolcanoSnailAttributes() {
		return PathAwareEntity.createLivingAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 2f)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f)
				.add(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY, 2f)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32)
				.add(EntityAttributes.GENERIC_ARMOR, 10.0f)
				.add(EntityAttributes.GENERIC_STEP_HEIGHT, 1f);
	}

	public static enum Type implements StringIdentifiable {
		IRON("iron", Items.RAW_IRON.getDefaultStack()),
		COPPER("copper", Items.RAW_COPPER.getDefaultStack());

		@SuppressWarnings("deprecation")
		public static final StringIdentifiable.EnumCodec<VolcanoSnailEntity.Type> CODEC = StringIdentifiable.createCodec(VolcanoSnailEntity.Type::values);
		final String name;
		final ItemStack metal;

		private Type(final String name, final ItemStack metal) {
			this.name = name;
			this.metal = metal;
		}

		public ItemStack getMetalState() { return this.metal; }
		@Override public String asString() { return this.name; }
		@SuppressWarnings("deprecation") static VolcanoSnailEntity.Type fromName(String name) { return CODEC.byId(name, IRON); }
	}

	@SuppressWarnings("deprecation")
	@Override
	public void copyDataToStack(ItemStack stack) {
		Bucketable.copyDataToStack(this, stack);
		NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, nbtCompound -> nbtCompound.putString("Type", this.getVariant().asString()));
		NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, nbtCompound -> nbtCompound.putBoolean("Hungry", this.dataTracker.get(HUNGRY)));
		NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, nbtCompound -> nbtCompound.putByte("Color", (byte)this.getColor().getId()));
		NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, nbtCompound -> nbtCompound.putBoolean("FromBucket", this.isFromBucket()));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void copyDataFromNbt(NbtCompound nbt) {
		Bucketable.copyDataFromNbt(this, nbt);
		this.setVariant(VolcanoSnailEntity.Type.fromName(nbt.getString("Type")));
		this.setHungry(nbt.getBoolean("Hungry"));
		this.setColor(DyeColor.byId(nbt.getByte("Color")));
		this.setFromBucket(nbt.getBoolean("FromBucket"));
	}

	@Override
	public ItemStack getBucketItem() {
		return new ItemStack(Resonance.VOLCANO_SNAIL_BUCKET);
	}

	@Override
	public SoundEvent getBucketFillSound() {
		return SoundEvents.ITEM_BUCKET_FILL;
	}

	public class EatChemicalsGoal extends MoveToTargetPosGoal {
		private static final Predicate<BlockState> VENT_FLUID_PREDICATE = BlockStatePredicate.forBlock(Resonance.VENT_FLUID);
		private final VolcanoSnailEntity entity;

		public EatChemicalsGoal(VolcanoSnailEntity entity, double speed, int range) {
			super(entity, speed, range, 2);
			this.entity = entity;
			this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
		}

		@Override public double getDesiredDistanceToTarget() { return 4; }
		@Override protected boolean isTargetPos(WorldView world, BlockPos pos) { return VENT_FLUID_PREDICATE.test(world.getBlockState(pos)); }
		@Override public boolean canStart() { return this.entity.getHungry() && this.mob.getRandom().nextInt(1000) == 0 && super.canStart(); }

		@Override
		public void tick() {
			super.tick();
			if (this.hasReached()) {
				this.entity.setHungry(false);
				this.entity.swingHand(preferredHand);
			}
		}
	}
}