package net.moss.resonance.advancement;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.moss.resonance.Resonance;

public class TintedBottleCriterion extends AbstractCriterion<TintedBottleCriterion.Conditions> {
    @Override public Codec<Conditions> getConditionsCodec() { return Conditions.CODEC; }
    
    public void trigger(ServerPlayerEntity player, Entity victim) {
        LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, victim);
        trigger(player, conditions -> { return conditions.matches(lootContext); });
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<LootContextPredicate> entity) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("entity").forGetter(Conditions::entity)
                ).apply(instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create(EntityPredicate.Builder entity) { return Resonance.TINTED_BOTTLE_CRITERION.create(new Conditions(Optional.empty(), Optional.of(EntityPredicate.asLootContextPredicate(entity.build())))); }

        boolean matches(LootContext context) {
            return this.entity.isPresent() && this.entity.get().test(context);
        }
    }
}