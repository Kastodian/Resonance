package net.moss.resonance.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.moss.resonance.Resonance;
import java.util.Optional;

public class PearlHubCriterion extends AbstractCriterion<PearlHubCriterion.Conditions> {
	@Override public Codec<Conditions> getConditionsCodec() { return Conditions.CODEC; }
	public void trigger(ServerPlayerEntity player, ItemStack stack) { this.trigger(player, conditions -> conditions.requirementsMet(stack)); }

	public record Conditions(Optional<LootContextPredicate> player, Optional<ItemPredicate> item) implements AbstractCriterion.Conditions {
		public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
				ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Conditions::item)
		).apply(instance, Conditions::new));

		public static AdvancementCriterion<Conditions> create(ItemPredicate.Builder predicate) { return Resonance.PEARL_HUB_CRITERION.create(new Conditions(Optional.empty(), Optional.of(predicate.build()))); }
		boolean requirementsMet(ItemStack stack) { return this.item.isEmpty() || this.item.get().test(stack); }
	}
}