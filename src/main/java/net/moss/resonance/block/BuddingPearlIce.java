package net.moss.resonance.block;

import org.jetbrains.annotations.Nullable;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.entity.BuddingPearlIceBlockEntity;
import net.minecraft.state.property.Properties;

public class BuddingPearlIce extends Block implements BlockEntityProvider {
	public static final IntProperty AGE = IntProperty.of("age", 0, 6);
	public static final BooleanProperty HAS_SEED = BooleanProperty.of("has_seed");
	public static final BooleanProperty HAS_POTION = BooleanProperty.of("has_potion");
	public static final BooleanProperty HAS_CATALYST = BooleanProperty.of("has_catalyst");
	public static final BooleanProperty HAS_PEARL = BooleanProperty.of("has_pearl");
	private static final IntProperty DUSTED = Properties.DUSTED;

	public BuddingPearlIce(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(DUSTED, 0).with(AGE, 0).with(HAS_PEARL, false).with(HAS_POTION, false).with(HAS_CATALYST, false).with(HAS_SEED, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder.add(HAS_SEED, HAS_POTION, HAS_PEARL, HAS_CATALYST, AGE, DUSTED));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
		super.afterBreak(world, player, pos, state, blockEntity, tool);
		if (blockEntity instanceof BuddingPearlIceBlockEntity buddingPearlIce) {
			dropStack(world, pos, buddingPearlIce.getCatalyst().copy());
			dropStack(world, pos, buddingPearlIce.getSeed().copy());
			dropStack(world, pos, buddingPearlIce.getPotion().copy());
			buddingPearlIce.clear();
		}
		if (world.getDimension().ultrawarm()) {
			world.removeBlock(pos, false);
			return;
		}
		BlockState blockState = world.getBlockState(pos.down());
		if (blockState.isSolid() || blockState.isLiquid()) { world.setBlockState(pos, Blocks.WATER.getDefaultState()); }
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		Direction direction = Direction.random(random);
		if (world.getBlockState(pos.offset(direction)).isReplaceable()) {
			world.setBlockState(pos.offset(direction), Resonance.ICE_FUR.getDefaultState().with(IceFur.getProperty(direction.getOpposite()), true).with(IceFur.WATERLOGGED, world.getFluidState(pos.offset(direction)).getFluid() == Fluids.WATER), Block.NOTIFY_LISTENERS);
		}
		int chance = 80;
		if (pos.getY() < 1) { chance -= 10; }
		if (world.isSkyVisible(pos)) { chance += 20; }
		if (world.isThundering()) { chance -= 30; }
		else if (world.isRaining()) { chance -= 20; }
		if (random.nextInt(chance) == 0 && world.getBlockEntity(pos) instanceof BuddingPearlIceBlockEntity blockEntity) {
			blockEntity.spreadSculk(state, world, random);
			if (state.get(AGE) == 5 && blockEntity.canCraftPearl()) { blockEntity.craftPearl(state, random); }
			else { world.setBlockState(pos, state.with(AGE, state.get(AGE) + 1), Block.NOTIFY_LISTENERS); }
		}
	}

	@Override
	public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
		return new ItemStack(state.get(AGE) > 2 ? Resonance.PEARL_ICE : Blocks.ICE);
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient() && world.getBlockEntity(pos) instanceof BuddingPearlIceBlockEntity blockEntity && !state.get(HAS_PEARL)) {
			if ((stack.contains(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER) || stack.contains(DataComponentTypes.POTION_CONTENTS) || stack.contains(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS)) && !state.get(HAS_POTION)) {
				blockEntity.setPotion(stack.split(1));
				state = state.with(HAS_POTION, true);
				world.setBlockState(pos, state);
				world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, state));
				world.updateNeighborsAlways(pos.down(), state.getBlock());
				return ItemActionResult.CONSUME;
			} else if (stack.contains(Resonance.PEARL_NATURE) && !state.get(HAS_CATALYST)) {
				blockEntity.setCatalyst(stack.split(1));
				state = state.with(HAS_CATALYST, true);
				world.setBlockState(pos, state);
				world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, state));
				world.updateNeighborsAlways(pos.down(), state.getBlock());
				return ItemActionResult.CONSUME;
			} else if (stack.isOf(Items.ECHO_SHARD) && world instanceof ServerWorld serverWorld) {
				stack.decrementUnlessCreative(1, player);
				Random random = world.getRandom();
				Direction direction = Direction.random(random);
				if (world.getBlockState(pos.offset(direction)).isReplaceable()) {
					world.setBlockState(pos.offset(direction), Resonance.ICE_FUR.getDefaultState().with(IceFur.getProperty(direction.getOpposite()), true).with(IceFur.WATERLOGGED, world.getFluidState(pos.offset(direction)).getFluid() == Fluids.WATER), Block.NOTIFY_LISTENERS);
				}
				blockEntity.spreadSculk(state, serverWorld, random);
				if (state.get(AGE) == 5 && blockEntity.canCraftPearl()) { blockEntity.craftPearl(state, random); }
				else { world.setBlockState(pos, state.with(AGE, state.get(AGE) + 1), Block.NOTIFY_LISTENERS); }
			}
		}
		return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	public void putSeed(World world, BlockPos pos, BlockState state, ItemStack stack) {
		if (state.get(HAS_SEED) || world.isClient()) { return; }
		world.getBlockEntity(pos, Resonance.BUDDING_PEARL_ICE_BLOCK_ENTITY).get().setSeed(stack.split(1));
		state = state.with(HAS_SEED, true);
		world.setBlockState(pos, state);
		world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, state));
		world.updateNeighborsAlways(pos, state.getBlock());
	}

	@Override protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) { return stateFrom.isOf(this) ? stateFrom.get(AGE) == state.get(AGE) : state.get(AGE) == 0 && stateFrom.isOf(Blocks.ICE) || super.isSideInvisible(state, stateFrom, direction); }
	@Override public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) { world.getBlockEntity(pos, Resonance.BUDDING_PEARL_ICE_BLOCK_ENTITY).get().scheduledTick(); }
	@Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new BuddingPearlIceBlockEntity(pos, state); }
	@Override protected boolean hasRandomTicks(BlockState state) { return state.get(AGE) < 6 && state.get(HAS_POTION); }
}