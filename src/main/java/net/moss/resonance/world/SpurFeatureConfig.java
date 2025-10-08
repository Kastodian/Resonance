package net.moss.resonance.world;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class SpurFeatureConfig implements FeatureConfig {
	public static final Codec<SpurFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			IntProvider.createValidatingCodec(0, 360).fieldOf("pitch").forGetter(config -> config.pitch),
			IntProvider.createValidatingCodec(0, 360).fieldOf("yaw").forGetter(config -> config.yaw),
			IntProvider.createValidatingCodec(1, 64).fieldOf("radius").forGetter(config -> config.radius),
			IntProvider.createValidatingCodec(1, 64).fieldOf("length_ratio").forGetter(config -> config.lengthRatio),
			BlockStateProvider.TYPE_CODEC.fieldOf("state_provider").forGetter(config -> config.stateProvider),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("inner_state_provider").forGetter(config -> config.innerStateProvider),
			BlockPredicate.BASE_CODEC.fieldOf("target").forGetter(config -> config.target),
			PlacedFeature.REGISTRY_CODEC.optionalFieldOf("top_tip_feature").forGetter(config -> config.topFeature),
			PlacedFeature.REGISTRY_CODEC.optionalFieldOf("bottom_tip_feature").forGetter(config -> config.bottomFeature)
	).apply(instance, SpurFeatureConfig::new));
	public final IntProvider pitch;
	public final IntProvider yaw;
	public final IntProvider radius;
	public final IntProvider lengthRatio;
	public final BlockStateProvider stateProvider;
	public final Optional<BlockStateProvider> innerStateProvider;
	public final BlockPredicate target;
	public final Optional<RegistryEntry<PlacedFeature>> topFeature;
	public final Optional<RegistryEntry<PlacedFeature>> bottomFeature;

	public SpurFeatureConfig(IntProvider pitch, IntProvider yaw, IntProvider radius, IntProvider lengthRatio, BlockStateProvider stateProvider, Optional<BlockStateProvider> innerStateProvider, BlockPredicate target, Optional<RegistryEntry<PlacedFeature>> topFeature, Optional<RegistryEntry<PlacedFeature>> bottomFeature) {
		this.pitch = pitch;
		this.yaw = yaw;
		this.radius = radius;
		this.lengthRatio = lengthRatio;
		this.stateProvider = stateProvider;
		this.innerStateProvider = innerStateProvider;
		this.target = target;
		this.topFeature = topFeature;
		this.bottomFeature = bottomFeature;
	}
}