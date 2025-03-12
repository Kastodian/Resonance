// Source code is decompiled from a .class file using FernFlower decompiler.
package net.moss.resonance.recipe;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.item.Pearl;

public class PearlDuplicationRecipe extends SpecialCraftingRecipe {
   public PearlDuplicationRecipe(CraftingRecipeCategory craftingRecipeCategory) {
      super(craftingRecipeCategory);
   }

   public boolean matches(CraftingRecipeInput craftingRecipeInput, World world) {
      if (craftingRecipeInput.getWidth() == 3 && craftingRecipeInput.getHeight() == 3) {
         for(int i = 0; i < craftingRecipeInput.getHeight(); ++i) {
            for(int j = 0; j < craftingRecipeInput.getWidth(); ++j) {
               ItemStack itemStack = craftingRecipeInput.getStackInSlot(j, i);
               if (j == 1 && i == 1) { if (!(itemStack.getItem() instanceof Pearl)) { return false; } }
               else if ((j == 1 || i == 1) && !itemStack.isOf(Items.ECHO_SHARD)) { return false; }
            }
         }
         return true;
      } else {
         return false;
      }
   }

   public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
      ItemStack itemStack = craftingRecipeInput.getStackInSlot(1, 1);
      if (!(itemStack.getItem() instanceof Pearl)) {
         return ItemStack.EMPTY;
      } else {
         ItemStack itemStack2 = new ItemStack(itemStack.getItem(), 2);
         itemStack2.set(DataComponentTypes.POTION_CONTENTS, itemStack.get(DataComponentTypes.POTION_CONTENTS));
         itemStack2.set(Resonance.PEARL_STATS, itemStack.get(Resonance.PEARL_STATS));
         return itemStack2;
      }
   }

   public boolean fits(int width, int height) {
      return width >= 3 && height >= 3;
   }

   public RecipeSerializer<?> getSerializer() {
      return Resonance.PEARL_DUPLICATION_RECIPE_SERIALIZER;
   }

   public static class Type implements RecipeType<PearlDuplicationRecipe> {
      public static final Type INSTANCE = new Type();
   }
}
