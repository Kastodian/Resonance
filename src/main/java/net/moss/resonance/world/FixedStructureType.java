package net.moss.resonance.world;

import java.util.List;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.alias.StructurePoolAliasBinding;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.structure.DimensionPadding;
import net.minecraft.world.gen.structure.JigsawStructure;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import net.moss.resonance.Resonance;

public class FixedStructureType extends Structure {
	public static final MapCodec<FixedStructureType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            configCodecBuilder(instance),
            StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
            Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
            Codec.intRange(0, 20).fieldOf("size").forGetter(structure -> structure.size),
            Codec.INT.fieldOf("x").forGetter(structure -> structure.x),
            HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
            Codec.INT.fieldOf("z").forGetter(structure -> structure.z),
            Codec.INT.optionalFieldOf("center_x", 0).forGetter(structure -> structure.centerX),
            Codec.INT.optionalFieldOf("center_z", 0).forGetter(structure -> structure.centerZ),
            Codec.BOOL.optionalFieldOf("multiple_of_coords", false).forGetter(structure -> structure.multipleOf),
            Codec.BOOL.fieldOf("use_expansion_hack").forGetter(structure -> structure.useExpansionHack),
            Heightmap.Type.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
            Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter),
            Codec.list(StructurePoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter(structure -> structure.poolAliasBindings),
            DimensionPadding.CODEC.optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING).forGetter(structure -> structure.dimensionPadding),
            StructureLiquidSettings.codec.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings))
			.apply(instance, FixedStructureType::new));

	private final RegistryEntry<StructurePool> startPool;
	private final Optional<Identifier> startJigsawName;
	private final int size;
	public final int x;
	private final HeightProvider startHeight;
	public final int z;
	public final int centerX;
	public final int centerZ;
	public final boolean multipleOf;
	private final boolean useExpansionHack;
	private final Optional<Heightmap.Type> projectStartToHeightmap;
	private final int maxDistanceFromCenter;
	private final List<StructurePoolAliasBinding> poolAliasBindings;
	private final DimensionPadding dimensionPadding;
	private final StructureLiquidSettings liquidSettings;
    
	public FixedStructureType(Structure.Config config, RegistryEntry<StructurePool> startPool, Optional<Identifier> startJigsawName, int size, int x, HeightProvider startHeight, int z, int centerX, int centerZ, boolean multipleOf, boolean useExpansionHack, Optional<Heightmap.Type> projectStartToHeightmap, int maxDistanceFromCenter, List<StructurePoolAliasBinding> poolAliasBindings, DimensionPadding dimensionPadding, StructureLiquidSettings liquidSettings) {
		super(config);
		this.startPool = startPool;
		this.startJigsawName = startJigsawName;
		this.size = size;
		this.x = x;
		this.startHeight = startHeight;
		this.z = z;
		this.centerX = centerX;
		this.centerZ = centerZ;
		this.multipleOf = multipleOf;
		this.useExpansionHack = useExpansionHack;
		this.projectStartToHeightmap = projectStartToHeightmap;
		this.maxDistanceFromCenter = maxDistanceFromCenter;
		this.poolAliasBindings = poolAliasBindings;
		this.dimensionPadding = dimensionPadding;
		this.liquidSettings = liquidSettings;
	}

	public FixedStructureType(Structure.Config config, RegistryEntry<StructurePool> startPool, int size, int x, HeightProvider startHeight, int z, boolean useExpansionHack, Heightmap.Type projectStartToHeightmap) {
		this(config, startPool, Optional.empty(), size, x, startHeight, z, 0, 0, false, useExpansionHack, Optional.of(projectStartToHeightmap), 80, List.of(), JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);
	}

	public FixedStructureType(Structure.Config config, RegistryEntry<StructurePool> startPool, int size, int x, HeightProvider startHeight, int z, boolean useExpansionHack) {
		this(config, startPool, Optional.empty(), size, x, startHeight, z, 0, 0, false, useExpansionHack, Optional.empty(), 80, List.of(), JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);
	}

	@Override
	public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
		BlockPos blockPos;
		if (multipleOf) {
			ChunkPos chunkPos = context.chunkPos();
			blockPos = new BlockPos((chunkPos.getStartX() / x) * x + centerX, this.startHeight.get(context.random(), new HeightContext(context.chunkGenerator(), context.world())), (chunkPos.getStartZ() / z) * z + centerZ);
		} else {
			blockPos = new BlockPos(x, this.startHeight.get(context.random(), new HeightContext(context.chunkGenerator(), context.world())), z);
		}
		return StructurePoolBasedGenerator.generate(context, this.startPool, this.startJigsawName, this.size, blockPos, this.useExpansionHack, this.projectStartToHeightmap, this.maxDistanceFromCenter, StructurePoolAliasLookup.create(this.poolAliasBindings, blockPos, context.seed()), this.dimensionPadding, this.liquidSettings);
	}

    @Override
    public StructureType<?> getType() {
		return Resonance.FIXED_STRUCTURE_TYPE;
    }
}
