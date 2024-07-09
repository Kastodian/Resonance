package net.moss.resonance.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.predicate.item.ComponentSubPredicate;
import net.moss.resonance.Resonance;

public record StoredCritsPredicate(NumberRange.IntRange critCount) implements ComponentSubPredicate<Integer> {
    public static final Codec<StoredCritsPredicate> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(IntRange.CODEC.optionalFieldOf("stored_crits", IntRange.ANY).forGetter(StoredCritsPredicate::critCount)).apply(instance, StoredCritsPredicate::new);
    });

    public StoredCritsPredicate(NumberRange.IntRange critCount) {
        this.critCount = critCount;
    }

    public ComponentType<Integer> getComponentType() {
        return Resonance.STORED_CRITS;
    }

    public boolean test(ItemStack itemStack, Integer integer) {
        if (!itemStack.contains(Resonance.STORED_CRITS)) { return false; }
        else { return this.critCount.test(itemStack.get(Resonance.STORED_CRITS)); }
    }

    public NumberRange.IntRange critCount() {
        return this.critCount;
    }
}
