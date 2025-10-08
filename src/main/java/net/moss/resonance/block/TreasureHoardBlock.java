package net.moss.resonance.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.moss.resonance.particle.ModParticles;

public class TreasureHoardBlock extends HeapBlock {
	public TreasureHoardBlock(AbstractBlock.Settings settings) {
		super(8, 8, 12, 12, 7, settings);
	}

	public TreasureHoardBlock(float radius1, float radius2, float boundingHeight1, float boundingHeight2, float collisionHeight, AbstractBlock.Settings settings) {
		super(radius1, radius2, boundingHeight1, boundingHeight2, collisionHeight, settings);
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (random.nextInt(2) != 0) { return; }
		world.addParticle(ModParticles.GOLDEN_GLITTER, pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat(), pos.getZ() + random.nextFloat(), 0.1, 0.1, 0.1);
	}
}