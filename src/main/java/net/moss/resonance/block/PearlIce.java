package net.moss.resonance.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.SnowyBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.moss.resonance.Resonance;

public class PearlIce extends SnowyBlock {
    public PearlIce(Settings settings) { super(settings); }
    @Override protected boolean hasRandomTicks(BlockState state) { return true; }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        for (int i = 0; i < 7; i++) {
            BlockPos blockPos = pos.add(random.nextBetween(0, 6) - 3, 0, random.nextBetween(0, 6) - 3);
            BlockState state2 = world.getBlockState(blockPos);
            BlockState state2up = world.getBlockState(blockPos.up());
            BlockState state2down = world.getBlockState(blockPos.down());
            if (state2.isOf(Blocks.WATER) && state2.get(FluidBlock.LEVEL) == 0 && (state2up.isAir() || state2up.isIn(BlockTags.ICE)) && world.getLightLevel(LightType.BLOCK, blockPos) < 11) {
                for (Direction direction : Direction.values()) {
                    if (direction != Direction.DOWN && world.getBlockState(blockPos.offset(direction)).isSideSolidFullSquare(world, blockPos.offset(direction), direction.getOpposite())) {
                        world.setBlockState(blockPos, Blocks.ICE.getDefaultState(), Block.NOTIFY_ALL);
                        return;
                    }
                }
            } else if (state2down.isOf(Blocks.WATER) && state2down.get(FluidBlock.LEVEL) == 0 && state2.isSideSolidFullSquare(world, blockPos, Direction.DOWN)) {
                world.setBlockState(blockPos.down(), Resonance.ICICLE.getDefaultState().with(Icicle.VERTICAL_DIRECTION, Direction.DOWN).with(Icicle.WATERLOGGED, true), Block.NOTIFY_ALL);
                return;
            }
        }
        for (BlockPos blockPos : BlockPos.iterateOutwards(pos, 3, 3, 3)) {
            BlockState state2 = world.getBlockState(blockPos);
            if (state2.isOf(Resonance.ICE_FUR) && state2.hasRandomTicks()) { state2.randomTick(world, blockPos, random); return; }
        }
    }
}