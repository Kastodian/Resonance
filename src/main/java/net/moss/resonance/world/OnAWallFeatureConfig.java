package net.moss.resonance.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;

public class OnAWallFeatureConfig implements FeatureConfig {
	public static final Codec<OnAWallFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			IntProvider.createValidatingCodec(0, 5).fieldOf("reach").forGetter(config -> config.reach),
			IntProvider.createValidatingCodec(1, 10).fieldOf("height").forGetter(config -> config.height),
			IntProvider.createValidatingCodec(1, 1024).fieldOf("desired_wall_size").forGetter(config -> config.desiredWallSize),
			BlockStateProvider.TYPE_CODEC.fieldOf("wall_state").forGetter(config -> config.wallStateProvider),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("floor_state", SimpleBlockStateProvider.of(Blocks.AIR)).forGetter(config -> config.floorStateProvider),
			BlockPredicate.BASE_CODEC.fieldOf("target").forGetter(config -> config.target)
	).apply(instance, OnAWallFeatureConfig::new));
	public final IntProvider reach;
	public final IntProvider height;
	public final IntProvider desiredWallSize;
	public final BlockStateProvider wallStateProvider;
	public final BlockStateProvider floorStateProvider;
	public final BlockPredicate target;

	public OnAWallFeatureConfig(IntProvider reach, IntProvider height, IntProvider desiredWallSize, BlockStateProvider wallStateProvider, BlockStateProvider floorStateProvider, BlockPredicate target) {
		this.reach = reach;
		this.height = height;
		this.desiredWallSize = desiredWallSize;
		this.wallStateProvider = wallStateProvider;
		this.floorStateProvider = floorStateProvider;
		this.target = target;
	}
}