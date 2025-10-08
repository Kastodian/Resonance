package net.moss.resonance.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.util.math.Direction.Type;

public class HeapBlock extends HorizontalConnectingBlock {
	public static final MapCodec<HeapBlock> CODEC = createCodec(HeapBlock::new);

	public HeapBlock(AbstractBlock.Settings settings) {
		this(8, 8, 9, 9, 7, settings);
	}

	public HeapBlock(float radius1, float radius2, float boundingHeight1, float boundingHeight2, float collisionHeight, AbstractBlock.Settings settings) {
		super(radius1, radius2, boundingHeight1, boundingHeight2, collisionHeight, settings);
		this.setDefaultState(getDefaultState().with(NORTH, false).with(EAST, false).with(SOUTH, false).with(WEST, false).with(WATERLOGGED, false));
	}

	@Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { super.appendProperties(builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED)); }
	@Override public MapCodec<HeapBlock> getCodec() { return CODEC; }

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockPos blockPos = ctx.getBlockPos();
		World world = ctx.getWorld();
		return super.getPlacementState(ctx).with(WATERLOGGED, world.getFluidState(blockPos).getFluid() == Fluids.WATER).with(NORTH, world.getBlockState(blockPos.north()).isSideSolidFullSquare(world, blockPos.north(), Direction.SOUTH)).with(EAST, world.getBlockState(blockPos.east()).isSideSolidFullSquare(world, blockPos.east(), Direction.WEST)).with(SOUTH, world.getBlockState(blockPos.south()).isSideSolidFullSquare(world, blockPos.south(), Direction.NORTH)).with(WEST, world.getBlockState(blockPos.west()).isSideSolidFullSquare(world, blockPos.west(), Direction.EAST));
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED)) { world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world)); }
		return direction.getAxis().getType() == Type.HORIZONTAL ? state.with(FACING_PROPERTIES.get(direction), neighborState.isSideSolidFullSquare(world, neighborPos, direction.getOpposite()) || neighborState.isOf(this)) : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}
}