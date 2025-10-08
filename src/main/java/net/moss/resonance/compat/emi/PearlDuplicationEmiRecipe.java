package net.moss.resonance.compat.emi;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.PearlStatsComponent;

public class PearlDuplicationEmiRecipe extends EmiPatternCraftingRecipe {
	private static final List<Potion> POTIONS = Registries.POTION.stream().collect(Collectors.toList());
	private final Item pearl;

	public PearlDuplicationEmiRecipe(Item pearl, Identifier id) {
		super(List.of(EmiStack.of(Items.ECHO_SHARD), EmiStack.of(Items.ECHO_SHARD), EmiStack.of(Items.ECHO_SHARD), EmiStack.of(Items.ECHO_SHARD), EmiStack.of(pearl)), EmiStack.of(pearl, 2), id);
		this.pearl = pearl;
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		if (slot % 2 == 1) { return new SlotWidget(EmiStack.of(Items.ECHO_SHARD), x, y); }
		else if (slot == 4) { return new GeneratedSlotWidget(r -> EmiStack.of(getPearl(r)), unique, x, y); }
		return new SlotWidget(EmiStack.EMPTY, x, y);
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(r -> EmiStack.of(getPearl(r), 2), unique, x, y);
	}

	private ItemStack getPearl(Random random) {
		Potion potion = POTIONS.get(random.nextInt(POTIONS.size()));
		ItemStack stack = pearl.getDefaultStack();
		stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Registries.POTION.getEntry(potion)));
		stack.set(Resonance.PEARL_STATS, PearlStatsComponent.randomStats(random, 16));
		return stack;
	}
}