package net.moss.resonance.world;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.Utils;

public class ModifyLootAndTradeTables {
	public static void modifyLootTables() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			RegistryEntryLookup<Enchantment> registryEntryLookup = registries.createRegistryLookup().getOrThrow(RegistryKeys.ENCHANTMENT);
			if (key.equals(LootTables.ANCIENT_CITY_CHEST) || key.equals(LootTables.TRIAL_CHAMBERS_SUPPLY_CHEST) || key.equals(LootTables.TRIAL_CHAMBERS_CORRIDOR_CHEST) || key.equals(LootTables.TRIAL_CHAMBERS_INTERSECTION_CHEST)) {
				if (key.equals(LootTables.ANCIENT_CITY_CHEST)) {
					tableBuilder.pool(Utils.enchantLootPoolBuilder(1, 0.2f, Items.ENCHANTED_BOOK, registryEntryLookup.getOrThrow(Resonance.RECOILING_CURSE), 1));
				}
				tableBuilder.pool(Utils.lootPoolBuilder(2, 0.05f, Resonance.CHARGED_AMETHYST_SHARD, 1, 3));
				tableBuilder.pool(Utils.lootPoolBuilder(1, 0.15f, Resonance.ROCK_CANDY, 1, 3));
				tableBuilder.pool(Utils.lootPoolBuilder(1, 0.05f, Resonance.ENCHANTED_ROCK_CANDY, 1, 3));
				tableBuilder.pool(Utils.lootPoolBuilder(1, 0.2f, Resonance.WHISTLING_ARROW, 1, 3));
				tableBuilder.pool(Utils.enchantLootPoolBuilder(1, 0.1f, Items.ENCHANTED_BOOK, registryEntryLookup.getOrThrow(Resonance.SPARKING_ENCHANT), 1));
			} else if (key.equals(LootTables.ANCIENT_CITY_ICE_BOX_CHEST)) {
				tableBuilder.pool(Utils.pearlLootPoolBuilder(1, 0.1f, Resonance.PEARL, 1, 1, Potions.STRENGTH, 1, 1, 1, 100));
				tableBuilder.pool(Utils.pearlLootPoolBuilder(1, 0.1f, Resonance.PEARL, 1, 1, Potions.REGENERATION, 1, 1, 1, 100));
				tableBuilder.pool(Utils.pearlLootPoolBuilder(1, 0.1f, Resonance.PEARL, 1, 1, Resonance.DARKNESS_POTION_1, 1, 1, 1, 100));
				tableBuilder.pool(Utils.pearlLootPoolBuilder(1, 0.1f, Resonance.PEARL, 1, 1, Resonance.SOUL_FATIGUE_POTION_2, 1, 1, 1, 100));
			} else if (key.equals(LootTables.BASTION_OTHER_CHEST) || key.equals(LootTables.JUNGLE_TEMPLE_CHEST)) {
				tableBuilder.pool(Utils.lootPoolBuilder(1, 0.2f, Resonance.TREASURE_HOARD.asItem(), 1, 1));
			} else if (key.equals(LootTables.BASTION_TREASURE_CHEST)) {
				tableBuilder.pool(Utils.lootPoolBuilder(2, 0.3f, Resonance.TREASURE_HOARD.asItem(), 1, 3));
			} else if (key.equals(LootTables.END_CITY_TREASURE_CHEST)) {
				tableBuilder.pool(Utils.lootPoolBuilder(1, 0.15f, Resonance.SHULKER_BOTTLE, 1, 2));
			}
		});

		TradeOfferHelper.registerWanderingTraderOffers(1, factories -> {
				factories.add((entity, random) -> new TradeOffer(
						new TradedItem(Items.EMERALD, 3),
						new ItemStack(Resonance.BATHYAL_VENT, 1),
						6, 12, 0.075f));
				factories.add((entity, random) -> new TradeOffer(
					new TradedItem(Items.EMERALD, 3),
					new ItemStack(Resonance.BOOK_PILE, 1),
					2, 12, 0.075f));
		});
	}
}