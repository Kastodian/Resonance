// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package net.moss.resonance.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.dynamic.Codecs;
import net.moss.resonance.Resonance;

public class SetPearlStatsLootFunction extends ConditionalLootFunction {
	public static final MapCodec<SetPearlStatsLootFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
		return addConditionsField(instance).and(
			Codecs.rangedInt(1, 10).fieldOf("duration").forGetter(function -> function.duration))
			.and(Codecs.rangedInt(1, 10).fieldOf("recuperation").forGetter(function -> function.recuperation))
			.and(Codecs.rangedInt(1, 10).fieldOf("strength").forGetter(function -> function.strength))
			.and(Codecs.rangedInt(1, 10).fieldOf("shinyBound").forGetter(function -> function.shinyBound)
		).apply(instance, SetPearlStatsLootFunction::new);
	});
	private final int duration;
	private final int recuperation;
	private final int strength;
	private final int shinyBound;

	private SetPearlStatsLootFunction(List<LootCondition> conditions, int duration, int recuperation, int strength, int shinyBound) {
		super(conditions);
		this.duration = duration;
		this.recuperation = recuperation;
		this.strength = strength;
		this.shinyBound = shinyBound;
	}

	public LootFunctionType<SetPearlStatsLootFunction> getType() {
		return Resonance.SET_PEARL_STATS;
	}

	public ItemStack process(ItemStack stack, LootContext context) {
		stack.set(Resonance.PEARL_STATS, PearlStatsComponent.randomStats(context.getRandom(), this.duration, this.recuperation, this.strength, this.shinyBound));
		return stack;
	}

	public static ConditionalLootFunction.Builder<?> builder(int duration, int recuperation, int strength, int shinyBound) {
		return builder((conditions) -> {
			return new SetPearlStatsLootFunction(conditions, duration, recuperation, strength, shinyBound);
		});
	}
}