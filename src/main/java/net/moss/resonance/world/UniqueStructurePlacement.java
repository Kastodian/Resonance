package net.moss.resonance.world;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;
import net.moss.resonance.Resonance;

public class UniqueStructurePlacement extends StructurePlacement {
	public static final MapCodec<UniqueStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.intRange(0, 1023).optionalFieldOf("distance", 0).forGetter(UniqueStructurePlacement::getDistance),
			RegistryCodecs.entryList(RegistryKeys.BIOME).fieldOf("biomes").forGetter(UniqueStructurePlacement::getBiomes),
			Codec.intRange(0, 63).optionalFieldOf("biome_depth", 0).forGetter(UniqueStructurePlacement::getBiomeDepth),
			Codec.intRange(-1023, 1023).optionalFieldOf("test_y", 80).forGetter(UniqueStructurePlacement::getTestHeight)
	).apply(instance, UniqueStructurePlacement::new));
	private final int distance;
	private final RegistryEntryList<Biome> biomes;
	private final int biomeDepth;
	private final int testHeight;
	private int chunkX = Integer.MAX_VALUE;
	private int chunkZ = Integer.MAX_VALUE;

	@SuppressWarnings("deprecation")
	protected UniqueStructurePlacement(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt, Optional<ExclusionZone> exclusionZone, int distance, RegistryEntryList<Biome> biomes, int biomeDepth, int testHeight) {
		super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
		this.distance = distance;
		this.biomes = biomes;
		this.biomeDepth = biomeDepth;
		this.testHeight = testHeight;
	}

	public UniqueStructurePlacement(int distance, RegistryEntryList<Biome> biomes, int biomeDepth, int testHeight) {
		this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0f, 0, Optional.empty(), distance, biomes, biomeDepth, testHeight);
	}

	public void setChunk(int chunkX, int chunkZ) {
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
	}

	@Override protected boolean isStartChunk(StructurePlacementCalculator calculator, int chunkX, int chunkZ) { return (this.chunkX == chunkX) && (this.chunkZ == chunkZ); }
	@Override public StructurePlacementType<?> getType() { return Resonance.UNIQUE_PLACEMENT_TYPE; }
	public RegistryEntryList<Biome> getBiomes() { return this.biomes; }
	public int getBiomeDepth() { return this.biomeDepth; }
	public int getTestHeight() { return this.testHeight; }
	public int getDistance() { return this.distance; }
}