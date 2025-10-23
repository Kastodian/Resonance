package net.moss.resonance.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Optional;

public class PearlHubCriterion extends AbstractCriterion<PearlHubCriterion.Conditions> {
	@Override public Codec<Conditions> getConditionsCodec() { return Conditions.CODEC; }
	public void trigger(ServerPlayerEntity player, ItemStack stack, int level) { this.trigger(player, conditions -> conditions.requirementsMet(stack, level)); }

	public record Conditions(Optional<LootContextPredicate> player, Optional<ItemPredicate> item, NumberRange.IntRange level) implements AbstractCriterion.Conditions {
		public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
				ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Conditions::item),
				NumberRange.IntRange.CODEC.optionalFieldOf("beacon_level", NumberRange.IntRange.ANY).forGetter(Conditions::level)
		).apply(instance, Conditions::new));

		boolean requirementsMet(ItemStack stack, int level) { return (this.item.isEmpty() || this.item.get().test(stack)) && this.level.test(level); }
	}
}