package net.moss.resonance.util;

import net.minecraft.block.Block;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.event.GameEvent;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import com.mojang.serialization.MapCodec;

public class Registerizer {
    public static Item item(String modId, String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(modId, name), item);
    }

    public static Block block(String modId, String name, Block block, Boolean isItem) {
        Identifier id = Identifier.of(modId, name);
        if (isItem) { Registry.register(Registries.ITEM, id, new BlockItem(block, new Settings())); }
        return Registry.register(Registries.BLOCK, id, block);
    }

    public static Block block(String modId, String name, Block block, Settings itemSettings) {
        Identifier id = Identifier.of(modId, name);
        Registry.register(Registries.ITEM, id, new BlockItem(block, itemSettings));
        return Registry.register(Registries.BLOCK, id, block);
    }

    public static RegistryEntry<StatusEffect> effect(String modId, String name, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(modId, name), effect);
    }

    public static Reference<Potion> potion(String modId, String name, RegistryEntry<StatusEffect> potion, int duration, int amplifier) {
        return Registry.registerReference(Registries.POTION, Identifier.of(modId, name), new Potion(new StatusEffectInstance(potion, duration, amplifier)));
    }

    public static Reference<Potion> potion(String modId, String name, StatusEffectInstance... effects) {
        return Registry.registerReference(Registries.POTION, Identifier.of(modId, name), new Potion(effects));
    }
    
	public static Reference<SoundEvent> sound(String modId, String name) {
        Identifier id = Identifier.of(modId, name);
        return Registry.registerReference(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

	public static Reference<GameEvent> gameEvent(String modId, String name) {
        return Registry.registerReference(Registries.GAME_EVENT, Identifier.of(modId, name), new GameEvent(16));
    }

    public static RegistryEntry<ArmorMaterial> registerArmourMaterial(String modId, String name, EnumMap<ArmorItem.Type, Integer> defense, int enchantability, RegistryEntry<SoundEvent> equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(Identifier.of(modId, name)));
        EnumMap<ArmorItem.Type, Integer> enumMap = new EnumMap<ArmorItem.Type, Integer>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values()) { enumMap.put(type, defense.get(type)); }
        return Registry.registerReference(Registries.ARMOR_MATERIAL, Identifier.of(modId, name), new ArmorMaterial(enumMap, enchantability, equipSound, repairIngredient, layers, toughness, knockbackResistance));
    }

    public static LootConditionType registerLootConditionType(String modId, String name, MapCodec<? extends LootCondition> codec) {
        return Registry.register(Registries.LOOT_CONDITION_TYPE, Identifier.of(modId, name), new LootConditionType(codec));
    }

    public static MapCodec<? extends EnchantmentEntityEffect> registerEnchantmentEffect(String modId, String name, MapCodec<? extends EnchantmentEntityEffect> codec) {
        return Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Identifier.of(modId, name), codec);
    }
    
    public static SmithingTemplateItem registerSmithingTemplateItem(String modId, String name) {
        Identifier id = Identifier.of(modId, name);
        return Registry.register(Registries.ITEM, id, SmithingTemplateItem.of(id));
    }
}