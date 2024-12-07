package net.moss.resonance.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.moss.resonance.Resonance;
import net.moss.resonance.ReverberationRecipe;
import net.moss.resonance.block.BuddingPearlIce;
import net.moss.resonance.block.JumpOrbBlock;
import net.moss.resonance.command.LocateUniqueStructureCommand;
import net.moss.resonance.entity.ModEntities;
import net.moss.resonance.mixin.MobSpawnerLogicAccessor;
import net.moss.resonance.particle.ModParticles;
import net.moss.resonance.util.EnchantUtils;
import net.moss.resonance.util.Utils;
import net.moss.resonance.util.Utils.CustomWindChargeBehavior;
import net.moss.resonance.util.interfaces.CritStoringInterface;
import net.moss.resonance.world.UniqueStructureDataSaver;
import net.moss.resonance.world.UniqueStructurePlacement;

public class ModEvents {
	public static ActionResult onEntityInteract(PlayerEntity player, World world, Hand hand, Entity target, EntityHitResult hitResult) {
		if (world.isClient()) { return ActionResult.PASS; }
		ItemStack itemstack = player.getStackInHand(hand);
		if (itemstack.getItem().equals(Resonance.TINTED_BOTTLE)) {
            Boolean successfulPickup = true;
            ItemStack bottleType = Resonance.TINTED_BOTTLE.getDefaultStack();
			if (target.getType() == EntityType.FIREBALL) { bottleType = Resonance.FIREBALL_BOTTLE.getDefaultStack(); }
            else if (target.getType() == EntityType.SHULKER_BULLET) { bottleType = Resonance.SHULKER_BOTTLE.getDefaultStack(); }
            else if (target.getType() == ModEntities.LOST_SOUL) { bottleType = Resonance.SOUL_BOTTLE.getDefaultStack(); }
            else { successfulPickup = false; }
            if (successfulPickup) {
                Resonance.TINTED_BOTTLE_CRITERION.trigger((ServerPlayerEntity)player, target);
                world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.PLAYERS);
				player.incrementStat(Stats.USED.getOrCreateStat(itemstack.getItem()));
                target.remove(RemovalReason.DISCARDED);
                if (itemstack.getCount() > 1) {
                    ItemUsage.exchangeStack(itemstack, player, bottleType);
                } else {
                    itemstack.decrement(1);
                    player.setStackInHand(hand, bottleType);
                }
                player.getItemCooldownManager().set(bottleType.getItem(), 20);
				return ActionResult.SUCCESS;
            }
		}
		return ActionResult.PASS;
	}

    public static ActionResult onBlockInteract(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		ItemStack itemstack = player.getStackInHand(hand);
        BlockPos pos = hitResult.getBlockPos();
		BlockState state = world.getBlockState(pos);
        if ((itemstack.isOf(Items.AMETHYST_SHARD) || itemstack.isOf(Resonance.CHARGED_AMETHYST_SHARD) || itemstack.isOf(Resonance.PEARL) || itemstack.isOf(Resonance.BIFURCATED_PEARL)) && state.isOf(Blocks.ICE)) {
            if (player instanceof ServerPlayerEntity serverPlayerEntity) { Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayerEntity, pos, itemstack); }
            player.incrementStat(Stats.USED.getOrCreateStat(itemstack.getItem()));
            world.setBlockState(pos, Resonance.BUDDING_PEARL_ICE.getDefaultState());
            state = world.getBlockState(pos);
            world.playSound(player, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS);
            ((BuddingPearlIce)state.getBlock()).putSeed(world, pos, state, itemstack);
            return ActionResult.SUCCESS;
        } else if (itemstack.isOf(Items.SHEARS)) {
            Block block = null;
            if (state.isOf(Blocks.AMETHYST_CLUSTER)) { block = Blocks.LARGE_AMETHYST_BUD; }
            else if (state.isOf(Blocks.LARGE_AMETHYST_BUD)) { block = Blocks.MEDIUM_AMETHYST_BUD; }
            else if (state.isOf(Blocks.MEDIUM_AMETHYST_BUD)) { block = Blocks.SMALL_AMETHYST_BUD; }
            else if (state.isOf(Resonance.CHARGED_AMETHYST_CLUSTER)) { block = Resonance.LARGE_CHARGED_AMETHYST_BUD; }
            else if (state.isOf(Resonance.LARGE_CHARGED_AMETHYST_BUD)) { block = Resonance.MEDIUM_CHARGED_AMETHYST_BUD; }
            else if (state.isOf(Resonance.MEDIUM_CHARGED_AMETHYST_BUD)) { block = Resonance.SMALL_CHARGED_AMETHYST_BUD; }
            if (block != null) {
                if (player instanceof ServerPlayerEntity serverPlayerEntity) { Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayerEntity, pos, itemstack); }
                player.incrementStat(Stats.USED.getOrCreateStat(itemstack.getItem()));
                world.addBlockBreakParticles(pos, state);
                world.setBlockState(pos, block.getStateWithProperties(state));
                world.playSound(player, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.BLOCKS);
                itemstack.damage(4, player, LivingEntity.getSlotForHand(hand));
                player.getItemCooldownManager().set(Items.SHEARS, 20);
            }
        }
		return ActionResult.PASS;
	}

    public static TypedActionResult<ItemStack> onItemUse(PlayerEntity player, World world, Hand hand) {
		ItemStack itemstack = player.getStackInHand(hand);
		if (world instanceof ServerWorld serverWorld) { EnchantUtils.onStackUsed(serverWorld, itemstack, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, (item) -> {}); }
        return TypedActionResult.pass(itemstack); 
	}

    public static void onServerStart(MinecraftServer server) {
        UniqueStructureDataSaver serverState = UniqueStructureDataSaver.getServerState(server);
        if (serverState.generated) {
            server.getOverworld().getChunkManager().getStructurePlacementCalculator().getStructureSets().forEach(structureSet -> {
                if (structureSet.value().placement() instanceof UniqueStructurePlacement placement) {
                    String name = structureSet.getIdAsString();
                    if (!serverState.name.contains(name)) { return; }
                    BlockPos pos = serverState.pos.get(serverState.name.indexOf(name));
                    placement.setChunk(pos.getX() >> 4, pos.getZ() >> 4);
                }
            });
        } else {
            int maxSearchRadius = 200;
            int sizeMultiplier = 100;
            for (RegistryKey<World> WorldKey : server.getWorldRegistryKeys().stream().toList()) {
                ServerWorld world = server.getWorld(WorldKey);
                List<RegistryEntry<StructureSet>> structureSets = new ArrayList<>();
                world.getChunkManager().getStructurePlacementCalculator().getStructureSets().forEach(structureSet -> {
                    if (structureSet.value().placement() instanceof UniqueStructurePlacement) { structureSets.add(structureSet); }
                });
                
                for (int searchRadius = 1; searchRadius <= maxSearchRadius; searchRadius++) {
                    List<Float> angles = new ArrayList<>();
                    for (float i = 0; i < 2 * Math.PI; i += (1 / (float) (searchRadius))) { angles.add(i); }
                    Collections.shuffle(angles);
                    for (Float searchAngle : angles) {
                        int x = (int) (Math.cos(searchAngle) * searchRadius * sizeMultiplier);
                        int z = (int) (Math.sin(searchAngle) * searchRadius * sizeMultiplier);
                        for (RegistryEntry<StructureSet> structureSet : structureSets) {
                            if (structureSet.value().placement() instanceof UniqueStructurePlacement placement) {
                                if (searchRadius * sizeMultiplier < placement.getDistance()) { break; }
                                BlockPos pos = new BlockPos(x, placement.getTestHeight(), z);
                                int depth = placement.getBiomeDepth();
                                RegistryEntryList<Biome> biomes = placement.getBiomes();
                                if (!biomes.contains(world.getBiome(pos.north(depth)))) { break; }
                                if (!biomes.contains(world.getBiome(pos.south(depth)))) { break; }
                                if (!biomes.contains(world.getBiome(pos.east(depth)))) { break; }
                                if (!biomes.contains(world.getBiome(pos.west(depth)))) { break; }
                                if (biomes.contains(world.getBiome(pos))) {
                                    serverState.name.add(structureSet.getIdAsString());
                                    serverState.pos.add(pos);
                                    placement.setChunk(pos.getX() >> 4, pos.getZ() >> 4);
                                    Resonance.LOGGER.info("Assigned [" + structureSet.getIdAsString() + "] To: " + pos);
                                }
                            }
                            structureSets.remove(structureSet);
                            break;
                        }
                        if (structureSets.isEmpty()) { break; }
                    }
                    if (structureSets.isEmpty()) { break; }
                }
            }
            serverState.generated = true;
        }
	}

    public static void onReverberate(World world, Vec3d pos, Vector3f angle, @Nullable Entity immuneEntity, @Nullable Entity sourceEntity, float strength, boolean windCharged) {
        world.emitGameEvent(Resonance.REVERBERATE_EVENT, pos, GameEvent.Emitter.of(world.getBlockState(BlockPos.ofFloored(pos))));
		if (!windCharged) { world.createExplosion(sourceEntity, Utils.damageSource(world, Resonance.REVERBERATION_DAMAGE), new CustomWindChargeBehavior(0f), pos.getX(), pos.getY(), pos.getZ(), strength / 2, false, World.ExplosionSourceType.TRIGGER, ModParticles.AMETHYST_PARTICLE, ModParticles.AMETHYST_PARTICLE, Resonance.REVERBERATION_SOUND); }
		if (world instanceof ServerWorld world2) {
			world2.spawnParticles(ModParticles.SMALL_RING_PARTICLE, pos.getX(), pos.getY(), pos.getZ(), 1, 0, 0, 0, 3);
			world2.spawnParticles(ModParticles.AMETHYST_PARTICLE, pos.getX(), pos.getY(), pos.getZ(), (int) (10 * strength), 0, 0, 0, 1);
			if (strength >= 1.5) { world2.spawnParticles(ModParticles.MEDIUM_RING_PARTICLE, pos.getX(), pos.getY(), pos.getZ(), 1, 0, 0, 0, 1); }
			for (Direction direction : Direction.values()) {
				BlockPos blockPos = BlockPos.ofFloored(pos).offset(direction);
				if (world.getBlockEntity(blockPos) instanceof MobSpawnerBlockEntity mobSpawnerBlockEntity) {
					Vec3d spawnerPos = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
					world2.spawnParticles(ModParticles.MEDIUM_RING_PARTICLE, spawnerPos.getX(), spawnerPos.getY(), spawnerPos.getZ(), 1, 0, 0, 0, 1);
					world2.spawnParticles(ParticleTypes.ENCHANT, spawnerPos.getX(), spawnerPos.getY() + 0.75, spawnerPos.getZ(), 20, 0, 0, 0, 1);
					((MobSpawnerLogicAccessor) mobSpawnerBlockEntity.getLogic()).setSpawnDelay(world.random.nextInt(15));
				} else if (world.getBlockState(blockPos).getBlock() == Resonance.BRITTLE_BLOCK) {
                    world.breakBlock(blockPos, true, sourceEntity);
                } else if (world.getBlockState(blockPos).getBlock() == Blocks.TNT) {
                    TntBlock.primeTnt(world, blockPos);
                } else if (world.getBlockState(blockPos).getBlock() instanceof JumpOrbBlock) {
                    Vec3d vec3d = blockPos.toCenterPos();
					world2.spawnParticles(ModParticles.MEDIUM_RING_PARTICLE, vec3d.getX(), vec3d.getY(), vec3d.getZ(), 1, 0, 0, 0, 1);
                    world.playSound(null, blockPos, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.BLOCKS, 2f, 0.5f + world.random.nextFloat() * 1.2f);
                    world.setBlockState(blockPos, world.getBlockState(blockPos).with(JumpOrbBlock.EXPENDED, false), Block.NOTIFY_LISTENERS);
                }
			}
		} else { world.addParticle(ModParticles.SMALL_RING_PARTICLE, true, pos.getX(), pos.getY(), pos.getZ(), angle.x, angle.y, angle.z); }
        List<Entity> list = Utils.entitiesInRadius(world, pos, immuneEntity, 3, 3, Entity::isAlive);
        for (Entity entity2 : list) {
			if (entity2 instanceof ItemEntity entity1 && world instanceof ServerWorld world2) {
				entity2.addVelocity(0, 0.1, 0);
				ItemStack material = entity1.getStack();
				Optional<RecipeEntry<ReverberationRecipe>> recipe = world2.getRecipeManager().getFirstMatch(ReverberationRecipe.Type.INSTANCE, new SingleStackRecipeInput(material), world2);
         		if (recipe.isPresent()) {
					ReverberationRecipe reverberationRecipe = recipe.get().value();
					int explosionPower = reverberationRecipe.getExplosionPower();
					if (reverberationRecipe.getResonates() && strength > 0.2f) { ReverberateEvent.REVERBERATE.invoker().onResonate(world, entity1.getPos(), angle, entity1, sourceEntity, 0.2f, false); }
					if (explosionPower > 0) { world2.createExplosion(sourceEntity, entity1.getX(), entity1.getY(), entity1.getZ(), explosionPower, ExplosionSourceType.MOB); }
					for (ItemStack stack : reverberationRecipe.getResults()) { world.spawnEntity(new ItemEntity(world, entity1.getX(), entity1.getY(), entity1.getZ(), stack.copy(), 0, 0.2, 0)); }
					if (material.getCount() > 0 && reverberationRecipe.getIsMaterialConsumed()) { material.decrement(1); }
				}
            } else if (!entity2.equals(sourceEntity) && entity2 instanceof PersistentProjectileEntity entity1 && !entity1.getItemStack().isEmpty() && entity1.pickupType == PickupPermission.ALLOWED) {
                ItemEntity itemEntity = new ItemEntity(world, entity1.getX(), entity1.getY(), entity1.getZ(), entity1.getItemStack().copy());
                world.spawnEntity(itemEntity);
                entity1.discard();
			} else if (entity2 instanceof LivingEntity entity1 && !entity1.isBlocking()) {
				if (entity2 instanceof CreeperEntity creeperEntity) { creeperEntity.ignite(); }
                else {
					float damage = (float) ((entity2.getType().isIn(Resonance.SOUND_SENSITIVE_TAG) ? 2 : 1) * strength * 8f / Math.max(pos.distanceTo(entity2.getPos()), 1));
                    if (sourceEntity != null && sourceEntity instanceof LivingEntity livingEntity) { entity2.damage(Utils.damageSource(world, Resonance.REVERBERATION_DAMAGE, livingEntity), damage); }
                    else { entity2.damage(Utils.damageSource(world, Resonance.REVERBERATION_DAMAGE, pos), damage); }
				}
				Utils.customKnockbackClamp(entity1, pos, 0.8f * strength, 3f);
			}
		}
	}

    public static ActionResult onParry(LivingEntity defender, @Nullable DamageSource source, float amount, Hand hand, ItemStack shield) {
        defender.emitGameEvent(Resonance.SHIELD_PARRY_EVENT);
        Resonance.PARRY_CRITERION.trigger((ServerPlayerEntity) defender);
        if (shield.getItem() == Resonance.AMETHYST_SHIELD) {
            defender.getWorld().playSound(null, defender.getX(), defender.getY(), defender.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 2f, 1f);
            if (source != null) {
                if (!source.isDirect()) {
                    ReverberateEvent.REVERBERATE.invoker().onResonate(defender.getEntityWorld(), source.getPosition().add(0, 0, 0), new Vector3f(0f, 0f, 0f), defender, defender, 0.5f, false);
                } else {
                    ReverberateEvent.REVERBERATE.invoker().onResonate(defender.getEntityWorld(), source.getAttacker().getPos().add(0, 1.25f, 0), new Vector3f(0f, 0f, 0f), defender, defender, 0.5f, false);
                }
            } else {
                ReverberateEvent.REVERBERATE.invoker().onResonate(defender.getEntityWorld(), defender.getEyePos(), new Vector3f(0f, 0f, 0f), defender, defender, 0.5f, false);
            }
        } else if (shield.getItem() == Resonance.COPPER_SHIELD) {
            if (source != null && source.isDirect()) {
                if (source.getAttacker() instanceof PlayerEntity player) {
                    player.getItemCooldownManager().set(shield.getItem(), 66);
                    player.clearActiveItem();
                    player.getWorld().sendEntityStatus(player, (byte) 30);
                }
                if (source.getAttacker() instanceof LivingEntity entity) {
                    entity.takeKnockback(0.7, defender.getX() - entity.getX(), defender.getZ() - entity.getZ());
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 50, 2, false, false, false));
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 2, false, false, false));
                }
            }
        } else {
            if (source != null && source.isDirect()) {
                if (source.getAttacker() instanceof LivingEntity entity) {
                    entity.takeKnockback(0.3, defender.getX() - entity.getX(), defender.getZ() - entity.getZ());
                }
            }
        }
        return ActionResult.PASS;
	}

    public static void onStaticShock(LivingEntity source, int amplifier, @Nullable LivingEntity attacker) {
        int damage = 1 + amplifier;
        World world2 = source.getWorld();
        if (world2.getDifficulty() == Difficulty.PEACEFUL) { damage -= 1; }
        else if (world2.getDifficulty() == Difficulty.EASY) { damage = (int) Math.min(damage / 2.0f + 1.0f, damage); }
        else if (world2.getDifficulty() == Difficulty.HARD) { damage = (int) (damage * 3.0f / 2.0f); }
        if (world2 instanceof ServerWorld world && source.damage(Utils.damageSource(world, Resonance.SPARKING_DAMAGE), damage)) {
            world2.playSound(null, source.getBlockPos(), Resonance.ELECTRIC_SPARK_SOUND.value(), SoundCategory.NEUTRAL, 1.0f, world2.getRandom().nextFloat() * 0.4f + 0.8f);
            if (attacker != null) { source.takeKnockback(0.2 * (amplifier + 1), attacker.getX() - source.getX(), attacker.getZ() - source.getZ()); }
            world.spawnParticles(ModParticles.SPARK_PARTICLE, source.getX(), source.getY() + source.getHeight()/2, source.getZ(), 20, 0, 0, 0, 1);
            int duration = source.getStatusEffect(Resonance.SPARKING).getDuration();
            if (amplifier > 0) {
                Utils.decrementStatusEffectAmplifiers(source, Resonance.SPARKING, null);
                List<Entity> list = Utils.entitiesInRadius(world, source.getPos(), source, 2, 2, Entity::isAlive);
                for (Entity entity2 : list) {
                    if (entity2 instanceof LivingEntity entity1 && world.random.nextInt(4/Math.min(amplifier + 1, 4)) == 0) {
                        world.spawnParticles(ModParticles.SPARK_PARTICLE, entity2.getX(), entity2.getY() + entity2.getHeight()/2, entity2.getZ(), 20, 0, 0, 0, 1);
                        entity2.damage(Utils.damageSource(world, Resonance.SPARKING_DAMAGE), damage);
                        entity1.takeKnockback(0.2 * (amplifier + 1), source.getX() - entity1.getX(), source.getZ() - entity1.getZ());
                        Utils.sumStatusEffectAmplifiers(entity1, new StatusEffectInstance(Resonance.SPARKING, duration, amplifier - 1), 4);
                    }
                }
            } else {
                source.removeStatusEffect(Resonance.SPARKING);
                if (duration > 100) { source.addStatusEffect(new StatusEffectInstance(Resonance.SPARKING, duration - 100, amplifier)); }
            }
        }
	}

    public static ActionResult onCrit(PlayerEntity attacker, Entity victim, int critCount, Hand hand, ItemStack weapon) {
        if (weapon.getItem() instanceof CritStoringInterface interface1) { interface1.critStreak(weapon, critCount); }
        Resonance.CRIT_CRITERION.trigger((ServerPlayerEntity) attacker, critCount);
        return ActionResult.PASS;
	}

    public static ActionResult onCritDeath(PlayerEntity attacker, Entity victim, int critCount, Hand hand, ItemStack weapon) {
        if (weapon.getItem() instanceof CritStoringInterface interface1) { interface1.critDeath(weapon, critCount); }
        return ActionResult.PASS;
	}

    public static void registerEvents() {
		ReverberateEvent.registerEvents();
		ShieldEvents.registerEvents();
		StaticShockEvent.registerEvents();
        PlayerCritEvent.registerEvents();

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> { return ModEvents.onEntityInteract(player, world, hand, entity, hitResult); });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> { return ModEvents.onBlockInteract(player, world, hand, hitResult); });
        UseItemCallback.EVENT.register((player, world, hand) -> { return ModEvents.onItemUse(player, world, hand); });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> { onServerStart(server); });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> { LocateUniqueStructureCommand.register(dispatcher, false); } );

        ReverberateEvent.REVERBERATE.register((world, pos, angle, immuneEntity, sourceEntity, strength, windCharged) -> { onReverberate(world, pos, angle, immuneEntity, sourceEntity, strength, windCharged); });
        ShieldEvents.SHIELD_BLOCK.register((defender, amount, hand, shield) -> { return ActionResult.PASS; });
        ShieldEvents.SHIELD_DISABLE.register((defender, hand, shield) -> { return ActionResult.PASS; });
        ShieldEvents.SHIELD_PARRY.register((defender, source, amount, hand, shield) -> { return onParry(defender, source, amount, hand, shield); });
        StaticShockEvent.STATIC_SHOCK.register((source, amplifier, attacker) -> { onStaticShock(source, amplifier, attacker); });
        PlayerCritEvent.CRIT_EVENT.register((attacker, victim, critCount, hand, weapon) -> { return onCrit(attacker, victim, critCount, hand, weapon); });
        PlayerCritEvent.CRIT_DEATH_EVENT.register((attacker, victim, critCount, hand, weapon) -> { return onCritDeath(attacker, victim, critCount, hand, weapon); });
	}
}