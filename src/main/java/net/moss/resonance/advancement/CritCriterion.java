package net.moss.resonance.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.interfaces.PlayerEntityInterface;

import java.util.Optional;

public class CritCriterion extends AbstractCriterion<CritCriterion.Conditions> {
    @Override public Codec<Conditions> getConditionsCodec() { return Conditions.CODEC; }

    public void trigger(ServerPlayerEntity player, int count) {
        this.trigger(player, conditions -> { return conditions.requirementsMet(player, count); });
    }

    public record Conditions(Optional<LootContextPredicate> player, NumberRange.IntRange count) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                NumberRange.IntRange.CODEC.optionalFieldOf("crit_count", NumberRange.IntRange.ANY).forGetter(Conditions::count)
                ).apply(instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create(ItemPredicate.Builder predicate) { return Resonance.CRIT_CRITERION.create(new Conditions(Optional.empty(), NumberRange.IntRange.ANY)); }

        boolean requirementsMet(ServerPlayerEntity player, int count) {
            return this.count.test(((PlayerEntityInterface)player).getCritCount());
        }
    }
}