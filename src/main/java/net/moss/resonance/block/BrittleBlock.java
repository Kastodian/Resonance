package net.moss.resonance.block;

import java.util.Optional;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;
import net.moss.resonance.FreezeThawing;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.entity.BrittleBlockEntity;

public class BrittleBlock extends BlockWithEntity {
	public static final MapCodec<BrittleBlock> CODEC = createCodec(BrittleBlock::new);
	public static final BooleanProperty CRACKED = Properties.CRACKED;

	public BrittleBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(CRACKED, false));
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!newState.isOf(Resonance.BRITTLE_BLOCK) && world.getBlockEntity(pos) instanceof BrittleBlockEntity brittleBlockEntity && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
			Block.dropStacks(brittleBlockEntity.getStoredState(), world, pos, brittleBlockEntity);
			world.removeBlockEntity(pos);
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	@Override
	public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
		if (!(world.getBlockEntity(pos) instanceof BrittleBlockEntity brittleBlockEntity)) return ItemStack.EMPTY;
		BlockState storedState = brittleBlockEntity.getStoredState();
		return storedState.getBlock().getPickStack(world, pos, storedState);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (world.getBlockEntity(pos) instanceof BrittleBlockEntity brittleBlockEntity && brittleBlockEntity.getStoredState().isAir()) { world.breakBlock(pos, true); }
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
		if (world.isClient()) { return; }
		if (state.get(CRACKED)) {
			world.breakBlock(pos, true, entity);
		} else if (world.getRandom().nextInt(3) == 0) {
			world.setBlockState(pos, state.with(CRACKED, true), Block.NOTIFY_ALL);
		}
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (hand.equals(Hand.MAIN_HAND) && player.getOffHandStack().isOf(Items.SHIELD) && !player.shouldCancelInteraction()) { return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; }
		else if (!stack.isIn(ItemTags.PICKAXES)) { return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; }
		else if (world.getBlockEntity(pos) instanceof BrittleBlockEntity brittleBlockEntity) {
			BlockState storedState = brittleBlockEntity.getStoredState();
			world.addBlockBreakParticles(pos, state);
			world.removeBlockEntity(pos);
			world.setBlockState(pos, storedState, 11);
			world.playSound(player, pos, SoundEvents.BLOCK_SNOW_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
			world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, Emitter.of(player, storedState));
			world.updateNeighbor(pos, storedState.getBlock(), pos);
			if (player instanceof ServerPlayerEntity serverPlayerEntity) { Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayerEntity, pos, stack); }
			if (player != null) { stack.damage(1, player, LivingEntity.getSlotForHand(hand)); }
			return ItemActionResult.SUCCESS;
		}
		return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (world.getBlockEntity(pos) instanceof BrittleBlockEntity brittleBlockEntity) {
			BlockState storedState = brittleBlockEntity.getStoredState();

			Optional<FreezeThawing> freezeThawing = world.getServer().getRegistryManager().get(Resonance.FREEZE_THAWIMG_REGISTRY_KEY).stream().filter(predicate -> storedState.isIn(predicate.input())).findFirst();

			if (freezeThawing.isPresent()) {
				FreezeThawing conversion = freezeThawing.get();
				if (random.nextInt(conversion.time()) != 0) {
					brittleBlockEntity.setStoredState(conversion.output().get(random, pos));
					world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
				}
			}

			if (storedState.isAir()) {
				world.removeBlockEntity(pos);
				world.removeBlock(pos, false);
				return;
			} else if (world.getLightLevel(LightType.BLOCK, pos) > 11 - state.getOpacity(world, pos) || world.getDimension().ultrawarm()) {
				world.removeBlockEntity(pos);
				world.setBlockState(pos, storedState, 11);
				return;
			}
		}
		for (Direction direction : Direction.values()) {
			BlockPos pos2 = pos.offset(direction);
			BlockState state2 = world.getBlockState(pos2);
			if (state2.isOf(Resonance.ICE_FUR) && state2.hasRandomTicks()) { state2.randomTick(world, pos2, random); return; }
		}
	}

	public static void makeBrittle(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		Block block = state.getBlock();
		if (state.isIn(Resonance.BRITTLE_BLACKLIST_TAG) || block instanceof BrittleBlock || block instanceof BlockEntityProvider || !state.isFullCube(world, pos) || state.getHardness(world, pos) < 0) { return; }
		world.setBlockState(pos, Resonance.BRITTLE_BLOCK.getDefaultState());
		if (world.getBlockEntity(pos) instanceof BrittleBlockEntity blockEntity) {
			blockEntity.setStoredState(state);
		}
	}

	@Override public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState) { return new BrittleBlockEntity(blockPos, blockState); }
	@Override protected MapCodec<? extends BlockWithEntity> getCodec() { return CODEC; }
	@Override public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }
	@Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { super.appendProperties(builder.add(CRACKED)); }
	@Override protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) { return stateFrom.isOf(this) ? true : super.isSideInvisible(state, stateFrom, direction); }
}