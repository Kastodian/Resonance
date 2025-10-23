package net.moss.resonance.compat.emi;

import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.block.SuspiciousStewIngredient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.component.type.SuspiciousStewEffectsComponent.StewEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;
import net.moss.resonance.item.Pearl;

// TODO: make this simpler and reflect stats, durability, and blacklist
public class PearlGestationEmiRecipe implements EmiRecipe {
	private static final List<Potion> POTIONS = Registries.POTION.stream().collect(Collectors.toList());
	private static final List<SuspiciousStewIngredient> STEW = SuspiciousStewIngredient.getAll();
	private final Identifier id;
	private final List<EmiIngredient> input;
	private final List<EmiStack> results;
	private final boolean override;

	public PearlGestationEmiRecipe(EmiIngredient input, ItemStack potion, ItemStack pearl, boolean override, Identifier id) {
		this.id = id;
		this.override = override;
		this.input = List.of(EmiStack.of(Items.ICE), input, EmiStack.of(potion));
		this.results = List.of(EmiStack.of(pearl), EmiStack.of(Resonance.PEARL_ICE));
	}

	public PearlGestationEmiRecipe(EmiIngredient input, ItemStack pearl, RegistryEntry<Potion> potion, Identifier id) {
		this.id = id;
		this.override = false;
		this.input = List.of(EmiStack.of(Items.ICE), input, EmiStack.of(setPotion(Items.POTION.getDefaultStack(), potion)));
		this.results = List.of(EmiStack.of(setPotion(pearl, potion)), EmiStack.of(Resonance.PEARL_ICE));
	}

	public PearlGestationEmiRecipe(EmiIngredient input, ItemStack pearl, int ominous, Identifier id) {
		ItemStack potion = new ItemStack(Items.OMINOUS_BOTTLE);
		potion.set(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER, ominous);
		PotionContentsComponent potionComponent = PotionContentsComponent.DEFAULT.with(new StatusEffectInstance(StatusEffects.BAD_OMEN, 120000, ominous));
		pearl.set(DataComponentTypes.POTION_CONTENTS, potionComponent);
		this.id = id;
		this.override = false;
		this.input = List.of(EmiStack.of(Items.ICE), input, EmiStack.of(potion));
		this.results = List.of(EmiStack.of(pearl), EmiStack.of(Resonance.PEARL_ICE));
	}

	public PearlGestationEmiRecipe(Identifier id) {
		this.id = id;
		this.override = true;
		this.input = List.of(EmiStack.of(Items.ICE), EmiStack.of(Resonance.PEARL), EmiStack.of(Resonance.PEARL));
		this.results = List.of(EmiStack.of(Resonance.BIFURCATED_PEARL), EmiStack.of(Resonance.PEARL_ICE));
	}

	public static ItemStack setPotion(ItemStack stack, RegistryEntry<Potion> potion) {
		stack.apply(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT, potion, PotionContentsComponent::with);
		return stack;
	}

	@Override public EmiRecipeCategory getCategory() { return EmiCompat.PEARL_GESTATION; }
	@Override public @Nullable Identifier getId() { return id; }
	@Override public List<EmiIngredient> getInputs() { return input; }
	@Override public List<EmiStack> getOutputs() { return results; }
	@Override public int getDisplayWidth() { return 132; }
	@Override public int getDisplayHeight() { return 22; }

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 16, 2);
		widgets.addSlot(input.get(0), 0, 2);

		if (override) {
			ItemStack stack1 = input.get(1).getEmiStacks().getFirst().getItemStack();
			ItemStack stack2 = input.get(2).getEmiStacks().getFirst().getItemStack();
			ItemStack result = results.get(0).getItemStack();
			if (stack1.getItem() instanceof Pearl) { widgets.add(randomPotion(stack1, false, false, 36, 2)).recipeContext(this); }
			else { widgets.addSlot(input.get(1), 36, 2).recipeContext(this); }

			if (stack2.isOf(Resonance.PEARL)) { widgets.add(randomPotion(stack2, true, false, 54, 2)).recipeContext(this); }
			else if (stack2.isOf(Resonance.BIFURCATED_PEARL)) { widgets.add(randomBifurcated(stack2, 54, 2)).recipeContext(this); }
			else { widgets.addSlot(input.get(2), 54, 2).recipeContext(this); }

			if (result.isOf(Resonance.PEARL)) { widgets.add(randomPotion(result, true, true, 90, 2)).recipeContext(this); }
			else if (result.isOf(Resonance.BIFURCATED_PEARL)) { widgets.add(randomBifurcated(result, 90, 2)).recipeContext(this); }
			else { widgets.addSlot(input.get(2), 90, 2).recipeContext(this); }
		} else {
			widgets.addSlot(input.get(1), 36, 2);
			widgets.addSlot(input.get(2), 54, 2);
			ItemStack stack = input.get(2).getEmiStacks().getFirst().getItemStack();
			if (stack.isOf(Items.SUSPICIOUS_STEW)) { widgets.add(randomStew(results.get(0).getItemStack(), 90, 2)).recipeContext(this); }
			else { widgets.addSlot(results.get(0), 90, 2).recipeContext(this); }
		}

		widgets.addSlot(results.get(1), 108, 2).recipeContext(this);
	}

	private GeneratedSlotWidget randomPotion(ItemStack stack, boolean second, boolean product, int x, int y) {
		return new GeneratedSlotWidget((random) -> {
			RegistryEntry<Potion> potion1 = Registries.POTION.getEntry(POTIONS.get(random.nextInt(POTIONS.size())));
			RegistryEntry<Potion> potion2 = Registries.POTION.getEntry(POTIONS.get(random.nextInt(POTIONS.size())));
			stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(second ? potion2 : potion1));
			return EmiStack.of(stack);
		}, 0, x, y);
	}

	private GeneratedSlotWidget randomBifurcated(ItemStack stack, int x, int y) {
		return new GeneratedSlotWidget((random) -> {
			PotionContentsComponent potionComponent = new PotionContentsComponent(Registries.POTION.getEntry(POTIONS.get(random.nextInt(POTIONS.size()))));
			for (StatusEffectInstance effectInstance : Registries.POTION.getEntry(POTIONS.get(random.nextInt(POTIONS.size()))).value().getEffects()) { potionComponent = potionComponent.with(effectInstance); }
			stack.set(DataComponentTypes.POTION_CONTENTS, potionComponent);
			return EmiStack.of(stack);
		}, 0, x, y);
	}

	private GeneratedSlotWidget randomStew(ItemStack stack, int x, int y) {
		return new GeneratedSlotWidget((random) -> {
			PotionContentsComponent potionComponent = PotionContentsComponent.DEFAULT;
			for (StewEffect effect : STEW.get(random.nextInt(STEW.size())).getStewEffects().effects()) {
				potionComponent = potionComponent.with(new StatusEffectInstance(effect.effect(), effect.duration(), 0));
			}
			stack.set(DataComponentTypes.POTION_CONTENTS, potionComponent);
			return EmiStack.of(stack);
		}, 0, x, y);
	}
}