package net.moss.resonance.item;

import java.util.List;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.component.type.AttributeModifiersComponent.Entry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class ScarfTrinket implements Trinket {
    @Override
    public Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, Identifier slotIdentifier) {
        var modifiers = Trinket.super.getModifiers(stack, slot, entity, slotIdentifier);
        List<AttributeModifiersComponent.Entry> newModifiers = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers();
        for (Entry entry : newModifiers) { modifiers.put(entry.attribute(), entry.modifier()); }
        return modifiers;
    }
}