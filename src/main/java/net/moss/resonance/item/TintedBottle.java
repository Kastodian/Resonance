package net.moss.resonance.item;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.LightningRodBlock;
import net.minecraft.block.SculkCatalystBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.BathyalVent;
import net.moss.resonance.entity.LostSoulEntity;
import net.moss.resonance.util.Utils;
import net.minecraft.world.RaycastContext;

public class TintedBottle extends Item {
	public static final DispenserBehavior TINTED_BOTTLE_BEHAVIOR = new FallibleItemDispenserBehavior(){
		private ItemStack replace(BlockPointer pointer, ItemStack oldStack, ItemStack newStack) {
			pointer.world().emitGameEvent(null, GameEvent.FLUID_PICKUP, pointer.pos());
			return this.decrementStackWithRemainder(pointer, oldStack, newStack);
		}

		@Override
		public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
			this.setSuccess(false);
			ServerWorld serverWorld = pointer.world();
			BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
			BlockState blockState = serverWorld.getBlockState(blockPos);

			if (blockState.isOf(Blocks.SCULK_CATALYST) && blockState.get(SculkCatalystBlock.BLOOM)) {
				if (serverWorld.getRandom().nextInt(4) == 0) { serverWorld.setBlockState(blockPos, blockState.with(SculkCatalystBlock.BLOOM, false), Block.NOTIFY_LISTENERS); }
				this.setSuccess(true);
				return this.replace(pointer, stack, new ItemStack(Resonance.SCULK_BOTTLE));
			} else if (blockState.isOf(Blocks.LIGHTNING_ROD) && blockState.get(LightningRodBlock.POWERED)) {
				if (serverWorld.getRandom().nextInt(4) == 0) { serverWorld.setBlockState(blockPos, blockState.with(SculkCatalystBlock.BLOOM, false), Block.NOTIFY_LISTENERS); }
				this.setSuccess(true);
				return this.replace(pointer, stack, new ItemStack(Resonance.SCULK_BOTTLE));
			} else if (blockState.isOf(Resonance.BATHYAL_VENT) && blockState.get(BathyalVent.LIT) && blockState.get(BathyalVent.WATERLOGGED)) {
				serverWorld.setBlockState(blockPos, blockState.with(BathyalVent.LIT, false).with(BathyalVent.WATERLOGGED, false), Block.NOTIFY_LISTENERS);
				this.setSuccess(true);
				return this.replace(pointer, stack, new ItemStack(Resonance.VENT_FLUID_BOTTLE));
			}
			List<Entity> list = pointer.world().getEntitiesByClass(Entity.class, new Box(blockPos), Entity::isAlive);
			for (Entity entity : list) {
				if (entity instanceof FireballEntity) {
					this.setSuccess(true);
					entity.remove(RemovalReason.DISCARDED);
					return this.replace(pointer, stack, new ItemStack(Resonance.FIREBALL_BOTTLE));
				} else if (entity instanceof ShulkerBulletEntity) {
					this.setSuccess(true);
					entity.remove(RemovalReason.DISCARDED);
					return this.replace(pointer, stack, new ItemStack(Resonance.SHULKER_BOTTLE));
				} else if (entity instanceof LostSoulEntity) {
					this.setSuccess(true);
					entity.remove(RemovalReason.DISCARDED);
					return this.replace(pointer, stack, new ItemStack(Resonance.SOUL_BOTTLE));
				}
			}

			return super.dispenseSilently(pointer, stack);
		}
	};

	public TintedBottle(Item.Settings settings) {
		super(settings);
		DispenserBlock.registerBehavior(this, TINTED_BOTTLE_BEHAVIOR);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
		ItemStack itemStack = playerEntity.getStackInHand(hand);
		if (playerEntity.isSneaking()) { return TypedActionResult.pass(itemStack); }
		BlockHitResult blockHitResult = TintedBottle.raycast(world, playerEntity, RaycastContext.FluidHandling.NONE);
		BlockPos blockPos = blockHitResult.getBlockPos();
		List<Entity> list = Utils.entitiesInRadius(world, blockHitResult.getPos(), playerEntity, 2, 2, Entity::isAlive);
		for (Entity entity2 : list) {
			if (entity2 instanceof LightningEntity) {
				world.playSound(null, blockPos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.PLAYERS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
				playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
				ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, playerEntity, Resonance.LIGHTNING_BOTTLE.getDefaultStack());
				playerEntity.getItemCooldownManager().set(Resonance.LIGHTNING_BOTTLE, 20);
				return TypedActionResult.success(itemStack2, world.isClient());
			}
		}

		if (((HitResult)blockHitResult).getType() == HitResult.Type.BLOCK) {
			BlockState blockState = world.getBlockState(blockPos);
			if (blockState.isOf(Blocks.SCULK_CATALYST) && blockState.get(SculkCatalystBlock.BLOOM)) {
				world.playSound(null, blockPos, SoundEvents.BLOCK_SCULK_SPREAD, SoundCategory.PLAYERS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
				world.playSound(null, blockPos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.PLAYERS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
				if (world.getRandom().nextInt(4) == 0) { world.setBlockState(blockPos, blockState.with(SculkCatalystBlock.BLOOM, false), Block.NOTIFY_LISTENERS); }
				playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
				ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, playerEntity, Resonance.SCULK_BOTTLE.getDefaultStack());
				return TypedActionResult.success(itemStack2, world.isClient());
			} else if (blockState.isOf(Resonance.BATHYAL_VENT) && blockState.get(BathyalVent.LIT) && blockState.get(BathyalVent.WATERLOGGED)) {
				world.playSound(null, blockPos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.PLAYERS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
				world.setBlockState(blockPos, blockState.with(BathyalVent.LIT, false).with(BathyalVent.WATERLOGGED, false), Block.NOTIFY_LISTENERS);
				playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
				ItemStack stack = Resonance.VENT_FLUID_BOTTLE.getDefaultStack();
				stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(blockState.get(BathyalVent.COLOR).getFireworkColor(), true));
				ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, playerEntity, stack);
				return TypedActionResult.success(itemStack2, world.isClient());
			}
		}
		return TypedActionResult.pass(itemStack);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		PlayerEntity playerEntity = context.getPlayer();
		if (context.getWorld().getBlockState(context.getBlockPos()).isOf(Resonance.BUDDING_PEARL_ICE)) {
			return ActionResult.CONSUME;
		}
		if (playerEntity.isSneaking()) {
			ActionResult actionResult = this.place(new ItemPlacementContext(context));
			if (!actionResult.isAccepted() && context.getStack().contains(DataComponentTypes.FOOD)) {
				ActionResult actionResult2 = super.use(context.getWorld(), playerEntity, context.getHand()).getResult();
				return actionResult2 == ActionResult.CONSUME ? ActionResult.CONSUME_PARTIAL : actionResult2;
			} else { return actionResult; }
		}
		return super.useOnBlock(context);
	}

	public ActionResult place(ItemPlacementContext context) {
		if (!this.getBlock().isEnabled(context.getWorld().getEnabledFeatures())) {
			return ActionResult.FAIL;
		} else if (!context.canPlace()) {
			return ActionResult.FAIL;
		} else {
			BlockState blockState = this.getPlacementState(context);
			if (blockState == null) {
				return ActionResult.FAIL;
			} else if (!this.place(context, blockState)) {
				return ActionResult.FAIL;
			} else {
				BlockPos blockPos = context.getBlockPos();
				World world = context.getWorld();
				PlayerEntity playerEntity = context.getPlayer();
				ItemStack itemStack = context.getStack();
				BlockState blockState2 = world.getBlockState(blockPos);
				if (blockState2.isOf(blockState.getBlock())) {
					blockState2 = this.placeFromNbt(blockPos, world, itemStack, blockState2);
					BlockItem.writeNbtToBlockEntity(world, playerEntity, blockPos, itemStack);
					copyComponentsToBlockEntity(world, blockPos, itemStack);
					blockState2.getBlock().onPlaced(world, blockPos, blockState2, playerEntity, itemStack);
					if (playerEntity instanceof ServerPlayerEntity) {
					Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)playerEntity, blockPos, itemStack);
					}
				}

				BlockSoundGroup blockSoundGroup = blockState2.getSoundGroup();
				world.playSound(playerEntity, blockPos, this.getPlaceSound(blockState2), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
				world.emitGameEvent(GameEvent.BLOCK_PLACE, blockPos, Emitter.of(playerEntity, blockState2));
				itemStack.decrementUnlessCreative(1, playerEntity);
				return ActionResult.success(world.isClient);
			}
		}
	}

	private BlockState placeFromNbt(BlockPos pos, World world, ItemStack stack, BlockState state) {
		BlockStateComponent blockStateComponent = (BlockStateComponent)stack.getOrDefault(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT);
		if (blockStateComponent.isEmpty()) {
			return state;
		} else {
			BlockState blockState = blockStateComponent.applyToState(state);
			if (blockState != state) {
				world.setBlockState(pos, blockState, 2);
			}
			return blockState;
		}
	}

	private static void copyComponentsToBlockEntity(World world, BlockPos pos, ItemStack stack) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity != null) {
			blockEntity.readComponents(stack);
			blockEntity.markDirty();
		}
	}

	@Nullable
	protected BlockState getPlacementState(ItemPlacementContext context) {
		BlockState blockState = this.getBlock().getPlacementState(context);
		return blockState != null && this.canPlace(context, blockState) ? blockState : null;
	}

	protected boolean canPlace(ItemPlacementContext context, BlockState state) {
		PlayerEntity playerEntity = context.getPlayer();
		ShapeContext shapeContext = playerEntity == null ? ShapeContext.absent() : ShapeContext.of(playerEntity);
		return (state.canPlaceAt(context.getWorld(), context.getBlockPos())) && context.getWorld().canPlace(state, context.getBlockPos(), shapeContext);
	}

	protected boolean place(ItemPlacementContext context, BlockState state) { return context.getWorld().setBlockState(context.getBlockPos(), state, 11); }
	protected SoundEvent getPlaceSound(BlockState state) { return state.getSoundGroup().getPlaceSound(); }
	public Block getBlock() { return Resonance.TINTED_BOTTLE_BLOCK; }
}