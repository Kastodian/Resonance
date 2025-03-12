package net.moss.resonance.compat.emi;

import java.util.stream.Collectors;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
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
    public static final EmiRecipeCategory REVERBERATION = new EmiRecipeCategory(Identifier.of(Resonance.MOD_ID, "reverberation"), CHARGED_AMETHYST, new SimplifiedRenderer(0, 0));
    public static final EmiRecipeCategory LIGHTNING_STRIKE = new EmiRecipeCategory(Identifier.of(Resonance.MOD_ID, "lightning_strike"), LIGHTNING_ROD, new SimplifiedRenderer(16, 0));
    public static final EmiRecipeCategory PEARL_DUPLICATION = new EmiRecipeCategory(Identifier.of(Resonance.MOD_ID, "pearl_duplication"), VanillaEmiRecipeCategories.CRAFTING, new SimplifiedRenderer(16, 0));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(REVERBERATION);
        registry.addWorkstation(REVERBERATION, CHARGED_AMETHYST);
        registry.addCategory(LIGHTNING_STRIKE);
        registry.addWorkstation(LIGHTNING_STRIKE, LIGHTNING_ROD);
        registry.addCategory(PEARL_DUPLICATION);

        RecipeManager manager = registry.getRecipeManager();

        for (RecipeEntry<ReverberationRecipe> recipe : manager.listAllOfType(ReverberationRecipe.Type.INSTANCE)) {
            registry.addRecipe(new ReverberationEmiRecipe(recipe.value(), recipe.id()));
        }
        for (RecipeEntry<LightningStrikeRecipe> recipe : manager.listAllOfType(LightningStrikeRecipe.Type.INSTANCE)) {
            registry.addRecipe(new LightningStrikeEmiRecipe(recipe.value(), recipe.id()));
        }
        for (Item pearl : Registries.ITEM.stream().filter(i -> i instanceof Pearl).collect(Collectors.toList())) {
            registry.addRecipe(new PearlDuplicationEmiRecipe(pearl, Identifier.of(Resonance.MOD_ID, "/crafting/" + pearl.getTranslationKey() + "_duplication")));
        }
    }
}