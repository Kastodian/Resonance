package net.moss.resonance.world;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.Utils;

public class ModifyLootAndTradeTables {
    private static final Identifier ANCIENT_CITY = Identifier.of("chests/ancient_city");
    private static final Identifier ANCIENT_CITY_ICE_BOX = Identifier.of("chests/ancient_city_ice_box");
    private static final Identifier BASTION_OTHER = Identifier.of("chests/bastion_other");
    private static final Identifier BASTION_TREASURE = Identifier.of("chests/bastion_treasure");
    private static final Identifier END_CITY = Identifier.of("chests/end_city_treasure");
    private static final Identifier SNOWY_TOWER_RARE = Identifier.of(Resonance.MOD_ID, "chests/snowy_tower_rare");
    private static final Identifier TRIAL_CHAMBER_SUPPLY = Identifier.of("chests/trial_chambers/supply");
    private static final Identifier TRIAL_CHAMBER_CORRIDOR = Identifier.of("chests/trial_chambers/corridor");
    private static final Identifier TRIAL_CHAMBER_INTERSECTION = Identifier.of("chests/trial_chambers/intersection");

    public static void modifyLootTables() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            Identifier id = key.getValue();
            RegistryEntryLookup<Enchantment> registryEntryLookup = registries.createRegistryLookup().getOrThrow(RegistryKeys.ENCHANTMENT);
            if (ANCIENT_CITY.equals(id) || TRIAL_CHAMBER_SUPPLY.equals(id) || TRIAL_CHAMBER_CORRIDOR.equals(id) || TRIAL_CHAMBER_INTERSECTION.equals(id)) {
                if (ANCIENT_CITY.equals(id)) {
                    tableBuilder.pool(Utils.enchantLootPoolBuilder(1, 0.2f, Items.ENCHANTED_BOOK, registryEntryLookup.getOrThrow(Resonance.RECOILING_CURSE), 1));
                }
                tableBuilder.pool(Utils.lootPoolBuilder(2, 0.05f, Resonance.CHARGED_AMETHYST_SHARD, 1, 3));
                tableBuilder.pool(Utils.lootPoolBuilder(1, 0.15f, Resonance.ROCK_CANDY, 1, 3));
                tableBuilder.pool(Utils.lootPoolBuilder(1, 0.05f, Resonance.ENCHANTED_ROCK_CANDY, 1, 3));
                tableBuilder.pool(Utils.lootPoolBuilder(1, 0.2f, Resonance.WHISTLING_ARROW, 1, 3));
                tableBuilder.pool(Utils.enchantLootPoolBuilder(1, 0.1f, Items.ENCHANTED_BOOK, registryEntryLookup.getOrThrow(Resonance.SPARKING_ENCHANT), 1));
            } else if (ANCIENT_CITY_ICE_BOX.equals(id)) {
                tableBuilder.pool(Utils.potionLootPoolBuilder(1, 0.1f, Resonance.PEARL, 1, 1, Potions.STRENGTH));
                tableBuilder.pool(Utils.potionLootPoolBuilder(1, 0.1f, Resonance.PEARL, 1, 1, Potions.REGENERATION));
                tableBuilder.pool(Utils.potionLootPoolBuilder(1, 0.1f, Resonance.PEARL, 1, 1, Resonance.DARKNESS_POTION_1));
                tableBuilder.pool(Utils.potionLootPoolBuilder(1, 0.1f, Resonance.PEARL, 1, 1, Resonance.SOUL_FATIGUE_POTION_2));
            } else if (BASTION_OTHER.equals(id)) {
                tableBuilder.pool(Utils.lootPoolBuilder(1, 0.1f, Resonance.TREASURE_HOARD.asItem(), 1, 1));
            } else if (BASTION_TREASURE.equals(id)) {
                tableBuilder.pool(Utils.lootPoolBuilder(2, 0.3f, Resonance.TREASURE_HOARD.asItem(), 1, 3));
            } else if (END_CITY.equals(id)) {
                tableBuilder.pool(Utils.lootPoolBuilder(1, 0.15f, Resonance.SHULKER_BOTTLE, 1, 2));
            } else if (SNOWY_TOWER_RARE.equals(id)) {
                tableBuilder.pool(Utils.potionLootPoolBuilder(1, 1f, Resonance.PEARL, 1, 1, Potions.LONG_SLOW_FALLING));
            }
        });
    }
}
