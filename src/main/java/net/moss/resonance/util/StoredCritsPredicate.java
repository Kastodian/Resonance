package net.moss.resonance.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.predicate.item.ItemSubPredicate;
import net.moss.resonance.Resonance;

public record StoredCritsPredicate(NumberRange.IntRange critCount) implements ItemSubPredicate {
	public static final Codec<StoredCritsPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(IntRange.CODEC.optionalFieldOf("stored_crits", IntRange.ANY).forGetter(StoredCritsPredicate::critCount)).apply(instance, StoredCritsPredicate::new));

	public boolean test(ItemStack itemStack) {
		return this.critCount.test(itemStack.getOrDefault(Resonance.STORED_CRITS, 0));
	}

	public NumberRange.IntRange critCount() { return this.critCount; }
}