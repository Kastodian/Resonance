package net.moss.resonance;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;

public class OtherOrbEffectsRecipe implements Recipe<SingleStackRecipeInput> {
    protected final Ingredient ingredient;
    protected final ItemStack result;
    protected final String texture;

    public OtherOrbEffectsRecipe(Ingredient ingredient, ItemStack result, String texture) {
        this.ingredient = ingredient;
        this.result = result;
        this.texture = texture;
    }

    @Override public ItemStack getResult(WrapperLookup registryManager) { return result; }
    public ItemStack getItem() { return result; }
    public String getTexture() { return texture; }
    @Override public RecipeSerializer<?> getSerializer() { return Serializer.INSTANCE; }
    @Override public RecipeType<?> getType() { return Type.INSTANCE; }
    @Override public ItemStack createIcon() { return new ItemStack(Resonance.PEARL_HUB); }
    @Override public boolean matches(SingleStackRecipeInput singleStackRecipeInput, World world) { return this.ingredient.test(singleStackRecipeInput.item()); }
    @Override public ItemStack craft(SingleStackRecipeInput singleStackRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) { return null; }
    @Override public boolean fits(int width, int height) { return true; }

    public static class Type implements RecipeType<OtherOrbEffectsRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "other_orb_effects";
    }

    public static class Serializer implements RecipeSerializer<OtherOrbEffectsRecipe> {
        public static final String ID = "other_orb_effects";
        public static final Serializer INSTANCE = new Serializer();
        private final PacketCodec<RegistryByteBuf, OtherOrbEffectsRecipe> packetCodec = PacketCodec.ofStatic(this::write, this::read);
        private final MapCodec<OtherOrbEffectsRecipe> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter((recipe) -> { return recipe.ingredient; }),
                ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter((recipe) -> { return recipe.result; }),
                Codec.STRING.optionalFieldOf("texture", "").forGetter((recipe) -> { return recipe.texture; })
        ).apply(instance, OtherOrbEffectsRecipe::new));

        @Override public MapCodec<OtherOrbEffectsRecipe> codec() { return this.codec; }
        @Override public PacketCodec<RegistryByteBuf, OtherOrbEffectsRecipe> packetCodec() { return packetCodec; }

        private void write(RegistryByteBuf buf, OtherOrbEffectsRecipe recipe) {
            Ingredient.PACKET_CODEC.encode(buf, recipe.ingredient);
            ItemStack.PACKET_CODEC.encode(buf, recipe.result);
            buf.writeString(recipe.texture);
        }

        private OtherOrbEffectsRecipe read(RegistryByteBuf buf) {
            Ingredient ingredient = Ingredient.PACKET_CODEC.decode(buf);
            ItemStack result = ItemStack.PACKET_CODEC.decode(buf);
            String texture = buf.readString();
            return new OtherOrbEffectsRecipe(ingredient, result, texture);
        }
    }
}