package net.moss.resonance.compat.emi;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.recipe.EmiIngredientRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.block.SuspiciousStewIngredient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;

// TODO: make this simpler and reflect stats, durability
public class PearlGestationEmiRecipe extends EmiIngredientRecipe {
	private static final List<Potion> POTIONS = Registries.POTION.stream().collect(Collectors.toList());
	private static final List<SuspiciousStewIngredient> STEW = SuspiciousStewIngredient.getAll();
	private final Identifier id;
	private final List<EmiIngredient> seeds;
	private final List<EmiStack> potions;
	private final List<EmiStack> results;
	private final boolean randomPotion;

	public PearlGestationEmiRecipe(List<EmiStack> seed, List<Item> potionSources, @Nullable RegistryEntry<Potion> potion, Item result, boolean randomPotion, Identifier id) {
		this.id = id;
		this.randomPotion = randomPotion;
		this.seeds = List.of(EmiStack.of(Items.ICE), EmiIngredient.of(seed));
		this.potions = potionSources.stream().map(potion == null ? e -> EmiStack.of(e) : e -> EmiStack.of(PotionContentsComponent.createStack(e, potion))).toList();
		this.results = List.of(potion == null ? EmiStack.of(result.getDefaultStack()) : pearlWithPotion(result.getDefaultStack(), potion.value().getEffects()), EmiStack.of(Resonance.PEARL_ICE));
	}

	public PearlGestationEmiRecipe(EmiIngredient input, ItemStack pearl, int ominous, Identifier id) {
		ItemStack potion = Items.OMINOUS_BOTTLE.getDefaultStack();
		potion.set(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER, ominous);
		this.id = id;
		this.randomPotion = false;
		this.seeds = List.of(EmiStack.of(Items.ICE), input);
		this.potions = List.of(EmiStack.of(potion));
		this.results = List.of(pearlWithPotion(pearl, List.of(new StatusEffectInstance(StatusEffects.BAD_OMEN, 120000, ominous))), EmiStack.of(Resonance.PEARL_ICE));
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addSlot(seeds.get(0), 0, 2);
		widgets.addTexture(EmiTexture.PLUS, 20, 4);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 72, 2);
		widgets.addSlot(results.get(1), 114, 2).recipeContext(this);

		if (randomPotion) {
			ItemStack stack1 = seeds.get(1).getEmiStacks().getFirst().getItemStack();
			if (stack1.isOf(Resonance.PEARL)) { widgets.add(randomPotion(stack1, true, 36, 2)).recipeContext(this); }
			else { widgets.addSlot(seeds.get(1), 36, 2).recipeContext(this); }
			widgets.add(randomPotion(potions.getFirst().getEmiStacks().getFirst().getItemStack(), false, 54, 2)).recipeContext(this);
			widgets.add(randomPotion(results.get(0).getItemStack(), false, 96, 2)).recipeContext(this);
		} else {
			widgets.addSlot(seeds.get(1), 36, 2);
			widgets.addSlot(this.getInputs().get(2), 54, 2);
			if (potions.getFirst().getEmiStacks().getFirst().getItemStack().isOf(Items.SUSPICIOUS_STEW)) { widgets.add(randomStew(results.get(0).getItemStack(), 96, 2)).recipeContext(this); }
			else { widgets.addSlot(results.get(0), 96, 2).recipeContext(this); }
		}
	}

	private static GeneratedSlotWidget randomPotion(ItemStack stack, boolean first, int x, int y) {
		return new GeneratedSlotWidget((random) -> {
			List<StatusEffectInstance> effects1 = Registries.POTION.getEntry(POTIONS.get(random.nextInt(POTIONS.size()))).value().getEffects();
			List<StatusEffectInstance> effects2 = Registries.POTION.getEntry(POTIONS.get(random.nextInt(POTIONS.size()))).value().getEffects();
			if (stack.isOf(Resonance.BIFURCATED_PEARL)) {
				return pearlWithPotion(stack, Stream.concat(effects1.stream(), effects2.stream()).toList());
			} else {
				return pearlWithPotion(stack, first ? effects1 : effects2);
			}
		}, 0, x, y);
	}

	private static GeneratedSlotWidget randomStew(ItemStack stack, int x, int y) {
		return new GeneratedSlotWidget(r -> pearlWithPotion(stack, STEW.get(r.nextInt(STEW.size())).getStewEffects().effects().stream().map(e -> e.createStatusEffectInstance()).toList()), 0, x, y);
	}

	public static EmiStack pearlWithPotion(ItemStack stack, List<StatusEffectInstance> effects) {
		effects = effects.stream().map(e -> e.getEffectType().isIn(Resonance.PEARL_EFFECT_BLACKLIST_TAG) ?
				new StatusEffectInstance(StatusEffects.UNLUCK, e.getDuration(), e.getAmplifier(), e.isAmbient(), e.shouldShowParticles(), e.shouldShowIcon()) : e).toList();
		stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.empty(), effects));
		return EmiStack.of(stack);
	}

	@Override
	protected EmiIngredient getIngredient() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getIngredient'");
	}

	@Override
	protected EmiRecipe getRecipeContext(EmiStack stack, int offset) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getRecipeContext'");
	}

	@Override public List<EmiIngredient> getInputs() { return List.of(this.seeds.get(0), this.seeds.get(1), super.getInputs().getFirst()); }
	@Override protected List<EmiStack> getStacks() { return this.potions; }
	@Override public boolean supportsRecipeTree() { return true; }
	@Override public EmiRecipeCategory getCategory() { return EmiCompat.PEARL_GESTATION; }
	@Override public @Nullable Identifier getId() { return id; }
	@Override public List<EmiStack> getOutputs() { return results; }
	@Override public int getDisplayWidth() { return 132; }
	@Override public int getDisplayHeight() { return 22; }
}