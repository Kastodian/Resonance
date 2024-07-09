package net.moss.resonance.mixin;

import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.recipe.BrewingRecipeRegistry.Builder;
import net.moss.resonance.Resonance;

@Mixin(BrewingRecipeRegistry.class)
public class BrewingRecipeMixin {
    @ModifyVariable(method = "registerDefaults", at = @At(value = "HEAD", target = "Lnet/minecraft/recipe/BrewingRecipeRegistry;registerDefaults(Lnet/minecraft/recipe/BrewingRecipeRegistry/Builder;)V"), argsOnly = true)
    private static Builder addRecipe(Builder builder) {
        builder.registerPotionRecipe(Potions.THICK, Resonance.CHARGED_AMETHYST_SHARD, Resonance.SPARKING_POTION_1);
        builder.registerPotionRecipe(Resonance.SPARKING_POTION_1, Resonance.CHARGED_AMETHYST_SHARD, Resonance.SPARKING_POTION_3);
        builder.registerPotionRecipe(Potions.THICK, Resonance.LIGHTNING_BOTTLE, Resonance.SPARKING_POTION_3);
        builder.registerPotionRecipe(Resonance.SPARKING_POTION_1, Resonance.LIGHTNING_BOTTLE, Resonance.SPARKING_POTION_3);
        builder.registerPotionRecipe(Resonance.SPARKING_POTION_2, Resonance.LIGHTNING_BOTTLE, Resonance.SPARKING_POTION_3);
        builder.registerPotionRecipe(Resonance.SPARKING_POTION_1, Items.GLOWSTONE_DUST, Resonance.SPARKING_POTION_3);
        builder.registerPotionRecipe(Resonance.SPARKING_POTION_2, Items.GLOWSTONE_DUST, Resonance.SPARKING_POTION_3);
        builder.registerPotionRecipe(Resonance.SPARKING_POTION_1, Items.REDSTONE, Resonance.SPARKING_POTION_2);
        builder.registerPotionRecipe(Resonance.SPARKING_POTION_3, Items.REDSTONE, Resonance.SPARKING_POTION_2);
        
        builder.registerPotionRecipe(Potions.THICK, Resonance.SCULK_BOTTLE, Resonance.DARKNESS_POTION_1);
        builder.registerPotionRecipe(Resonance.DARKNESS_POTION_1, Items.REDSTONE, Resonance.DARKNESS_POTION_2);
        
        builder.registerPotionRecipe(Potions.SLOW_FALLING, Items.FERMENTED_SPIDER_EYE, Potions.INVISIBILITY);
        builder.registerPotionRecipe(Potions.LONG_SLOW_FALLING, Items.FERMENTED_SPIDER_EYE, Potions.LONG_INVISIBILITY);
        
        builder.registerPotionRecipe(Potions.THICK, Items.TURTLE_SCUTE, Potions.TURTLE_MASTER);

        builder.registerPotionRecipe(Potions.THICK, Resonance.SHULKER_BOTTLE, Resonance.LEVITATION_POTION_1);
        builder.registerPotionRecipe(Potions.THICK, Items.SHULKER_SHELL, Resonance.LEVITATION_POTION_1);
        builder.registerPotionRecipe(Resonance.LEVITATION_POTION_1, Items.REDSTONE, Resonance.LEVITATION_POTION_2);
        builder.registerPotionRecipe(Resonance.LEVITATION_POTION_3, Items.REDSTONE, Resonance.LEVITATION_POTION_2);
        builder.registerPotionRecipe(Resonance.LEVITATION_POTION_1, Items.GLOWSTONE_DUST, Resonance.LEVITATION_POTION_3);
        builder.registerPotionRecipe(Resonance.LEVITATION_POTION_2, Items.GLOWSTONE_DUST, Resonance.LEVITATION_POTION_3);
        builder.registerPotionRecipe(Resonance.LEVITATION_POTION_1, Items.FERMENTED_SPIDER_EYE, Potions.SLOW_FALLING);
        builder.registerPotionRecipe(Resonance.LEVITATION_POTION_2, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOW_FALLING);
        builder.registerPotionRecipe(Resonance.LEVITATION_POTION_3, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOW_FALLING);
        
        builder.registerPotionRecipe(Potions.THICK, Resonance.SOUL_BOTTLE, Resonance.SOUL_FATIGUE_POTION_1);
        builder.registerPotionRecipe(Potions.WEAKNESS, Items.SOUL_SAND, Resonance.SOUL_FATIGUE_POTION_1);
        builder.registerPotionRecipe(Potions.WEAKNESS, Items.SOUL_SOIL, Resonance.SOUL_FATIGUE_POTION_1);
        builder.registerPotionRecipe(Potions.LONG_WEAKNESS, Items.SOUL_SAND, Resonance.SOUL_FATIGUE_POTION_2);
        builder.registerPotionRecipe(Potions.LONG_WEAKNESS, Items.SOUL_SOIL, Resonance.SOUL_FATIGUE_POTION_2);
        builder.registerPotionRecipe(Resonance.SOUL_FATIGUE_POTION_1, Items.GLOWSTONE_DUST, Resonance.SOUL_FATIGUE_POTION_2);
        builder.registerPotionRecipe(Resonance.SOUL_FATIGUE_POTION_2, Items.GLOWSTONE_DUST, Resonance.SOUL_FATIGUE_POTION_3);

        builder.registerPotionRecipe(Resonance.LEVITATION_POTION_1, Items.PHANTOM_MEMBRANE, Resonance.LEVITATION_AND_SLOWFALLING_POTION_1);
        builder.registerPotionRecipe(Resonance.LEVITATION_POTION_2, Items.PHANTOM_MEMBRANE, Resonance.LEVITATION_AND_SLOWFALLING_POTION_1);
        builder.registerPotionRecipe(Resonance.LEVITATION_POTION_3, Items.PHANTOM_MEMBRANE, Resonance.LEVITATION_AND_SLOWFALLING_POTION_1);
        
        builder.registerPotionRecipe(Resonance.LEVITATION_AND_SLOWFALLING_POTION_1, Items.REDSTONE, Resonance.LEVITATION_AND_SLOWFALLING_POTION_2);
        builder.registerPotionRecipe(Resonance.LEVITATION_AND_SLOWFALLING_POTION_3, Items.REDSTONE, Resonance.LEVITATION_AND_SLOWFALLING_POTION_2);
        builder.registerPotionRecipe(Resonance.LEVITATION_AND_SLOWFALLING_POTION_1, Items.GLOWSTONE_DUST, Resonance.LEVITATION_AND_SLOWFALLING_POTION_3);
        builder.registerPotionRecipe(Resonance.LEVITATION_AND_SLOWFALLING_POTION_2, Items.GLOWSTONE_DUST, Resonance.LEVITATION_AND_SLOWFALLING_POTION_3);
        builder.registerPotionRecipe(Resonance.LEVITATION_AND_SLOWFALLING_POTION_1, Items.FERMENTED_SPIDER_EYE, Potions.LEAPING);
        builder.registerPotionRecipe(Resonance.LEVITATION_AND_SLOWFALLING_POTION_2, Items.FERMENTED_SPIDER_EYE, Potions.LONG_LEAPING);
        builder.registerPotionRecipe(Resonance.LEVITATION_AND_SLOWFALLING_POTION_3, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_LEAPING);
        return (builder);
    }
}