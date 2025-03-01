package net.moss.resonance.block;

import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.moss.resonance.util.Utils;

public class LostSoulBlock extends EmptyLikeBlock {
    public LostSoulBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!Utils.canMobSpawn(world, pos) || random.nextInt(state.get(DURATION)) == 0) { world.removeBlock(pos, false); }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(3) == 0) { return; }
        double d = pos.getX() + 0.9 - random.nextFloat() * 0.9f;
        double f = pos.getZ() + 0.9 - random.nextFloat() * 0.9f;
        world.addParticle(ParticleTypes.SOUL, d, pos.getY(), f, random.nextGaussian() * 0.005, 0, random.nextGaussian() * 0.005);
    }
}