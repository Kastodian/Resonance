package net.moss.resonance.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;
import net.moss.resonance.ReverberationRecipe;

public class EmiCompat implements EmiPlugin {
    public static final Identifier MY_SPRITE_SHEET = Identifier.of(Resonance.MOD_ID, "textures/gui/emi_simplified_textures.png");
    public static final EmiStack CHARGED_AMETHYST = EmiStack.of(Resonance.CHARGED_AMETHYST_CLUSTER);
    public static final EmiRecipeCategory REVERBERATION
        = new EmiRecipeCategory(Identifier.of(Resonance.MOD_ID, "reverberation"), CHARGED_AMETHYST, new EmiTexture(MY_SPRITE_SHEET, 0, 0, 16, 16));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(REVERBERATION);
        registry.addWorkstation(REVERBERATION, CHARGED_AMETHYST);

        RecipeManager manager = registry.getRecipeManager();

        // Use vanilla's concept of your recipes and pass them to your EmiRecipe representation
        for (RecipeEntry<ReverberationRecipe> recipe : manager.listAllOfType(ReverberationRecipe.Type.INSTANCE)) {
            registry.addRecipe(new ReverberationEmiRecipe(recipe.value(), recipe.id()));
        }
    }
}