package net.moss.resonance.util;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Degradable;
import net.minecraft.util.StringIdentifiable;
import net.moss.resonance.Resonance;

public interface Dischargable extends Degradable<Dischargable.ChargeLevel> {
	Supplier<BiMap<Block, Block>> CHARGE_DECREASES = Suppliers.memoize(
		() -> ImmutableBiMap.<Block, Block>builder()
				.put(Resonance.CHARGED_AMETHYST_CLUSTER, Resonance.LARGE_CHARGED_AMETHYST_BUD)
				.put(Resonance.LARGE_CHARGED_AMETHYST_BUD, Resonance.MEDIUM_CHARGED_AMETHYST_BUD)
				.put(Resonance.MEDIUM_CHARGED_AMETHYST_BUD, Resonance.SMALL_CHARGED_AMETHYST_BUD)
				.put(Resonance.SMALL_CHARGED_AMETHYST_BUD, Blocks.SMALL_AMETHYST_BUD)
				.build()
	);
	Supplier<BiMap<Block, Block>> CHARGE_INCREASES = Suppliers.memoize(() -> CHARGE_DECREASES.get().inverse());


	static Block getMaxChargedBlock(Block block) {
		Block block2 = block;
		for (Block block3 = CHARGE_INCREASES.get().get(block); block3 != null; block3 = CHARGE_INCREASES.get().get(block3)) { block2 = block3; }
		return block2;
	}

	static Optional<Block> getIncreasedChargeBlock(Block block) { return Optional.ofNullable(CHARGE_INCREASES.get().get(block)); }
	static Optional<BlockState> getIncreasedChargeState(BlockState state) { return getIncreasedChargeBlock(state.getBlock()).map(block -> block.getStateWithProperties(state)); }
	static Optional<Block> getDecreasedChargeBlock(Block block) { return Optional.ofNullable(CHARGE_DECREASES.get().get(block)); }
	static BlockState getMaxChargedState(BlockState state) { return getMaxChargedBlock(state.getBlock()).getStateWithProperties(state); }
	@Override default Optional<BlockState> getDegradationResult(BlockState state) { return getDecreasedChargeBlock(state.getBlock()).map(block -> block.getStateWithProperties(state)); }
	@Override default float getDegradationChanceMultiplier() { return 1.0F; }

	public static enum ChargeLevel implements StringIdentifiable {
		OVERCHARGED(0, "overcharged"),
		STRONG(1, "strong"),
		MEDIUM(2, "medium"),
		WEAK(3, "weak");

		public static final Codec<Dischargable.ChargeLevel> CODEC = StringIdentifiable.createCodec(Dischargable.ChargeLevel::values);
		private final int number;
		private final String id;

		private ChargeLevel(final int number, final String id) {
			this.number = number;
			this.id = id;
		}

		public int getNumber() {
			return this.number;
		}

		@Override
		public String asString() {
			return this.id;
		}
	}
}
