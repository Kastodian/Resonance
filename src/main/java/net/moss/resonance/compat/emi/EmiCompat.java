package net.moss.resonance.compat.emi;

import java.util.stream.Collectors;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;
import net.moss.resonance.item.Pearl;
import net.moss.resonance.recipe.LightningStrikeRecipe;
import net.moss.resonance.recipe.ReverberationRecipe;

public class EmiCompat implements EmiPlugin {
	public static final EmiStack CHARGED_AMETHYST = EmiStack.of(Resonance.CHARGED_AMETHYST_CLUSTER);
	public static final EmiStack LIGHTNING_ROD = EmiStack.of(Items.LIGHTNING_ROD);
	public static final EmiStack PEARL_ICE = EmiStack.of(Resonance.PEARL_ICE);
	public static final EmiRecipeCategory REVERBERATION = new EmiRecipeCategory(Identifier.of(Resonance.MOD_ID, "reverberation"), CHARGED_AMETHYST, new SimplifiedRenderer(0, 0));
	public static final EmiRecipeCategory LIGHTNING_STRIKE = new EmiRecipeCategory(Identifier.of(Resonance.MOD_ID, "lightning_strike"), LIGHTNING_ROD, new SimplifiedRenderer(16, 0));
	public static final EmiRecipeCategory PEARL_DUPLICATION = new EmiRecipeCategory(Identifier.of(Resonance.MOD_ID, "pearl_duplication"), VanillaEmiRecipeCategories.CRAFTING, new SimplifiedRenderer(16, 0));
	public static final EmiRecipeCategory PEARL_GESTATION = new EmiRecipeCategory(Identifier.of(Resonance.MOD_ID, "pearl_gestation"), PEARL_ICE, new SimplifiedRenderer(0, 16));

	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(REVERBERATION);
		registry.addWorkstation(REVERBERATION, CHARGED_AMETHYST);
		registry.addCategory(LIGHTNING_STRIKE);
		registry.addWorkstation(LIGHTNING_STRIKE, LIGHTNING_ROD);
		registry.addCategory(PEARL_DUPLICATION);
		registry.addCategory(PEARL_GESTATION);
		registry.addWorkstation(PEARL_GESTATION, PEARL_ICE);

		RecipeManager manager = registry.getRecipeManager();

		for (RecipeEntry<ReverberationRecipe> recipe : manager.listAllOfType(ReverberationRecipe.Type.INSTANCE)) {
			registry.addRecipe(new ReverberationEmiRecipe(recipe.value(), recipe.id()));
		}
		for (RecipeEntry<LightningStrikeRecipe> recipe : manager.listAllOfType(LightningStrikeRecipe.Type.INSTANCE)) {
			registry.addRecipe(new LightningStrikeEmiRecipe(recipe.value(), recipe.id()));
		}
		for (Item pearl : Registries.ITEM.stream().filter(i -> i instanceof Pearl).collect(Collectors.toList())) {
			registry.addRecipe(new PearlDuplicationEmiRecipe(pearl, Identifier.of(Resonance.MOD_ID, "/crafting/" + Item.getRawId(pearl) + "_duplication")));
		}

		Registries.POTION.streamEntries().forEach((potion) -> {
			registry.addRecipe(new PearlGestationEmiRecipe(EmiIngredient.of(Ingredient.ofStacks(Items.AMETHYST_SHARD.getDefaultStack(), Resonance.CHARGED_AMETHYST_SHARD.getDefaultStack(), Resonance.PEARL.getDefaultStack())), Resonance.PEARL.getDefaultStack(), potion, Identifier.of(Resonance.MOD_ID, "/pearl_gestation/" + potion.getKey().get().getValue().getPath())));
		});
		Registries.POTION.streamEntries().forEach((potion) -> {
			registry.addRecipe(new PearlGestationEmiRecipe(EmiStack.of(Resonance.BIFURCATED_PEARL), Resonance.BIFURCATED_PEARL.getDefaultStack(), potion, Identifier.of(Resonance.MOD_ID, "/pearl_gestation/" + potion.getKey().get().getValue().getPath() + "_bifurcated_pearl")));
		});

		registry.addRecipe(new PearlGestationEmiRecipe(EmiIngredient.of(Ingredient.ofStacks(Items.AMETHYST_SHARD.getDefaultStack(), Resonance.CHARGED_AMETHYST_SHARD.getDefaultStack(), Resonance.PEARL.getDefaultStack())), Items.SUSPICIOUS_STEW.getDefaultStack(), Resonance.PEARL.getDefaultStack(), false, Identifier.of(Resonance.MOD_ID, "/pearl_gestation/stew")));
		registry.addRecipe(new PearlGestationEmiRecipe(EmiStack.of(Resonance.BIFURCATED_PEARL), Items.SUSPICIOUS_STEW.getDefaultStack(), Resonance.BIFURCATED_PEARL.getDefaultStack(), false, Identifier.of(Resonance.MOD_ID, "/pearl_gestation/stew_bifurcated_pearl")));

		for(int i = 0; i <= 4; ++i) {
			registry.addRecipe(new PearlGestationEmiRecipe(EmiIngredient.of(Ingredient.ofStacks(Items.AMETHYST_SHARD.getDefaultStack(), Resonance.CHARGED_AMETHYST_SHARD.getDefaultStack(), Resonance.PEARL.getDefaultStack())), Resonance.PEARL.getDefaultStack(), i, Identifier.of(Resonance.MOD_ID, "/pearl_gestation/ominous_" + i)));
		}
		for(int i = 0; i <= 4; ++i) {
			registry.addRecipe(new PearlGestationEmiRecipe(EmiStack.of(Resonance.BIFURCATED_PEARL), Resonance.BIFURCATED_PEARL.getDefaultStack(), i, Identifier.of(Resonance.MOD_ID, "/pearl_gestation/ominous_" + i + "_bifurcated_pearl")));
		}

		registry.addRecipe(new PearlGestationEmiRecipe(EmiIngredient.of(Ingredient.ofStacks(Items.AMETHYST_SHARD.getDefaultStack(), Resonance.CHARGED_AMETHYST_SHARD.getDefaultStack())), Resonance.PEARL.getDefaultStack(), Resonance.PEARL.getDefaultStack(), true, Identifier.of(Resonance.MOD_ID, "/pearl_gestation/pearl_as_potion")));
		registry.addRecipe(new PearlGestationEmiRecipe(EmiIngredient.of(Ingredient.ofStacks(Items.AMETHYST_SHARD.getDefaultStack(), Resonance.CHARGED_AMETHYST_SHARD.getDefaultStack())), Resonance.BIFURCATED_PEARL.getDefaultStack(), Resonance.BIFURCATED_PEARL.getDefaultStack(), true, Identifier.of(Resonance.MOD_ID, "/pearl_gestation/bifurcated_pearl_as_potion")));

		registry.addRecipe(new PearlGestationEmiRecipe(Identifier.of(Resonance.MOD_ID, "/pearl_gestation/bifurcated_pearl")));
	}
}