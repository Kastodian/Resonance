package net.moss.resonance.world;

import java.util.List;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
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
        float pitch = spurFeatureConfig.getPitch().get(random) * MathHelper.RADIANS_PER_DEGREE;
        float yaw = spurFeatureConfig.getYaw().get(random) * MathHelper.RADIANS_PER_DEGREE;
        float radius = spurFeatureConfig.getRadius().get(random) - 0.5f + random.nextFloat();
        int lengthRatio = spurFeatureConfig.getlengthRatio().get(random);
        int length = (int) (lengthRatio * radius);
        BlockPredicate target = spurFeatureConfig.getTarget();
        BlockStateProvider state = spurFeatureConfig.getStateProvider();
        List<BlockPos> blocks = BlockPos.stream(-length, -length, -length, length, length, length).map(BlockPos::toImmutable).toList();
        blocks.forEach(blockPos -> {
            Vec3d vec2 = (new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ())).rotateX(-pitch).rotateZ(-yaw);
            if (vec2.isWithinRangeOf(Vec3d.ZERO, radius - (vec2.getY() / lengthRatio), length) && vec2.isWithinRangeOf(Vec3d.ZERO, radius + (vec2.getY() / lengthRatio), length)) {
                blockPos = blockPos.add(origin);
                if (target.test(structureWorldAccess, blockPos)) {
                    structureWorldAccess.setBlockState(blockPos, state.get(random, blockPos), Block.NOTIFY_ALL);
                }
            }
        });
        return true;
    }
}
