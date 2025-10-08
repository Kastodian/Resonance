package net.moss.resonance;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;

public record FreezeThawing(RegistryEntryList<Block> input, BlockStateProvider output, int time) {
	public static final Codec<FreezeThawing> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("input").forGetter(FreezeThawing::input),
			BlockStateProvider.TYPE_CODEC.fieldOf("output").forGetter(FreezeThawing::output),
			Codecs.POSITIVE_INT.optionalFieldOf("time", 10).forGetter(FreezeThawing::time)
	).apply(instance, FreezeThawing::new));
}