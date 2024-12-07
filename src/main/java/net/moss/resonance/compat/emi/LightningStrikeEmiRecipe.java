package net.moss.resonance.compat.emi;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;
import net.moss.resonance.LightningStrikeRecipe;

public class LightningStrikeEmiRecipe implements EmiRecipe {
    private final Identifier id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> results;
    private final boolean resonates;
    private final boolean isMaterialConsumed;
    private final int explosionPower;
    private final EmiTexture explodeTexture = new EmiTexture(Identifier.of(Resonance.MOD_ID, "textures/gui/emi_explodes.png"), 0, 0, 32, 27, 32, 27, 32, 27);
    private final EmiTexture resonateTexture = new EmiTexture(Identifier.of(Resonance.MOD_ID, "textures/gui/emi_resonates.png"), 0, 0, 32, 27, 32, 27, 32, 27);

    public LightningStrikeEmiRecipe(LightningStrikeRecipe recipe, Identifier id) {
        this.id = id;
        this.input = List.of(EmiIngredient.of(recipe.getIngredients().get(0)));
        this.results = recipe.getResults().stream().map(i -> EmiStack.of(i)).toList();
        this.resonates = recipe.getResonates();
        this.isMaterialConsumed = recipe.getIsMaterialConsumed();
        this.explosionPower = recipe.getExplosionPower();
    }

    @Override public EmiRecipeCategory getCategory() { return EmiCompat.LIGHTNING_STRIKE; }
    @Override public @Nullable Identifier getId() { return id; }
    @Override public List<EmiIngredient> getInputs() { return input; }
    @Override public List<EmiStack> getOutputs() { return results; }
    @Override public int getDisplayWidth() { return 76; }
    @Override public int getDisplayHeight() { return 32; }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 20, 8);
        if (resonates) { widgets.addTexture(resonateTexture, 44, 0); }
        if (explosionPower > 0) { widgets.addTexture(explodeTexture, 44, 0); }

        widgets.addSlot(input.get(0), 0, 7);

        if (!isMaterialConsumed) { addOffsetWidget(widgets, input.get(0), 0, results.size() + 1); }
        if (results.isEmpty()) { return; }
        for (int i = 0; i < results.size(); i++) {
            if (!isMaterialConsumed) { addOffsetWidget(widgets, results.get(i), i + 1, results.size() + 1); }
            else { addOffsetWidget(widgets, results.get(i), i, results.size()); }
        }
    }

    public void addOffsetWidget(WidgetHolder widgets, EmiIngredient ingredient, int index, int size) {
        int xOffset = 0;
        int yOffset = 0;
        if (size == 2) {
            xOffset = index == 0 ? -8 : 8;
        } else if (size == 3) {
            yOffset = index > 1 ? 8 : -8;
            xOffset = index == 0 ? -8 : index == 1 ? 8 : 0;
        } else if (size == 4) {
            xOffset = index == 0 ? -8 : 8;
            yOffset = index > 1 ? 8 : -8;
        }
        widgets.addSlot(ingredient, 51 + xOffset, 7 + yOffset).drawBack(false).recipeContext(this);
    }
}