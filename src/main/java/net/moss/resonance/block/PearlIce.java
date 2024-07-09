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

public class PearlIce extends SnowyBlock {
    public PearlIce(Settings settings) { super(settings); }
    @Override protected boolean hasRandomTicks(BlockState state) { return true; }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        for (int i = 0; i < 7; i++) {
            BlockPos blockPos = pos.add(random.nextBetween(0, 6) - 3, random.nextBetween(0, 6) - 3, random.nextBetween(0, 6) - 3);
            BlockState state2 = world.getBlockState(blockPos);
            BlockState state3 = world.getBlockState(blockPos.up());
            if (state2.isOf(Blocks.WATER) && (state3.isAir() || state3.isIn(BlockTags.ICE)) && world.getLightLevel(LightType.BLOCK, blockPos) < 11) {
                if (state2.get(FluidBlock.LEVEL) == 0) {
                    for (Direction direction : Direction.values()) {
                        if (direction != Direction.DOWN && world.getBlockState(blockPos.offset(direction)).isSideSolidFullSquare(world, blockPos.offset(direction), direction.getOpposite())) {
                            world.setBlockState(blockPos, Blocks.ICE.getDefaultState(), Block.NOTIFY_ALL);
                            break;
                        }
                    }
                }
            }
        }
    }
}