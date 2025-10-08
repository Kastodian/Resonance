package net.moss.resonance.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class EmptyLikeBlock extends Block {
	public static final IntProperty DURATION = IntProperty.of("duration",0,32);

	public EmptyLikeBlock(Settings settings) {
		super(settings.replaceable().noCollision().dropsNothing().pistonBehavior(PistonBehavior.DESTROY).noBlockBreakParticles().breakInstantly());
		this.setDefaultState(this.getDefaultState().with(DURATION, 3));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(DURATION);
	}

	public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) { return true; }
	public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.INVISIBLE; }
	public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) { return 1.0F; }
	@Override public boolean hasRandomTicks(BlockState state) { return true; }
	@Override protected boolean canPathfindThrough(BlockState state, NavigationType type) { return true; }
	@Override public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return Block.createCuboidShape(4, 4, 4, 12, 12, 12); }
}