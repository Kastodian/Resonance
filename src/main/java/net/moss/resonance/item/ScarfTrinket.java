package net.moss.resonance.item;

import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;

public class ScarfTrinket implements Trinket {
	@Override
	public Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, Identifier slotIdentifier) {
		var modifiers = Trinket.super.getModifiers(stack, slot, entity, slotIdentifier);
		stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers().forEach((entry) -> {
			EntityAttributeModifier newModifier = entry.modifier();
			modifiers.put(entry.attribute(), new EntityAttributeModifier(Identifier.of(Resonance.MOD_ID, newModifier.id().getPath() + ".trinket"), newModifier.value(), newModifier.operation()));
		});
		return modifiers;
	}
}