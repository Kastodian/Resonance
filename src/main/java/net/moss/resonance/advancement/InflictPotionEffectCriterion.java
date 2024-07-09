package net.moss.resonance.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.loot.context.LootContext;
import java.util.Optional;
import net.minecraft.predicate.entity.EntityEffectPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.moss.resonance.Resonance;

public class InflictPotionEffectCriterion extends AbstractCriterion<InflictPotionEffectCriterion.Conditions> {
    @Override public Codec<Conditions> getConditionsCodec() { return Conditions.CODEC; }

    public void trigger(ServerPlayerEntity player, Entity victim, StatusEffectInstance statusEffectInstance) {
        LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, victim);
        trigger(player, conditions -> { return conditions.requirementsMet(statusEffectInstance, player, victim, lootContext); });
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<EntityEffectPredicate> effects, Optional<LootContextPredicate> victim) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                EntityEffectPredicate.CODEC.optionalFieldOf("effects").forGetter(Conditions::effects),
                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("victim").forGetter(Conditions::victim)
                ).apply(instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create(EntityEffectPredicate.Builder effects) { return Resonance.INFLICT_POTION_EFFECT_CRITERION.create(new Conditions(Optional.empty(), effects.build(), Optional.empty())); }
        public static AdvancementCriterion<Conditions> create(EntityPredicate.Builder victim) { return Resonance.INFLICT_POTION_EFFECT_CRITERION.create(new Conditions(Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.asLootContextPredicate(victim.build())))); }

        boolean requirementsMet(StatusEffectInstance statusEffectInstance, ServerPlayerEntity player, Entity victim, LootContext context) {
            if (this.effects.isPresent() && !this.effects.get().test(victim)) { return false; }
            return !this.victim.isPresent() || this.victim.get().test(context);
        }
    }
}