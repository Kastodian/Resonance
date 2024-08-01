package net.moss.resonance.item;

import java.util.List;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.component.type.AttributeModifiersComponent.Entry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Equipment;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.moss.resonance.util.Utils;
import net.moss.resonance.util.interfaces.ScarfInterface;

public class ScarfTrinket extends TrinketItem implements Equipment, ScarfInterface {
    public String TEXTURE;
    public Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> bonusModifiers;

    public ScarfTrinket(Settings settings, String modId, String texture) {
        super(settings.maxCount(1));
        TEXTURE = modId + ":textures/models/armor/" + texture;
        bonusModifiers = ArrayListMultimap.create();
    }

    public ScarfTrinket(Settings settings, String modId, String texture, Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> bonusModifiers) {
        super(settings.maxCount(1));
        this.TEXTURE = modId + ":textures/models/armor/" + texture;
        this.bonusModifiers = bonusModifiers;
    }

    @Override
    public Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, Identifier slotIdentifier) {
        var modifiers = super.getModifiers(stack, slot, entity, slotIdentifier);
        List<AttributeModifiersComponent.Entry> newModifiers = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers();
        for (Entry entry : newModifiers) { modifiers.put(entry.attribute(), entry.modifier()); }
        modifiers.putAll(bonusModifiers);
        return modifiers;
    }
    
    @Override public EquipmentSlot getSlotType() { return EquipmentSlot.CHEST; }
    @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) { return Utils.trinketHotswap(world, user, hand, this); }
    @Override public String getTexture() { return TEXTURE; }
}