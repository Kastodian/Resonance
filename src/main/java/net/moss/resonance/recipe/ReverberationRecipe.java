package net.moss.resonance.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import java.util.Iterator;

public class ReverberationRecipe implements Recipe<SingleStackRecipeInput> {
	protected final Ingredient ingredient;
	protected final boolean isMaterialConsumed;
	protected final boolean resonates;
	protected final int explosionPower;
	protected final DefaultedList<ItemStack> results;

	public ReverberationRecipe(Ingredient ingredient, DefaultedList<ItemStack> results, boolean isMaterialConsumed, boolean resonates, int explosionPower) {
		this.ingredient = ingredient;
		this.results = results;
		this.isMaterialConsumed = isMaterialConsumed;
		this.resonates = resonates;
		this.explosionPower = explosionPower;
	}

	public DefaultedList<ItemStack> getResults() { return this.results; }
	public boolean getIsMaterialConsumed() { return this.isMaterialConsumed; }
	public boolean getResonates() { return this.resonates; }
	public int getExplosionPower() { return this.explosionPower; }
	@Override public RecipeType<?> getType() { return ReverberationRecipe.Type.INSTANCE; }
	@Override public RecipeSerializer<?> getSerializer() { return ReverberationRecipe.Serializer.INSTANCE; }
	@Override public ItemStack createIcon() { return new ItemStack(Resonance.CHARGED_AMETHYST_CLUSTER); }
	@Override public boolean matches(SingleStackRecipeInput singleStackRecipeInput, World world) { return this.ingredient.test(singleStackRecipeInput.item()); }
	@Override public boolean fits(int width, int height) { return true; }
	@Override public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) { return this.results.isEmpty() ? Items.AIR.getDefaultStack() : this.results.get(0); }
	@Override public ItemStack craft(SingleStackRecipeInput singleStackRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) { return this.results.isEmpty() ? Items.AIR.getDefaultStack() : this.results.get(0); }

	@Override
	public DefaultedList<Ingredient> getIngredients() {
		DefaultedList<Ingredient> defaultedList = DefaultedList.of();
		defaultedList.add(this.ingredient);
		return defaultedList;
	}

	public static class Type implements RecipeType<ReverberationRecipe> {
		public static final Type INSTANCE = new Type();
	}

	public static class Serializer implements RecipeSerializer<ReverberationRecipe> {
		public static final Serializer INSTANCE = new Serializer();
		public static final PacketCodec<RegistryByteBuf, ReverberationRecipe> PACKET_CODEC = PacketCodec.ofStatic(ReverberationRecipe.Serializer::write, ReverberationRecipe.Serializer::read);
		private final MapCodec<ReverberationRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> { return instance.group(
				Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter((recipe) -> { return recipe.ingredient; }),
				ItemStack.VALIDATED_CODEC.listOf().fieldOf("results").flatXmap((itemstacks) -> {
					ItemStack[] itemstacks2 = (ItemStack[])itemstacks.stream().filter((itemstack) -> {
						return !itemstack.isEmpty();
					}).toArray((i) -> { return new ItemStack[i]; });
					return itemstacks2.length > 3 ? DataResult.error(() -> {
						return "Too many results for reverberation recipe";
					}) : DataResult.success(DefaultedList.copyOf(ItemStack.EMPTY, itemstacks2));
				}, DataResult::success).forGetter((recipe) -> {
					return recipe.results;
				}),
				Codec.BOOL.fieldOf("isMaterialConsumed").orElse(true).forGetter((recipe) -> { return recipe.isMaterialConsumed; }),
				Codec.BOOL.fieldOf("resonates").orElse(false).forGetter((recipe) -> { return recipe.resonates; }),
				Codec.INT.fieldOf("explosionPower").orElse(0).forGetter((recipe) -> { return recipe.explosionPower; })
		).apply(instance, ReverberationRecipe::new); });

		@Override public MapCodec<ReverberationRecipe> codec() { return CODEC; }
		@Override public PacketCodec<RegistryByteBuf, ReverberationRecipe> packetCodec() { return PACKET_CODEC; }

		@SuppressWarnings("rawtypes")
		private static void write(RegistryByteBuf buf, ReverberationRecipe recipe) {
			Ingredient.PACKET_CODEC.encode(buf, recipe.ingredient);
			buf.writeVarInt(recipe.results.size());
			Iterator var2 = recipe.results.iterator();
			while(var2.hasNext()) {
				ItemStack itemstack = (ItemStack)var2.next();
				ItemStack.PACKET_CODEC.encode(buf, itemstack);
			}
			buf.writeBoolean(recipe.isMaterialConsumed);
			buf.writeBoolean(recipe.resonates);
			buf.writeInt(recipe.explosionPower);
		}

		private static ReverberationRecipe read(RegistryByteBuf buf) {
			Ingredient ingredient = Ingredient.PACKET_CODEC.decode(buf);
			int i = buf.readVarInt();
			DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(i, ItemStack.EMPTY);
			defaultedList.replaceAll((empty) -> {
				return (ItemStack)ItemStack.PACKET_CODEC.decode(buf);
			});
			boolean isMaterialConsumed = buf.readBoolean();
			boolean resonates = buf.readBoolean();
			int explosionPower = buf.readInt();
			return new ReverberationRecipe(ingredient, defaultedList, isMaterialConsumed, resonates, explosionPower);
		}
	}
}