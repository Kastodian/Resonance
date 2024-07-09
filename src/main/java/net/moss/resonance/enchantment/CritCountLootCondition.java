package net.moss.resonance.enchantment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContext.EntityTarget;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.interfaces.PlayerEntityInterface;

public record CritCountLootCondition(LootNumberProvider critCount, LootContext.EntityTarget entity) implements LootCondition {
    public static final MapCodec<CritCountLootCondition> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(
            LootNumberProviderTypes.CODEC.fieldOf("crit_count").forGetter(CritCountLootCondition::critCount),
            EntityTarget.CODEC.fieldOf("entity").forGetter(CritCountLootCondition::entity)).apply(instance, CritCountLootCondition::new);
    });

    public CritCountLootCondition(LootNumberProvider critCount, LootContext.EntityTarget entity) {
        this.critCount = critCount;
        this.entity = entity;
    }

    public LootConditionType getType() {
        return Resonance.CRIT_COUNT_LOOT_CONDITION;
    }

    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.get(this.entity.getParameter());
        if (entity instanceof PlayerEntity player) {
            return this.critCount.nextInt(lootContext) <= ((PlayerEntityInterface)player).getCritCount();
        }
        return false;
    }

    public static LootCondition.Builder builder(float critCount, LootContext.EntityTarget entity) {
        return () -> {
            return new CritCountLootCondition(ConstantLootNumberProvider.create(critCount), entity);
        };
    }

    public static LootCondition.Builder builder(LootNumberProvider critCount, LootContext.EntityTarget entity) {
        return () -> {
            return new CritCountLootCondition(critCount, entity);
        };
    }

    public LootNumberProvider critCount() {
        return this.critCount;
    }

    public LootContext.EntityTarget entity() {
        return this.entity;
    }
}
