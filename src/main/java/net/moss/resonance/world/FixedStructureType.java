package net.moss.resonance.world;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.EmptyPoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.alias.StructurePoolAliasBinding;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkGenerator;
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
			Codec.INT.fieldOf("x").forGetter(structure -> structure.x),//
			HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
			Codec.INT.fieldOf("z").forGetter(structure -> structure.z),//
			Codec.INT.optionalFieldOf("grid_offset", 0).forGetter(structure -> structure.gridOffset),//
			Codec.BOOL.optionalFieldOf("multiple_of_coords", false).forGetter(structure -> structure.multipleOf),//
			BlockRotation.CODEC.optionalFieldOf("fixed_rotation").forGetter(structure -> structure.fixedRotation),//
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
	public final int gridOffset;
	public final boolean multipleOf;
	public final Optional<BlockRotation> fixedRotation;
	private final boolean useExpansionHack;
	private final Optional<Heightmap.Type> projectStartToHeightmap;
	private final int maxDistanceFromCenter;
	private final List<StructurePoolAliasBinding> poolAliasBindings;
	private final DimensionPadding dimensionPadding;
	private final StructureLiquidSettings liquidSettings;

	public FixedStructureType(Structure.Config config, RegistryEntry<StructurePool> startPool, Optional<Identifier> startJigsawName, int size, int x, HeightProvider startHeight, int z, int gridOffset, boolean multipleOf, Optional<BlockRotation> fixedRotation, boolean useExpansionHack, Optional<Heightmap.Type> projectStartToHeightmap, int maxDistanceFromCenter, List<StructurePoolAliasBinding> poolAliasBindings, DimensionPadding dimensionPadding, StructureLiquidSettings liquidSettings) {
		super(config);
		this.startPool = startPool;
		this.startJigsawName = startJigsawName;
		this.size = size;
		this.x = x;
		this.startHeight = startHeight;
		this.z = z;
		this.gridOffset = gridOffset;
		this.multipleOf = multipleOf;
		this.fixedRotation = fixedRotation;
		this.useExpansionHack = useExpansionHack;
		this.projectStartToHeightmap = projectStartToHeightmap;
		this.maxDistanceFromCenter = maxDistanceFromCenter;
		this.poolAliasBindings = poolAliasBindings;
		this.dimensionPadding = dimensionPadding;
		this.liquidSettings = liquidSettings;
	}

	public FixedStructureType(Structure.Config config, RegistryEntry<StructurePool> startPool, int size, int x, HeightProvider startHeight, int z, boolean useExpansionHack, Heightmap.Type projectStartToHeightmap) {
		this(config, startPool, Optional.empty(), size, x, startHeight, z, 0, false, Optional.empty(), useExpansionHack, Optional.of(projectStartToHeightmap), 80, List.of(), JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);
	}

	public FixedStructureType(Structure.Config config, RegistryEntry<StructurePool> startPool, int size, int x, HeightProvider startHeight, int z, boolean useExpansionHack) {
		this(config, startPool, Optional.empty(), size, x, startHeight, z, 0, false, Optional.empty(), useExpansionHack, Optional.empty(), 80, List.of(), JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);
	}

	@Override
	public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
		BlockPos blockPos;
		if (multipleOf) {
			ChunkPos chunkPos = context.chunkPos();
			blockPos = new BlockPos((chunkPos.getStartX() / x) * x + gridOffset, this.startHeight.get(context.random(), new HeightContext(context.chunkGenerator(), context.world())), (chunkPos.getStartZ() / z) * z + gridOffset);
		} else {
			blockPos = new BlockPos(x, this.startHeight.get(context.random(), new HeightContext(context.chunkGenerator(), context.world())), z);
		}
		if (this.fixedRotation.isPresent()) {
			return generate(context, this.fixedRotation.get(), this.startPool, this.startJigsawName, this.size, blockPos, this.maxDistanceFromCenter, this.liquidSettings);
		}
		return StructurePoolBasedGenerator.generate(context, this.startPool, this.startJigsawName, this.size, blockPos, this.useExpansionHack, this.projectStartToHeightmap, this.maxDistanceFromCenter, StructurePoolAliasLookup.create(this.poolAliasBindings, blockPos, context.seed()), this.dimensionPadding, this.liquidSettings);
	}

	@Override
	public StructureType<?> getType() {
		return Resonance.FIXED_STRUCTURE_TYPE;
	}

	public static Optional<StructurePosition> generate(Context context, BlockRotation blockRotation, RegistryEntry<StructurePool> structurePool, Optional<Identifier> id, int size, BlockPos pos, int maxDistanceFromCenter, StructureLiquidSettings liquidSettings) {
		DynamicRegistryManager dynamicRegistryManager = context.dynamicRegistryManager();
		ChunkGenerator chunkGenerator = context.chunkGenerator();
		StructureTemplateManager structureTemplateManager = context.structureTemplateManager();
		HeightLimitView heightLimitView = context.world();
		ChunkRandom chunkRandom = context.random();
		Registry<StructurePool> registry = dynamicRegistryManager.get(RegistryKeys.TEMPLATE_POOL);
		StructurePoolElement structurePoolElement = structurePool.value().getRandomElement(chunkRandom);
		if (structurePoolElement == EmptyPoolElement.INSTANCE) {
			return Optional.empty();
		} else {
			BlockPos blockPos;
			if (id.isPresent()) {
				Identifier identifier = id.get();
				Optional<BlockPos> optional = findStartingJigsawPos(structurePoolElement, identifier, pos, blockRotation, structureTemplateManager, chunkRandom);
				if (optional.isEmpty()) {
					Resonance.LOGGER.error("No starting jigsaw {} found in start pool {}", identifier, structurePool.getKey().map(key -> key.getValue().toString()).orElse("<unregistered>"));
					return Optional.empty();
				}
				blockPos = optional.get();
			} else {
				blockPos = pos;
			}

			Vec3i vec3i = blockPos.subtract(pos);
			BlockPos blockPos2 = pos.subtract(vec3i);
			PoolStructurePiece poolStructurePiece = new PoolStructurePiece(structureTemplateManager, structurePoolElement, blockPos2, structurePoolElement.getGroundLevelDelta(), blockRotation, structurePoolElement.getBoundingBox(structureTemplateManager, blockPos2, blockRotation), liquidSettings);
			BlockBox blockBox = poolStructurePiece.getBoundingBox();
			int i = (blockBox.getMaxX() + blockBox.getMinX()) / 2;
			int j = (blockBox.getMaxZ() + blockBox.getMinZ()) / 2;
			int k = blockPos2.getY();

			int l = blockBox.getMinY() + poolStructurePiece.getGroundLevelDelta();
			poolStructurePiece.translate(0, k - l, 0);
			int m = k + vec3i.getY();
			return Optional.of(new StructurePosition(new BlockPos(i, m, j), collector -> {
				List<PoolStructurePiece> list = Lists.<PoolStructurePiece>newArrayList();
				list.add(poolStructurePiece);
				if (size > 0) {
					Box box = new Box(i - maxDistanceFromCenter, Math.max(m - maxDistanceFromCenter, heightLimitView.getBottomY()), j - maxDistanceFromCenter, i + maxDistanceFromCenter + 1, Math.min(m + maxDistanceFromCenter + 1, heightLimitView.getTopY()), j + maxDistanceFromCenter + 1);
					VoxelShape voxelShape = VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box), VoxelShapes.cuboid(Box.from(blockBox)), BooleanBiFunction.ONLY_FIRST);
					StructurePoolBasedGenerator.generate(context.noiseConfig(), size, true, chunkGenerator, structureTemplateManager, heightLimitView, chunkRandom, registry, poolStructurePiece, list, voxelShape, StructurePoolAliasLookup.EMPTY, liquidSettings);
					list.forEach(collector::addPiece);
				}
			}));
		}
	}

	private static Optional<BlockPos> findStartingJigsawPos(StructurePoolElement pool, Identifier id, BlockPos pos, BlockRotation rotation, StructureTemplateManager structureManager, ChunkRandom random) {
		List<StructureBlockInfo> list = pool.getStructureBlockInfos(structureManager, pos, rotation, random);
		for (StructureBlockInfo structureBlockInfo : list) {
			Identifier identifier = Identifier.tryParse(Objects.requireNonNull(structureBlockInfo.nbt(), () -> structureBlockInfo + " nbt was null").getString("name"));
			if (id.equals(identifier)) { return Optional.of(structureBlockInfo.pos()); }
		}
		return Optional.empty();
	}
}