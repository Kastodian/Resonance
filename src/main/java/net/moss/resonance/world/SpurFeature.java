package net.moss.resonance.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class SpurFeature extends Feature<SpurFeatureConfig> {
	public SpurFeature(Codec<SpurFeatureConfig> configCodec) {
		super(configCodec);
	}

	@Override
	public boolean generate(FeatureContext<SpurFeatureConfig> context) {
		StructureWorldAccess structureWorldAccess = context.getWorld();
		BlockPos origin = context.getOrigin();
		Random random = context.getRandom();
		SpurFeatureConfig spurFeatureConfig = context.getConfig();
		float pitch = spurFeatureConfig.pitch.get(random) * MathHelper.RADIANS_PER_DEGREE;
		float yaw = spurFeatureConfig.yaw.get(random) * MathHelper.RADIANS_PER_DEGREE;
		float radius = spurFeatureConfig.radius.get(random) - 0.5f + random.nextFloat();
		int lengthRatio = spurFeatureConfig.lengthRatio.get(random);
		int length = (int) (lengthRatio * radius);
		BlockPredicate target = spurFeatureConfig.target;
		BlockStateProvider state = spurFeatureConfig.stateProvider;
		boolean innerPresent = spurFeatureConfig.innerStateProvider.isPresent();
		BlockStateProvider innerState = innerPresent ? spurFeatureConfig.innerStateProvider.get() : state;

		BlockPos.stream(-length, -length, -length, length, length, length).map(BlockPos::toImmutable).forEach(blockPos -> {
			BlockPos realBlockPos = blockPos.add(origin);
			if (structureWorldAccess.isOutOfHeightLimit(realBlockPos) || !target.test(structureWorldAccess, realBlockPos)) { return; }

			Vec3d vec2 = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).rotateX(-pitch).rotateZ(-yaw);
			if (Math.abs(vec2.y) >= length) { return; }

			double squaredHypot = MathHelper.squaredHypot(vec2.x, vec2.z);
			double radiusMinus = MathHelper.square(radius - (vec2.getY() / lengthRatio));
			double radiusPlus = MathHelper.square(radius + (vec2.getY() / lengthRatio));
			if (squaredHypot >= radiusMinus || squaredHypot >= radiusPlus) { return; }

			if (innerPresent && squaredHypot + radius < radiusMinus && squaredHypot + radius < radiusPlus) {
				structureWorldAccess.setBlockState(realBlockPos, innerState.get(random, realBlockPos), Block.NOTIFY_LISTENERS);
			} else {
				structureWorldAccess.setBlockState(realBlockPos, state.get(random, realBlockPos), Block.NOTIFY_LISTENERS);
			}
		});

		BlockPos pos = BlockPos.ofFloored(new Vec3d(0, length * 0.9, 0).rotateZ(yaw).rotateX(pitch));
		ChunkGenerator generator = context.getGenerator();
		if (spurFeatureConfig.topFeature.isPresent()) { spurFeatureConfig.topFeature.get().value().generateUnregistered(structureWorldAccess, generator, random, pos.add(origin)); }
		if (spurFeatureConfig.bottomFeature.isPresent()) { spurFeatureConfig.bottomFeature.get().value().generateUnregistered(structureWorldAccess, generator, random, origin.subtract(pos)); }

		return true;
	}
}