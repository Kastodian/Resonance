package net.moss.resonance.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.moss.resonance.Resonance;
import net.minecraft.state.property.Properties;
import org.jetbrains.annotations.Nullable;

public class BlockEraserStructureProcessor extends StructureProcessor {
	public static final MapCodec<BlockEraserStructureProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("erasable_blocks").forGetter(processor -> processor.erasableBlocks),
			Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(processor -> processor.chance)
	).apply(instance, BlockEraserStructureProcessor::new));
	private final RegistryEntryList<Block> erasableBlocks;
	private final float chance;

	private BlockEraserStructureProcessor(RegistryEntryList<Block> erasableBlocks, float chance) {
		this.chance = chance;
		this.erasableBlocks = erasableBlocks;
	}

	@Nullable
	@Override
	public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
		if (originalBlockInfo.state().isIn(this.erasableBlocks) && data.getRandom(currentBlockInfo.pos()).nextFloat() < this.chance) {
			return originalBlockInfo.state().contains(Properties.WATERLOGGED) && originalBlockInfo.state().get(Properties.WATERLOGGED) ? new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(), Blocks.WATER.getDefaultState(), null) : new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(), Blocks.AIR.getDefaultState(), null);
		}
		return currentBlockInfo;
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return Resonance.BLOCK_ERASER_STRUCTURE_PROCESSOR_TYPE;
	}
}