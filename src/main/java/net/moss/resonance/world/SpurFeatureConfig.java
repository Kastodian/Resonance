package net.moss.resonance.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class SpurFeatureConfig implements FeatureConfig {
    public static final Codec<SpurFeatureConfig> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					IntProvider.createValidatingCodec(0, 360).fieldOf("pitch").forGetter(config -> config.pitch),
					IntProvider.createValidatingCodec(0, 360).fieldOf("yaw").forGetter(config -> config.yaw),
					IntProvider.createValidatingCodec(1, 64).fieldOf("radius").forGetter(config -> config.radius),
					IntProvider.createValidatingCodec(1, 64).fieldOf("length_ratio").forGetter(config -> config.lengthRatio),
                    BlockStateProvider.TYPE_CODEC.fieldOf("state_provider").forGetter(config -> config.stateProvider),
					BlockPredicate.BASE_CODEC.fieldOf("target").forGetter(config -> config.target)
				)
				.apply(instance, SpurFeatureConfig::new)
	);
    private final IntProvider pitch;
	private final IntProvider yaw;
	private final IntProvider radius;
	private final IntProvider lengthRatio;
	private final BlockStateProvider stateProvider;
	private final BlockPredicate target;

    public SpurFeatureConfig(IntProvider pitch, IntProvider yaw, IntProvider radius, IntProvider lengthRatio, BlockStateProvider stateProvider, BlockPredicate target) {
        this.pitch = pitch;
		this.yaw = yaw;
		this.radius = radius;
		this.lengthRatio = lengthRatio;
		this.stateProvider = stateProvider;
		this.target = target;
    }

	public IntProvider getPitch() { return this.pitch; }
	public IntProvider getYaw() { return this.yaw; }
	public IntProvider getRadius() { return this.radius; }
	public IntProvider getlengthRatio() { return this.lengthRatio; }
	public BlockStateProvider getStateProvider() { return this.stateProvider; }
	public BlockPredicate getTarget() { return this.target; }
}
