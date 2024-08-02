package net.moss.resonance;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Unit;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.SculkSensorFrequencyRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.predicate.item.ItemSubPredicate;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.moss.resonance.advancement.*;
import net.moss.resonance.block.*;
import net.moss.resonance.block.entity.RainChimeBlockEntity;
import net.moss.resonance.block.entity.BrittleBlockEntity;
import net.moss.resonance.block.entity.BuddingPearlIceBlockEntity;
import net.moss.resonance.block.entity.PearlHubBlockEntity;
import net.moss.resonance.enchantment.CritCountLootCondition;
import net.moss.resonance.enchantment.ReverberateEnchantmentEffect;
import net.moss.resonance.enchantment.SetCritCountEnchantmentEffect;
import net.moss.resonance.entity.ModEntities;
import net.moss.resonance.event.*;
import net.moss.resonance.item.*;
import net.moss.resonance.networking.JumpPayload;
import net.moss.resonance.particle.ModParticles;
import net.moss.resonance.potioneffects.CrushingWeightEffect;
import net.moss.resonance.potioneffects.DoubleJumpEffect;
import net.moss.resonance.potioneffects.SoulFatigueEffect;
import net.moss.resonance.potioneffects.StaticEffect;
import net.moss.resonance.util.Registerizer;
import net.moss.resonance.util.StoredCritsPredicate;
import net.moss.resonance.world.ModifyLootAndTradeTables;
import net.moss.resonance.world.UniqueStructurePlacement;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.enchantment.effect.TargetedEnchantmentEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;

public class Resonance implements ModInitializer {
	public static final String MOD_ID = "resonance";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final boolean isTrinketsInstalled = FabricLoader.getInstance().isModLoaded("trinkets");

	// Tags -------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static final TagKey<EntityType<?>> SOUND_SENSITIVE_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "sound_sensitive"));
    public static final TagKey<Block> CONDUCTIVE_BLOCKS_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "conductive_blocks"));
	public static final TagKey<Item> DUAL_WIELD_RENDER_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "dual_wield_render"));
	public static final TagKey<Item> CONDUCTIVE_ITEMS_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "conductive_items"));
    public static final TagKey<Biome> IS_DREAM_SWEET_TAG = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "is_dream_sweet"));
	public static final TagKey<Item> RENDER_ON_BACK_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "render_on_back"));
	public static final TagKey<Item> UNIQUE_ITEMS_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "unique_items"));
	public static final TagKey<Item> PEARL_ITEMS_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "alternate_pearl_items"));
	// Advancement Criterion --------------------------------------------------------------------------------------------------------------------------------------------------------
    public static final CritCriterion CRIT_CRITERION = Criteria.register(MOD_ID + ":" + "crit", new CritCriterion());
    public static final ParryCriterion PARRY_CRITERION = Criteria.register(MOD_ID + ":" + "shield_parry", new ParryCriterion());
    public static final PearlHubCriterion PEARL_HUB_CRITERION = Criteria.register(MOD_ID + ":" + "use_pearl_hub", new PearlHubCriterion());
    public static final DoubleJumpCriterion DOUBLE_JUMP_CRITERION = Criteria.register(MOD_ID + ":" + "double_jump", new DoubleJumpCriterion());
    public static final TintedBottleCriterion TINTED_BOTTLE_CRITERION = Criteria.register(MOD_ID + ":" + "tinted_bottling", new TintedBottleCriterion());
    public static final InflictPotionEffectCriterion INFLICT_POTION_EFFECT_CRITERION = Criteria.register(MOD_ID + ":" + "inflict_effect", new InflictPotionEffectCriterion());
    // Damage Types -----------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static final RegistryKey<DamageType> REVERBERATION_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID, "reverberation"));
	public static final RegistryKey<DamageType> GROUND_POUND_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID, "ground_pound"));
	public static final RegistryKey<DamageType> SPARKING_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID, "sparking"));
	public static final RegistryKey<DamageType> ICICLE_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID, "icicle"));
    // Gamerules --------------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static final CustomGameRuleCategory RESONANCE_GAMERULES = new CustomGameRuleCategory(Identifier.of(MOD_ID, "resonance_gamerules"), Text.translatable("catagory.resonance").formatted(Formatting.YELLOW, Formatting.BOLD));
	public static final GameRules.Key<GameRules.IntRule> PEARL_REFRESH_DELAY_GAMERULE = GameRuleRegistry.register("pearlRefreshDelay", RESONANCE_GAMERULES, GameRuleFactory.createIntRule(1200, 1));
	public static final GameRules.Key<GameRules.IntRule> JUMP_ORB_REFRESH_DELAY_GAMERULE = GameRuleRegistry.register("jumpOrbRefreshDelay", RESONANCE_GAMERULES, GameRuleFactory.createIntRule(100, 1));
    public static final GameRules.Key<GameRules.BooleanRule> CACTI_DESTROY_UNIQUE_ITEMS_GAMERULE = GameRuleRegistry.register("cactiDestroyUniqueItems", RESONANCE_GAMERULES, GameRuleFactory.createBooleanRule(false));
    // Sounds -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static final RegistryEntry.Reference<SoundEvent> REVERBERATION_SOUND = Registerizer.sound(MOD_ID, "reverberation");
    public static final RegistryEntry.Reference<SoundEvent> ELECTRIC_SPARK_SOUND = Registerizer.sound(MOD_ID, "sparks");
    public static final RegistryEntry.Reference<SoundEvent> DOUBLE_JUMP_SOUND = Registerizer.sound(MOD_ID, "double_jump");
    public static final RegistryEntry.Reference<SoundEvent> PEARL_HUB_ADD_SOUND = Registerizer.sound(MOD_ID, "pearl_hub_add");
    public static final RegistryEntry.Reference<SoundEvent> PEARL_HUB_REMOVE_SOUND = Registerizer.sound(MOD_ID, "pearl_hub_remove");
    public static final RegistryEntry.Reference<SoundEvent> PEARL_HUB_USE_SOUND = Registerizer.sound(MOD_ID, "pearl_hub_use");
    // Recipe Types -----------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static final RecipeSerializer<ReverberationRecipe> REVERBERATE_RECIPE_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, ReverberationRecipe.Serializer.ID), ReverberationRecipe.Serializer.INSTANCE);
    public static final RecipeType<ReverberationRecipe> REVERBERATE_RECIPE_TYPE = Registry.register(Registries.RECIPE_TYPE, Identifier.of(MOD_ID, ReverberationRecipe.Type.ID), ReverberationRecipe.Type.INSTANCE);
    public static final RecipeSerializer<OtherOrbEffectsRecipe> OTHER_ORB_RECIPE_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, OtherOrbEffectsRecipe.Serializer.ID), OtherOrbEffectsRecipe.Serializer.INSTANCE);
    public static final RecipeType<OtherOrbEffectsRecipe> OTHER_ORB_RECIPE_TYPE = Registry.register(Registries.RECIPE_TYPE, Identifier.of(MOD_ID, OtherOrbEffectsRecipe.Type.ID), OtherOrbEffectsRecipe.Type.INSTANCE);
    // Components -------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static final ComponentType<List<Integer>> PEARL_STATS = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "pearl_stats"), ComponentType.<List<Integer>>builder().codec(Codec.INT.listOf()).build());
    public static final ComponentType<List<Integer>> CATALYST_STATS = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "catalyst_stats"), ComponentType.<List<Integer>>builder().codec(Codec.INT.listOf()).build());
    public static final ComponentType<Integer> STORED_CRITS = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "stored_crits"), ComponentType.<Integer>builder().codec(Codec.INT).build());
    public static final ComponentType<Unit> HAS_RECOIL = Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, MOD_ID + ":" + "has_recoil", ComponentType.<Unit>builder().codec(Unit.CODEC).build());
    public static final ComponentType<EnchantmentValueEffect> COOLDOWN_TIME = Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, MOD_ID + ":" + "cooldown_time", ComponentType.<EnchantmentValueEffect>builder().codec(EnchantmentValueEffect.CODEC).build());
    public static final ComponentType<EnchantmentValueEffect> WEIGHT_VALUE = Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, MOD_ID + ":" + "weight_value", ComponentType.<EnchantmentValueEffect>builder().codec(EnchantmentValueEffect.CODEC).build());
    public static final ComponentType<List<TargetedEnchantmentEffect<EnchantmentEntityEffect>>> POST_CRIT = Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Identifier.of(MOD_ID, "post_crit"), ComponentType.<List<TargetedEnchantmentEffect<EnchantmentEntityEffect>>>builder().codec(TargetedEnchantmentEffect.createPostAttackCodec(EnchantmentEntityEffect.CODEC, LootContextTypes.ENCHANTED_DAMAGE).listOf()).build());
    public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentEntityEffect>>> POST_USE = Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, Identifier.of(MOD_ID, "post_use"), ComponentType.<List<EnchantmentEffectEntry<EnchantmentEntityEffect>>>builder().codec(EnchantmentEffectEntry.createCodec(EnchantmentEntityEffect.CODEC, LootContextTypes.ENCHANTED_ITEM).listOf()).build());
    // Loot Condition Types ---------------------------------------------------------------------------------------------------------------------------------------------------------
    public static final LootConditionType CRIT_COUNT_LOOT_CONDITION = Registerizer.registerLootConditionType(MOD_ID, "crit_count", CritCountLootCondition.CODEC);
    // Enchantment Effects ----------------------------------------------------------------------------------------------------------------------------------------------------------
    public static final MapCodec<? extends EnchantmentEntityEffect> REVERBERATE_ENCHANTMENT_EFFECT = Registerizer.registerEnchantmentEffect(MOD_ID, "reverberate", ReverberateEnchantmentEffect.CODEC);
    public static final MapCodec<? extends EnchantmentEntityEffect> SET_STORED_CRITS_ENCHANTMENT_EFFECT = Registerizer.registerEnchantmentEffect(MOD_ID, "set_stored_crits", SetCritCountEnchantmentEffect.CODEC);
    // Potions ----------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static final RegistryEntry<StatusEffect> SPARKING = Registerizer.effect(Resonance.MOD_ID, "sparking", new StaticEffect());
    public static final RegistryEntry<StatusEffect> SOUL_FATIGUE = Registerizer.effect(Resonance.MOD_ID, "soul_fatigue", new SoulFatigueEffect()
            .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, Identifier.of(MOD_ID, "effect.soul_fatigue"), -0.1f, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, Identifier.of(MOD_ID, "effect.soul_fatigue"), -1, Operation.ADD_VALUE)
            .addAttributeModifier(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, Identifier.of(MOD_ID, "effect.soul_fatigue"), -1, Operation.ADD_VALUE));
    public static final RegistryEntry<StatusEffect> NORMAL_DOUBLE_JUMP = Registerizer.effect(Resonance.MOD_ID, "normal_double_jump", new DoubleJumpEffect()
            .addAttributeModifier(Resonance.JUMPS,Identifier.of(MOD_ID, "effect.normal_double_jump"), 1f, Operation.ADD_VALUE));
    public static final RegistryEntry<StatusEffect> STRONG_DOUBLE_JUMP = Registerizer.effect(Resonance.MOD_ID, "strong_double_jump", new DoubleJumpEffect()
            .addAttributeModifier(Resonance.JUMPS, Identifier.of(MOD_ID, "effect.strong_double_jump"), 1f, Operation.ADD_VALUE));
    public static final RegistryEntry<StatusEffect> CRUSHING_WEIGHT = Registerizer.effect(Resonance.MOD_ID, "crushing_weight", new CrushingWeightEffect()
            .addAttributeModifier(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED, Identifier.of(MOD_ID, "effect.crushing_weight"), -0.2f, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_JUMP_STRENGTH, Identifier.of(MOD_ID, "effect.crushing_weight"), -0.2f, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_STEP_HEIGHT, Identifier.of(MOD_ID, "effect.crushing_weight"), -0.2f, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_GRAVITY, Identifier.of(MOD_ID, "effect.crushing_weight"), 0.2f, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER, Identifier.of(MOD_ID, "effect.crushing_weight"), 0.1f, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, Identifier.of(MOD_ID, "effect.crushing_weight"), -0.1f, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, Identifier.of(MOD_ID, "effect.crushing_weight"), -1.0, Operation.ADD_VALUE)
            .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, Identifier.of(MOD_ID, "effect.crushing_weight"), -0.2f, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, Identifier.of(MOD_ID, "effect.crushing_weight"), 2, Operation.ADD_VALUE));
    public static final RegistryEntry<Potion> SPARKING_POTION_1 = Registerizer.potion(Resonance.MOD_ID, "sparking_potion_1", SPARKING, 30*20, 1);
    public static final RegistryEntry<Potion> SPARKING_POTION_2 = Registerizer.potion(Resonance.MOD_ID, "sparking_potion_2", SPARKING, 60*20, 1);
    public static final RegistryEntry<Potion> SPARKING_POTION_3 = Registerizer.potion(Resonance.MOD_ID, "sparking_potion_3", SPARKING, 15*20, 3);
    public static final RegistryEntry<Potion> DARKNESS_POTION_1 = Registerizer.potion(Resonance.MOD_ID, "darkness_potion_1", StatusEffects.DARKNESS, 30*20, 0);
    public static final RegistryEntry<Potion> DARKNESS_POTION_2 = Registerizer.potion(Resonance.MOD_ID, "darkness_potion_2", StatusEffects.DARKNESS, 180*20, 0);
    public static final RegistryEntry<Potion> LEVITATION_POTION_1 = Registerizer.potion(Resonance.MOD_ID, "levitation_potion_1", StatusEffects.LEVITATION, 20*20, 0);
    public static final RegistryEntry<Potion> LEVITATION_POTION_2 = Registerizer.potion(Resonance.MOD_ID, "levitation_potion_2", StatusEffects.LEVITATION, 40*20, 0);
    public static final RegistryEntry<Potion> LEVITATION_POTION_3 = Registerizer.potion(Resonance.MOD_ID, "levitation_potion_3", StatusEffects.LEVITATION, 10*20, 3);
    public static final RegistryEntry<Potion> SOUL_FATIGUE_POTION_1 = Registerizer.potion(Resonance.MOD_ID, "soul_fatigue_potion_1", SOUL_FATIGUE, 15*20, 0);
    public static final RegistryEntry<Potion> SOUL_FATIGUE_POTION_2 = Registerizer.potion(Resonance.MOD_ID, "soul_fatigue_potion_2", SOUL_FATIGUE, 15*20, 1);
    public static final RegistryEntry<Potion> SOUL_FATIGUE_POTION_3 = Registerizer.potion(Resonance.MOD_ID, "soul_fatigue_potion_3", SOUL_FATIGUE, 15*20, 2);
    public static final RegistryEntry<Potion> LEVITATION_AND_SLOWFALLING_POTION_1 = Registerizer.potion(Resonance.MOD_ID, "levitation_and_slowfalling_potion_1", new StatusEffectInstance(StatusEffects.LEVITATION, 10*20, 0), new StatusEffectInstance(StatusEffects.SLOW_FALLING, 20*20, 0));
    public static final RegistryEntry<Potion> LEVITATION_AND_SLOWFALLING_POTION_2 = Registerizer.potion(Resonance.MOD_ID, "levitation_and_slowfalling_potion_2", new StatusEffectInstance(StatusEffects.LEVITATION, 20*20, 0), new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40*20, 0));
    public static final RegistryEntry<Potion> LEVITATION_AND_SLOWFALLING_POTION_3 = Registerizer.potion(Resonance.MOD_ID, "levitation_and_slowfalling_potion_3", new StatusEffectInstance(StatusEffects.LEVITATION, 5*20, 3), new StatusEffectInstance(StatusEffects.SLOW_FALLING, 10*20, 1));
    // Blocks & Items ---------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static final Block CHARGED_AMETHYST_CLUSTER = Registerizer.block(MOD_ID, "charged_amethyst_cluster", new ChargedAmethystBlock(0), true);
    public static final Block LARGE_CHARGED_AMETHYST_BUD = Registerizer.block(MOD_ID, "large_charged_amethyst_bud", new ChargedAmethystBlock(1), true);
    public static final Block MEDIUM_CHARGED_AMETHYST_BUD = Registerizer.block(MOD_ID, "medium_charged_amethyst_bud", new ChargedAmethystBlock(2), true);
    public static final Block SMALL_CHARGED_AMETHYST_BUD = Registerizer.block(MOD_ID, "small_charged_amethyst_bud", new ChargedAmethystBlock(3), true);
    public static final Item CHARGED_AMETHYST_SHARD = Registerizer.item(MOD_ID, "charged_amethyst_shard", new Item(new Settings()));
    public static final Item WHISTLING_ARROW = Registerizer.item(MOD_ID, "whistling_arrow", new WhistlingArrow(new Settings()));
    public static final Item AMETHYST_SHIELD = Registerizer.item(MOD_ID, "amethyst_shield", new CustomShield(new Settings().maxDamage(336), 133, 20, Items.AMETHYST_SHARD));
    public static final Item ROCK_CANDY = Registerizer.item(MOD_ID, "rock_candy", new Item(new Settings().food(new FoodComponent.Builder().nutrition(6).saturationModifier(0.3f).build())));
    public static final Item ENCHANTED_ROCK_CANDY = Registerizer.item(MOD_ID, "enchanted_rock_candy", new Item(new Settings().component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).rarity(Rarity.UNCOMMON).food(new FoodComponent.Builder().nutrition(8).saturationModifier(0.2f).statusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 1000, 0), 1f).statusEffect(new StatusEffectInstance(SPARKING, 180, 0), 0.5f).build())));
    public static final Block RAIN_CHIME = Registerizer.block(MOD_ID, "rain_chime", new RainChimeBlock(AbstractBlock.Settings.create()), true);
    public static final Item COPPER_SHIELD = Registerizer.item(MOD_ID, "copper_shield", new CustomShield(new Settings().maxDamage(336).attributeModifiers(AttributeModifiersComponent.builder()
            .add(EntityAttributes.GENERIC_ARMOR, new EntityAttributeModifier(Identifier.of(MOD_ID, "armor.sheild"), 2, Operation.ADD_VALUE), AttributeModifierSlot.HAND)
            .build()), 66, 10, Items.COPPER_INGOT));
    public static final Item TINTED_BOTTLE = Registerizer.item(MOD_ID, "tinted_bottle", new TintedBottle(new Settings().maxCount(16)));
    public static final Item SCULK_BOTTLE = Registerizer.item(MOD_ID, "sculk_bottle", new SculkBottle(new Settings().maxCount(16).recipeRemainder(TINTED_BOTTLE).component(CATALYST_STATS, Ints.asList(0,-1,2,0))));
    public static final Item SOUL_BOTTLE = Registerizer.item(MOD_ID, "soul_bottle", new SoulBottle(new Settings().maxCount(16).recipeRemainder(TINTED_BOTTLE).component(CATALYST_STATS, Ints.asList(-1,-1,-1,0))));
    public static final Item FIREBALL_BOTTLE = Registerizer.item(MOD_ID, "fireball_bottle", new FireballBottle(new Settings().maxCount(16).recipeRemainder(TINTED_BOTTLE).component(CATALYST_STATS, Ints.asList(1,0,1,0))));
    public static final Item LIGHTNING_BOTTLE = Registerizer.item(MOD_ID, "lightning_bottle", new LightningBottle(new Settings().maxCount(16).recipeRemainder(TINTED_BOTTLE).component(CATALYST_STATS, Ints.asList(0,-1,2,0))));
    public static final Item SHULKER_BOTTLE = Registerizer.item(MOD_ID, "shulker_bottle", new ShulkerBottle(new Settings().maxCount(16).recipeRemainder(TINTED_BOTTLE).component(CATALYST_STATS, Ints.asList(1,1,0,0))));
	public static final Item LOST_SOUL_SPAWN_EGG = Registerizer.item(MOD_ID, "lost_soul_spawn_egg", new SpawnEggItem(ModEntities.LOST_SOUL, 0x76F1E3, 0x60E8D9, new Settings()));
    public static final Block TREASURE_HOARD = Registerizer.block(MOD_ID, "treasure_hoard", new TreasureHoardBlock(AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).sounds(BlockSoundGroup.CHAIN).nonOpaque().strength(1.4f, 6.0f)), true);
    public static final Block BOOK_PILE = Registerizer.block(MOD_ID, "book_pile", new HeapBlock(AbstractBlock.Settings.copy(Blocks.BOOKSHELF).sounds(BlockSoundGroup.CHISELED_BOOKSHELF).nonOpaque().burnable().strength(1.2f, 1f)), true);
    public static final Block COMPOST_HEAP = Registerizer.block(MOD_ID, "compost_heap", new HeapBlock(8, 8, 11, 11, 7, AbstractBlock.Settings.copy(Blocks.DIRT).sounds(BlockSoundGroup.AZALEA_LEAVES).nonOpaque().strength(0.5f, 0.3f)), true);
    public static final Block DATAPACK_BLOCK = Registerizer.block(MOD_ID, "datapack_block", new DatapackBlock(AbstractBlock.Settings.copy(Blocks.BARREL).nonOpaque().strength(1.2f, 1f)), true);
    public static final Item DATAPACK_ITEM = Registerizer.item(MOD_ID, "datapack_item", new DatapackItem(new Settings()));
    public static final Item DATAPACK_HAT = Registerizer.item(MOD_ID, "datapack_hat", new DatapackHat(new Settings()));
    public static final Item DATAPACK_SCARF = Registerizer.item(MOD_ID, "datapack_scarf", new DatapackScarf(new Settings(), MOD_ID, "datapack_scarf"));
    public static final Item DATAPACK_BACK_ITEM = Registerizer.item(MOD_ID, "datapack_back_item", new BackRenderItem(new Settings()));
	public static final Item SCARF = Registerizer.item(MOD_ID, "scarf", new DyeableScarf(new Settings(), MOD_ID, "scarf"));
    public static final Item PHANTOM_SCARF = Registerizer.item(MOD_ID, "phantom_scarf", isTrinketsInstalled ?
            new ScarfTrinket(new Settings().attributeModifiers(AttributeModifiersComponent.builder()
                    .add(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, new EntityAttributeModifier(Identifier.of(MOD_ID, "armor.phantom_scarf"), 2, Operation.ADD_VALUE), AttributeModifierSlot.ARMOR)
                    .build()), MOD_ID, "phantom_scarf") : 
            new ScarfItem(new Settings().attributeModifiers(AttributeModifiersComponent.builder()
                    .add(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, new EntityAttributeModifier(Identifier.of(MOD_ID, "armor.phantom_scarf"), 2, Operation.ADD_VALUE), AttributeModifierSlot.ARMOR)
                    .build()), MOD_ID, "phantom_scarf"));
    public static final Item SPIRIT_SCARF = Registerizer.item(MOD_ID, "spirit_scarf", isTrinketsInstalled ?
            new ScarfTrinket(new Settings().attributeModifiers(AttributeModifiersComponent.builder()
                    .add(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.of(MOD_ID, "armor.spirit_scarf"), 1, Operation.ADD_VALUE), AttributeModifierSlot.ARMOR)
                    .add(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.of(MOD_ID, "armor.spirit_scarf"), 1, Operation.ADD_VALUE), AttributeModifierSlot.ARMOR)
                    .build()), MOD_ID, "spirit_scarf") : 
            new ScarfItem(new Settings().attributeModifiers(AttributeModifiersComponent.builder()
                    .add(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.of(MOD_ID, "armor.spirit_scarf"), 1, Operation.ADD_VALUE), AttributeModifierSlot.ARMOR)
                    .add(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.of(MOD_ID, "armor.spirit_scarf"), 1, Operation.ADD_VALUE), AttributeModifierSlot.ARMOR)
                    .build()), MOD_ID, "spirit_scarf"));
    public static final Item ECHO_SCARF = Registerizer.item(MOD_ID, "echo_scarf", isTrinketsInstalled ?
            new ScarfTrinket(new Settings().attributeModifiers(AttributeModifiersComponent.builder()
                    .add(EntityAttributes.PLAYER_SNEAKING_SPEED, new EntityAttributeModifier(Identifier.of(MOD_ID, "armor.echo_scarf"), 0.3, Operation.ADD_VALUE), AttributeModifierSlot.ARMOR)
                    .build()), MOD_ID, "echo_scarf") : 
            new ScarfItem(new Settings().attributeModifiers(AttributeModifiersComponent.builder()
                    .add(EntityAttributes.PLAYER_SNEAKING_SPEED, new EntityAttributeModifier(Identifier.of(MOD_ID, "armor.echo_scarf"), 0.3, Operation.ADD_VALUE), AttributeModifierSlot.ARMOR)
                    .build()), MOD_ID, "echo_scarf"));
	public static final Block JUMP_ORB_NORMAL = Registerizer.block(MOD_ID, "jump_orb_normal", new JumpOrbBlock(NORMAL_DOUBLE_JUMP), true);
	public static final Block JUMP_ORB_STRONG = Registerizer.block(MOD_ID, "jump_orb_strong", new JumpOrbBlock(STRONG_DOUBLE_JUMP), true);
	public static final Item PEARL = Registerizer.item(MOD_ID, "pearl", new Pearl(new Settings().component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.AWKWARD)).component(PEARL_STATS, Ints.asList(1,1,1,0))));
	public static final Item BIFURCATED_PEARL = Registerizer.item(MOD_ID, "bifurcated_pearl", new Pearl(new Settings().component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.AWKWARD)).component(PEARL_STATS, Ints.asList(1,1,1,0))));
	public static final Block PEARL_HUB = Registerizer.block(MOD_ID, "pearl_hub", new PearlHub(AbstractBlock.Settings.copy(Blocks.POLISHED_DEEPSLATE_SLAB).luminance(state -> 1)), true);
    public static final Block BUDDING_PEARL_ICE = Registerizer.block(MOD_ID, "budding_pearl_ice", new BuddingPearlIce(AbstractBlock.Settings.copy(Blocks.PACKED_ICE).mapColor(MapColor.PINK).slipperiness(0.9999f).nonOpaque().solidBlock(Blocks::never)), false);
    public static final Block PEARL_ICE = Registerizer.block(MOD_ID, "pearl_ice", new PearlIce(AbstractBlock.Settings.copy(Blocks.PACKED_ICE).mapColor(MapColor.PINK).slipperiness(0.9999f)), true);
    public static final Block ICICLE = Registerizer.block(MOD_ID, "icicle", new Icicle(AbstractBlock.Settings.copy(Blocks.ICE).nonOpaque().dynamicBounds().pistonBehavior(PistonBehavior.DESTROY).solidBlock(Blocks::never)), true);
    public static final Block ICE_FUR = Registerizer.block(MOD_ID, "ice_fur", new IceFur(AbstractBlock.Settings.copy(Blocks.ICE).sounds(BlockSoundGroup.SNOW).dropsNothing().nonOpaque().replaceable().breakInstantly().noCollision().pistonBehavior(PistonBehavior.DESTROY).solidBlock(Blocks::never)), true);
    public static final Block BRITTLE_BLOCK = Registerizer.block(MOD_ID, "brittle_block", new BrittleBlock(AbstractBlock.Settings.copy(Blocks.ICE).slipperiness(0.8f).strength(0.4f, 0.1f).pistonBehavior(PistonBehavior.DESTROY)), false);
    public static final Block LOST_SOUL_ECHO = Registerizer.block(MOD_ID, "lost_soul_echo", new LostSoulEcho(AbstractBlock.Settings.create().dropsNothing()), false);
    public static final Block TINTED_BOTTLE_BLOCK = Registerizer.block(MOD_ID, "tinted_bottle", new TintedBottleBlock(AbstractBlock.Settings.create().mapColor(MapColor.GRAY).solid().blockVision(Blocks::never).breakInstantly().sounds(BlockSoundGroup.GLASS).nonOpaque().pistonBehavior(PistonBehavior.DESTROY), TINTED_BOTTLE), false);
    public static final Block SCULK_BOTTLE_BLOCK = Registerizer.block(MOD_ID, "sculk_bottle", new TintedBottleBlock(AbstractBlock.Settings.create().mapColor(MapColor.GRAY).solid().blockVision(Blocks::never).breakInstantly().sounds(BlockSoundGroup.GLASS).nonOpaque().pistonBehavior(PistonBehavior.DESTROY), SCULK_BOTTLE), false);
    public static final Block SOUL_BOTTLE_BLOCK = Registerizer.block(MOD_ID, "soul_bottle", new TintedBottleBlock(AbstractBlock.Settings.create().mapColor(MapColor.GRAY).solid().blockVision(Blocks::never).breakInstantly().sounds(BlockSoundGroup.GLASS).nonOpaque().pistonBehavior(PistonBehavior.DESTROY), SOUL_BOTTLE), false);
    public static final Block FIREBALL_BOTTLE_BLOCK = Registerizer.block(MOD_ID, "fireball_bottle", new TintedBottleBlock(AbstractBlock.Settings.create().mapColor(MapColor.GRAY).solid().blockVision(Blocks::never).breakInstantly().sounds(BlockSoundGroup.GLASS).nonOpaque().pistonBehavior(PistonBehavior.DESTROY), FIREBALL_BOTTLE), false);
    public static final Block LIGHTNING_BOTTLE_BLOCK = Registerizer.block(MOD_ID, "lightning_bottle", new TintedBottleBlock(AbstractBlock.Settings.create().mapColor(MapColor.GRAY).solid().blockVision(Blocks::never).breakInstantly().sounds(BlockSoundGroup.GLASS).nonOpaque().pistonBehavior(PistonBehavior.DESTROY), LIGHTNING_BOTTLE), false);
    public static final Block SHULKER_BOTTLE_BLOCK = Registerizer.block(MOD_ID, "shulker_bottle", new TintedBottleBlock(AbstractBlock.Settings.create().mapColor(MapColor.GRAY).solid().blockVision(Blocks::never).breakInstantly().sounds(BlockSoundGroup.GLASS).nonOpaque().pistonBehavior(PistonBehavior.DESTROY), SHULKER_BOTTLE), false);
    // Block Entity -----------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static BlockEntityType<RainChimeBlockEntity> RAIN_CHIME_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, "rain_chime"), BlockEntityType.Builder.create(RainChimeBlockEntity::new, RAIN_CHIME).build());
    public static BlockEntityType<BuddingPearlIceBlockEntity> BUDDING_PEARL_ICE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, "budding_pearl_ice"), BlockEntityType.Builder.create(BuddingPearlIceBlockEntity::new, BUDDING_PEARL_ICE).build());
    public static BlockEntityType<PearlHubBlockEntity> PEARL_HUB_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, "pearl_hub"), BlockEntityType.Builder.create(PearlHubBlockEntity::new, PEARL_HUB).build());
    public static BlockEntityType<BrittleBlockEntity> BRITTLE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, "brittle_block"), BlockEntityType.Builder.create(BrittleBlockEntity::new, BRITTLE_BLOCK).build());
    // World Gen --------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static final RegistryKey<World> DREAM_SWEET = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(MOD_ID, "dream_sweet"));
    public static final StructurePlacementType<UniqueStructurePlacement> UNIQUE_PLACEMENT_TYPE = Registry.register(Registries.STRUCTURE_PLACEMENT, Identifier.of(MOD_ID, "unique"), () -> UniqueStructurePlacement.CODEC);
    public static final RegistryKey<PlacedFeature> ICICLE_DOWN_FEATURE = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, "icicle_down"));
	// Attribute --------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static final RegistryEntry<EntityAttribute> JUMPS = Registry.registerReference(Registries.ATTRIBUTE, Identifier.of(MOD_ID, "double_jump"), new ClampedEntityAttribute("attribute." + MOD_ID + '.' + "jumps", 0, 0, 1024).setTracked(true));
    // Game Events ------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static final RegistryEntry.Reference<GameEvent> SHIELD_PARRY_EVENT = Registerizer.gameEvent(MOD_ID, "shield_parry");
    public static final RegistryEntry.Reference<GameEvent> REVERBERATE_EVENT = Registerizer.gameEvent(MOD_ID, "reverberate");
    // Enchants ---------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static final RegistryKey<Enchantment> SPARKING_ENCHANT = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "sparking"));
    public static final RegistryKey<Enchantment> RECOILING_CURSE = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "recoiling_curse"));
    
    

	public static final ItemGroup ResonanceModGroup = Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "resonance_group_id"), FabricItemGroup.builder().displayName(Text.translatable("itemGroup.resonance.resonance_group_id")).icon(() -> new ItemStack(CHARGED_AMETHYST_SHARD)).entries((displayContext, entries) -> {
        entries.add(Items.BUNDLE);
        entries.add(SMALL_CHARGED_AMETHYST_BUD);
        entries.add(MEDIUM_CHARGED_AMETHYST_BUD);
        entries.add(LARGE_CHARGED_AMETHYST_BUD);
        entries.add(CHARGED_AMETHYST_CLUSTER);
        entries.add(CHARGED_AMETHYST_SHARD);
        entries.add(WHISTLING_ARROW);
        entries.add(ROCK_CANDY);
        entries.add(ENCHANTED_ROCK_CANDY);
        entries.add(RAIN_CHIME);
        entries.add(AMETHYST_SHIELD);
        entries.add(COPPER_SHIELD);
        entries.add(TINTED_BOTTLE);
        entries.add(SCULK_BOTTLE);
        entries.add(SOUL_BOTTLE);
        entries.add(FIREBALL_BOTTLE);
        entries.add(LIGHTNING_BOTTLE);
        entries.add(SHULKER_BOTTLE);
        entries.add(SCARF);
        entries.add(PHANTOM_SCARF);
        entries.add(SPIRIT_SCARF);
        entries.add(ECHO_SCARF);
        entries.add(TREASURE_HOARD);
        entries.add(BOOK_PILE);
        entries.add(COMPOST_HEAP);
        entries.add(JUMP_ORB_NORMAL);
        entries.add(JUMP_ORB_STRONG);
        entries.add(PEARL);
        entries.add(BIFURCATED_PEARL);
        entries.add(PEARL_HUB);
        entries.add(PEARL_ICE);
        entries.add(ICICLE);
        entries.add(ICE_FUR);
    }).build());

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	public void onInitialize() {
        FuelRegistry.INSTANCE.add(BOOK_PILE, 600);
        FuelRegistry.INSTANCE.add(Items.FIRE_CHARGE, 1200);
        CompostingChanceRegistry.INSTANCE.add(Items.BAMBOO, 0.3f);
        CompostingChanceRegistry.INSTANCE.add(ROCK_CANDY, 0.2f);
        CompostingChanceRegistry.INSTANCE.add(ENCHANTED_ROCK_CANDY, 0.2f);
        CompostingChanceRegistry.INSTANCE.add(COMPOST_HEAP, 1f);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
            if (entries.getContext().hasPermissions()) {
                entries.add(DATAPACK_BLOCK);
                entries.add(DATAPACK_ITEM);
                entries.add(DATAPACK_HAT);
                entries.add(DATAPACK_SCARF);
                entries.add(DATAPACK_BACK_ITEM);
            }
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.addAfter(Items.LLAMA_SPAWN_EGG, LOST_SOUL_SPAWN_EGG);
        });

        Registry.register(Registries.ITEM_SUB_PREDICATE_TYPE, Identifier.of(MOD_ID, "stored_crits"), new ItemSubPredicate.Type(StoredCritsPredicate.CODEC));

        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map().putIfAbsent(SCARF, CauldronBehavior.CLEAN_DYEABLE_ITEM);

        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.POLAR_BEARS_SPAWN_ON_ALTERNATE_BLOCKS), GenerationStep.Feature.VEGETAL_DECORATION, ICICLE_DOWN_FEATURE);

        SculkSensorFrequencyRegistry.register(SHIELD_PARRY_EVENT.registryKey(), 5);
        SculkSensorFrequencyRegistry.register(REVERBERATE_EVENT.registryKey(), 15);

        ModifyLootAndTradeTables.modifyLootTables();
		ModParticles.registerModParticles();
		ModEntities.registerModEntities();
        ModEvents.registerEvents();
        
		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.SOUL_SAND_VALLEY), SpawnGroup.MONSTER, ModEntities.LOST_SOUL, 5, 1, 2);
        SpawnRestriction.register(ModEntities.LOST_SOUL, SpawnRestriction.getLocation(ModEntities.LOST_SOUL), Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);

        PayloadTypeRegistry.playC2S().register(JumpPayload.ID, JumpPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CustomPayload.id("resonance/jump"), new ServerPlayNetworking.PlayPayloadHandler<JumpPayload>() {
            @Override public void receive(JumpPayload payload, ServerPlayNetworking.Context context) { DoubleJumpEffect.performDoubleJump(context.player()); }
        });
	}
}