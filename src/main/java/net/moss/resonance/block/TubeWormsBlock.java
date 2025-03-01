package net.moss.resonance.block;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.moss.resonance.Resonance;

public class TubeWormsBlock extends Block implements FluidFillable {
	public static final IntProperty AGE = Properties.AGE_2;
    protected static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public TubeWormsBlock(Settings settings) {
        super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(AGE, 0));
    }
    
    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) { return Blocks.AIR.getDefaultState(); }
        BlockState blockState = super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        if (!blockState.isAir()) { world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world)); }
        return blockState;
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        if (!isWater(world, pos)) { return null; }
        else if (canPlaceAt(this.getDefaultState(), world, pos)) { return super.getPlacementState(ctx); }
        else { return Resonance.WALL_TUBE_WORMS.getPlacementState(ctx); }
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        return world.getBlockState(blockPos).isSideSolidFullSquare(world, blockPos, Direction.UP);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (random.nextInt(50) != 0) { return; }
        BlockPos upPos = pos.up();
        if (!isWater(world, upPos)) { return; }
        List<Direction> l = Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
        Collections.shuffle(l);
        for (Direction direction : l) {
            if (canGrowOn(world, direction, upPos)) {
                state = state.cycle(AGE);
                world.setBlockState(pos, state);
                world.setBlockState(upPos, Resonance.WALL_TUBE_WORMS.getDefaultState().with(AGE, state.get(AGE)).with(WallTubeWormsBlock.HORIZONTAL_FACING, direction));
            }
        }
    }

    protected static boolean canGrowOn(BlockView world, Direction direction, BlockPos pos) {
        BlockPos offsetPos = pos.offset(direction);
        BlockState state = world.getBlockState(offsetPos);
        return Block.isFaceFullSquare(state.getSidesShape(world, offsetPos), direction.getOpposite()) || Block.isFaceFullSquare(state.getCollisionShape(world, offsetPos), direction.getOpposite());
    }

    protected static boolean isWater(World world, BlockPos pos) {
        FluidState fluidState = world.getFluidState(pos);
        return fluidState.isIn(FluidTags.WATER) && fluidState.getLevel() == 8 && world.getBlockState(pos).isReplaceable();
    }

    @Override protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return SHAPE; }
    @Override public boolean canFillWithFluid(@Nullable PlayerEntity player, BlockView world, BlockPos pos, BlockState state, Fluid fluid) { return false; }
    @Override public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) { return false; }
    @Override protected FluidState getFluidState(BlockState state) { return Fluids.WATER.getStill(false); }
	@Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { builder.add(AGE); }
}
