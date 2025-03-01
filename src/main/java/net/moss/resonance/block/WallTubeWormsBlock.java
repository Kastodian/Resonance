package net.moss.resonance.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public class WallTubeWormsBlock extends TubeWormsBlock {
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(3, 0, 13, 13, 16, 16);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(3, 0, 0, 13, 16, 3);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0, 0, 3, 3, 16, 13);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(13, 0, 3, 16, 16, 13);
    public static final DirectionProperty HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;

    public WallTubeWormsBlock(Settings settings) {
        super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(AGE, 0).with(HORIZONTAL_FACING, Direction.SOUTH));
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(HORIZONTAL_FACING)) {
            case NORTH:
                return SOUTH_SHAPE;
            case SOUTH:
                return NORTH_SHAPE;
            case EAST:
                return WEST_SHAPE;
            default:
                return EAST_SHAPE;
        }
    }

    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return !canGrowOn(world, direction, neighborPos) ? Blocks.AIR.getDefaultState() : state;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (canGrowOn(world, Direction.NORTH, pos)) { return true; }
        else if (canGrowOn(world, Direction.SOUTH, pos)) { return true; }
        else if (canGrowOn(world, Direction.EAST, pos)) { return true; }
        else if (canGrowOn(world, Direction.WEST, pos)) { return true; }
        return false;
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE, HORIZONTAL_FACING);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getPlacementDirections()[0];
        if (canGrowOn(ctx.getWorld(), direction, ctx.getBlockPos())) {
            return this.getDefaultState().with(HORIZONTAL_FACING, direction);
        }
        return null;
    }
    
    @Override protected boolean hasRandomTicks(BlockState state) { return state.get(AGE) < 2; }
}
