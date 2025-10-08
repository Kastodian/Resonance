package net.moss.resonance;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.recipe.Ingredient;

public record AlternatePearl(Ingredient pearl, List<StatusEffectInstance> effects, SoundEvent sound, int recuperation) {
	public static final Codec<AlternatePearl> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter(AlternatePearl::pearl),
			StatusEffectInstance.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(AlternatePearl::effects),
			SoundEvent.CODEC.optionalFieldOf("sound", Resonance.PEARL_HUB_USE_SOUND.value()).forGetter(AlternatePearl::sound),
			Codecs.POSITIVE_INT.optionalFieldOf("recuperation", 5).forGetter(AlternatePearl::recuperation)
	).apply(instance, AlternatePearl::new));

	public boolean matches(ItemStack itemStack) { return this.pearl.test(itemStack); }
}