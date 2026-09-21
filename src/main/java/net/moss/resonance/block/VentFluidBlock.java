package net.moss.resonance.block;

import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.Utils;

public class VentFluidBlock extends Block implements FluidDrainable {
	public static final IntProperty DISTANCE = Properties.DISTANCE_1_7;

	public VentFluidBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(DISTANCE, 1));
	}

	public static boolean isStillWater(BlockState state) {
		return state.isOf(Resonance.VENT_FLUID) || state.isOf(Blocks.WATER) && state.getFluidState().getLevel() >= 8 && state.getFluidState().isStill();
	}

	public static void update(WorldAccess world, BlockPos pos, BlockState state) {
		update(world, pos, world.getBlockState(pos), state);
	}

	public static void update(WorldAccess world, BlockPos pos, BlockState water, BlockState bubbleSource) {
		for (Direction direction2 : Direction.values()) {
			if (world.getFluidState(pos.offset(direction2.getOpposite())).isIn(FluidTags.LAVA)) {
				world.syncWorldEvent(1501, pos, 0);
				world.setBlockState(pos, Blocks.BLACKSTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
				return;
			}
		}
		if (isStillWater(water)) {
			BlockState blockState = getBubbleState(bubbleSource);
			int distance = blockState.isOf(Blocks.WATER) ? 7 : blockState.get(DISTANCE);
			world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
			BlockPos.Mutable mutable = pos.mutableCopy().move(Direction.UP);
			while (isStillWater(world.getBlockState(mutable)) && distance < 7) {
				distance++;
				if (!world.setBlockState(mutable, blockState.isOf(Blocks.WATER) ? blockState : blockState.with(DISTANCE, distance), Block.NOTIFY_LISTENERS)) { break; }
				mutable.move(Direction.UP);
			}
		}
	}

	private static BlockState getBubbleState(BlockState bubbleSource) {
		if (bubbleSource.isOf(Resonance.BATHYAL_VENT)) {
			return Resonance.VENT_FLUID.getDefaultState();
		} else if (bubbleSource.isOf(Resonance.VENT_FLUID) && bubbleSource.get(DISTANCE) < 7) {
			return Resonance.VENT_FLUID.getDefaultState().with(DISTANCE, bubbleSource.get(DISTANCE) + 1);
		} else {
			return Blocks.WATER.getDefaultState();
		}
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (random.nextInt(2) == 1) { world.addParticle(ParticleTypes.WHITE_ASH, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0, 0, 0); }
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (entity instanceof LivingEntity) { entity.damage(Utils.damageSource(world, Resonance.VENT_PLUME_DAMAGE), 1.0F); }
		super.onEntityCollision(state, world, pos, entity);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		for (Direction direction2 : Direction.values()) {
			if (world.getFluidState(pos.offset(direction2.getOpposite())).isIn(FluidTags.LAVA)) {
				world.syncWorldEvent(1501, pos, 0);
				return Blocks.BLACKSTONE.getDefaultState();
			}
		}
		if (!state.canPlaceAt(world, pos)) {
			world.scheduleBlockTick(pos, this, 5);
		}
		world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		for (Direction direction : Direction.values()) {
			if (direction == Direction.UP) { continue; }
			BlockState blockState = world.getBlockState(pos.offset(direction));
			if ((blockState.isOf(Resonance.BATHYAL_VENT) && blockState.get(BathyalVent.LIT) && blockState.get(BathyalVent.WATERLOGGED)) || blockState.isOf(Resonance.VENT_FLUID)) {
				update(world, pos, state, blockState);
				return;
			}
		}
		update(world, pos, state, world.getBlockState(pos.down()));
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		if (!isStillWater(state)) { return false; }
		for (Direction direction : Direction.values()) {
			if (direction == Direction.UP) { continue; }
			BlockState blockState = world.getBlockState(pos.offset(direction));
			int distance = state.isOf(Resonance.VENT_FLUID) ? state.get(DISTANCE) : 7;
			if ((blockState.isOf(Resonance.BATHYAL_VENT) && blockState.get(BathyalVent.LIT) && blockState.get(BathyalVent.WATERLOGGED)) || (blockState.isOf(Resonance.VENT_FLUID) && blockState.get(DISTANCE) < distance)) { return true; }
		}
		return false;
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		super.randomTick(state, world, pos, random);
		if (random.nextInt(5 + (state.get(DISTANCE) * 2)) != 1) { return; }
		BlockState upState = world.getBlockState(pos.up());
		if (upState.getBlock() instanceof Oxidizable oxidizable) { oxidizable.tryDegrade(state, world, pos.up(), random).ifPresent(degraded -> world.setBlockState(pos.up(), degraded)); }
	}

	@Override
	public ItemStack tryDrainFluid(PlayerEntity player, WorldAccess world, BlockPos pos, BlockState state) {
		world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL_AND_REDRAW);
		return new ItemStack(Items.WATER_BUCKET);
	}

	@Override public Optional<SoundEvent> getBucketFillSound() { return Fluids.WATER.getBucketFillSound(); }
	@Override protected FluidState getFluidState(BlockState state) { return Fluids.WATER.getStill(false); }
	@Override protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return VoxelShapes.empty(); }
	@Override protected BlockRenderType getRenderType(BlockState state) { return BlockRenderType.INVISIBLE; }
	@Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { builder.add(DISTANCE); }
	@Override protected boolean hasRandomTicks(BlockState state) { return true; }
}