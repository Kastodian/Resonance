package net.moss.resonance.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;

public class OnAWallFeatureConfig implements FeatureConfig {
	public static final Codec<OnAWallFeatureConfig> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					IntProvider.createValidatingCodec(0, 5).fieldOf("reach").forGetter(config -> config.reach),
					IntProvider.createValidatingCodec(1, 10).fieldOf("height").forGetter(config -> config.height),
					IntProvider.createValidatingCodec(1, 1024).fieldOf("desired_wall_size").forGetter(config -> config.desiredWallSize),
                    BlockStateProvider.TYPE_CODEC.fieldOf("wall_state").forGetter(config -> config.wallStateProvider),
                    BlockStateProvider.TYPE_CODEC.optionalFieldOf("floor_state", SimpleBlockStateProvider.of(Blocks.AIR)).forGetter(config -> config.floorStateProvider),
					Codec.BOOL.optionalFieldOf("allign_facing_to_wall", false).forGetter(config -> config.allignFacingToWall),
					BlockStateProvider.TYPE_CODEC.fieldOf("targets").forGetter(config -> config.targets)
				)
				.apply(instance, OnAWallFeatureConfig::new)
	);
    private final IntProvider reach;
	private final IntProvider height;
	private final IntProvider desiredWallSize;
	private final BlockStateProvider wallStateProvider;
	private final BlockStateProvider floorStateProvider;
	private final Boolean allignFacingToWall;
	private final BlockStateProvider targets;

	public OnAWallFeatureConfig(IntProvider reach, IntProvider height, IntProvider desiredWallSize, BlockStateProvider wallStateProvider, BlockStateProvider floorStateProvider, Boolean allignFacingToWall, BlockStateProvider targets) {
		this.reach = reach;
		this.height = height;
		this.desiredWallSize = desiredWallSize;
		this.wallStateProvider = wallStateProvider;
		this.floorStateProvider = floorStateProvider;
		this.allignFacingToWall = allignFacingToWall;
		this.targets = targets;
	}

	public IntProvider getReach() { return this.reach; }
	public IntProvider getHeight() { return this.height; }
	public IntProvider getDesiredWallSize() { return this.desiredWallSize; }
	public BlockStateProvider getWallStateProvider() { return this.wallStateProvider; }
	public BlockStateProvider getFloorStateProvider() { return this.floorStateProvider; }
	public Boolean getAllignFacingToWall() { return this.allignFacingToWall; }
	public BlockStateProvider getTargets() { return this.targets; }
}