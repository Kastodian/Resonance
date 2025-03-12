package net.moss.resonance.entity;

import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BrushItem;
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
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.Resonance;

public class VolcanoSnailEntity extends WaterCreatureEntity implements Bucketable {
	private static final TrackedData<String> TYPE = DataTracker.registerData(VolcanoSnailEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<Boolean> HUNGRY = DataTracker.registerData(VolcanoSnailEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final TrackedData<Boolean> FROM_BUCKET = DataTracker.registerData(VolcanoSnailEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	@Nullable private UUID lightningId;

    public VolcanoSnailEntity(EntityType<? extends WaterCreatureEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 2;
    }

	@Override
	public float getPathfindingFavor(BlockPos pos, WorldView world) {
		return world.getBiome(pos) == Resonance.BATHYAL_ZONE || world.getBlockState(pos.down()).isIn(Resonance.VOLCANO_SNAIL_PREFERED_TAG) || world.getBlockState(pos).isIn(Resonance.VOLCANO_SNAIL_PREFERED_TAG) ? 15.0F : world.getPhototaxisFavor(pos);
	}

    @Override
    protected void initGoals() {
        super.initGoals();
		this.goalSelector.add(1, new EatGlowLichenGoal(this));
		this.goalSelector.add(2, new EscapeDangerGoal(this, 0.6f));
		this.goalSelector.add(3, new WanderAroundGoal(this, 0.5f));
		this.goalSelector.add(4, new LookAroundGoal(this));
    }

	public void setVariant(VolcanoSnailEntity.Type type) {
		this.dataTracker.set(TYPE, type.name);
	}

	public VolcanoSnailEntity.Type getVariant() {
		return VolcanoSnailEntity.Type.fromName(this.dataTracker.get(TYPE));
	}

	public Boolean isHungry() {
		return this.dataTracker.get(HUNGRY);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putString("Type", this.getVariant().asString());
		nbt.putBoolean("Hungry", this.dataTracker.get(HUNGRY));
		nbt.putBoolean("FromBucket", this.isFromBucket());
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		this.setVariant(VolcanoSnailEntity.Type.fromName(nbt.getString("Type")));
        this.dataTracker.set(HUNGRY, nbt.getBoolean("Hungry"));
		this.setFromBucket(nbt.getBoolean("FromBucket"));
	}

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return damageSource.isIn(DamageTypeTags.IS_FIRE) ? true : super.isInvulnerableTo(damageSource);
    }
    
	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();
		if (item instanceof BrushItem && !player.getItemCooldownManager().isCoolingDown(item) && !this.isHungry()) {
            this.dataTracker.set(HUNGRY, true);
            this.dropStack(this.getVariant().getMetalState().copy());
			this.emitGameEvent(GameEvent.ENTITY_INTERACT);
			itemStack.damage(2, player, getSlotForHand(hand));
            player.getItemCooldownManager().set(item, 50);
            // TODO : Sound Effects...
			return ActionResult.success(this.getWorld().isClient);
		} else {
			return (ActionResult)Bucketable.tryBucket(player, hand, this).orElse(super.interactMob(player, hand));
		}
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
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(TYPE, VolcanoSnailEntity.Type.IRON.name);
		builder.add(HUNGRY, false);
		builder.add(FROM_BUCKET, false);
	}
    
    public static DefaultAttributeContainer.Builder createVolcanoSnailAttributes() {
        return PathAwareEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 2f)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4f)
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
		final ItemStack mushroom;

		private Type(final String name, final ItemStack mushroom) {
			this.name = name;
			this.mushroom = mushroom;
		}

		public ItemStack getMetalState() { return this.mushroom; }
		@Override public String asString() { return this.name; }
		@SuppressWarnings("deprecation") static VolcanoSnailEntity.Type fromName(String name) { return (VolcanoSnailEntity.Type)CODEC.byId(name, IRON); }
	}
    
    public class EatGlowLichenGoal extends Goal {
        private static final Predicate<BlockState> GLOW_LICHEN_PREDICATE = BlockStatePredicate.forBlock(Blocks.GLOW_LICHEN);
        private final VolcanoSnailEntity mob;
        private final World world;
        private int timer;

        public EatGlowLichenGoal(VolcanoSnailEntity mob) {
            this.mob = mob;
            this.world = mob.getWorld();
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
        }

        @Override
        public boolean canStart() {
            if (!this.mob.isHungry() || this.mob.getRandom().nextInt(1000) != 0) {
                return false;
            } else {
                BlockPos blockPos = this.mob.getBlockPos();
                return GLOW_LICHEN_PREDICATE.test(this.world.getBlockState(blockPos));
            }
        }

        @Override
        public void start() {
            this.timer = this.getTickCount(40);
            this.world.sendEntityStatus(this.mob, EntityStatuses.SET_SHEEP_EAT_GRASS_TIMER_OR_PRIME_TNT_MINECART);
            this.mob.getNavigation().stop();
        }

        @Override
        public void stop() {
            this.timer = 0;
        }

        @Override
        public boolean shouldContinue() {
            return this.timer > 0;
        }

        public int getTimer() {
            return this.timer;
        }

        @Override
        public void tick() {
            this.timer = Math.max(0, this.timer - 1);
            if (this.timer == this.getTickCount(4)) {
                BlockPos blockPos = this.mob.getBlockPos();
                if (GLOW_LICHEN_PREDICATE.test(this.world.getBlockState(blockPos))) {
                    if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                        this.world.breakBlock(blockPos, false);
                    }
                    this.mob.dataTracker.set(HUNGRY, false);
                }
            }
        }
    }

    @Override
    public boolean isFromBucket() {
		return this.dataTracker.get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean fromBucket) {
		this.dataTracker.set(FROM_BUCKET, fromBucket);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void copyDataToStack(ItemStack stack) {
		Bucketable.copyDataToStack(this, stack);
		NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, nbtCompound -> nbtCompound.putString("Type", this.getVariant().asString()));
		NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, nbtCompound -> nbtCompound.putBoolean("Hungry", this.dataTracker.get(HUNGRY)));
		NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, nbtCompound -> nbtCompound.putBoolean("FromBucket", this.isFromBucket()));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void copyDataFromNbt(NbtCompound nbt) {
		Bucketable.copyDataFromNbt(this, nbt);
		this.setVariant(VolcanoSnailEntity.Type.fromName(nbt.getString("Type")));
        this.dataTracker.set(HUNGRY, nbt.getBoolean("Hungry"));
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
}