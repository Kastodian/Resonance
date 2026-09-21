package net.moss.resonance.block.entity;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.AlternatePearl;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.PearlHubBlock;
import net.moss.resonance.entity.BeaconedPearlEntity;
import net.moss.resonance.entity.ModEntities;
import net.moss.resonance.item.Pearl;
import net.moss.resonance.util.PearlStatsComponent;
import net.moss.resonance.util.abstracts.BlockEntityWithItemIntentory;

public class PearlHubBlockEntity extends BlockEntityWithItemIntentory {
	protected int charge;
	protected int level;

	public PearlHubBlockEntity(BlockPos pos, BlockState state) {
		super(Resonance.PEARL_HUB_BLOCK_ENTITY, pos, state);
	}

	public PearlHubBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public int getLevel() {
		return level;
	}

	public void beaconActivate(World world, BlockPos pos, BlockState state, PearlHubBlockEntity entity, BeaconBlockEntity beacon, int level) {
		if (entity.charge <= -1 || !state.get(PearlHubBlock.BEACON_POWERED)) {
			BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
			BeaconBlockEntity.playSound(world, pos, Resonance.REVERBERATION_SOUND.value());
			world.setBlockState(pos, state.with(PearlHubBlock.BEACON_POWERED, true));
			if (entity.hasStack()) {
				BeaconedPearlEntity beaconedPearlEntity = new BeaconedPearlEntity(world, pos.getX() + 0.5, pos.getY() + 2.2, pos.getZ() + 0.5, entity.getStack());
				world.spawnEntity(beaconedPearlEntity);
				for (ServerPlayerEntity player : world.getNonSpectatingEntities(ServerPlayerEntity.class, new Box(pos).expand(12))) {
					Resonance.PEARL_HUB_CRITERION.trigger(player, entity.getStack(), level);
				}
			}
		}
		entity.level = level;
		entity.charge = 1;
	}

	public static void tick(World world, BlockPos pos, BlockState state, PearlHubBlockEntity entity) {
		if (world.getTime() % 80L == 0L) {
			world.playSound(null, pos, SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS, 0.3F, 1.0F);
			if (entity.charge <= -1) {
				BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
				BeaconBlockEntity.playSound(world, pos, Resonance.REVERBERATION_SOUND.value());
				world.setBlockState(pos, state.with(PearlHubBlock.BEACON_POWERED, false));
				for (BeaconedPearlEntity beaconedPearlEntity : world.getEntitiesByType(ModEntities.BEACONED_PEARL, new Box(pos.up(2)), BeaconedPearlEntity::isAttackable)) {
					beaconedPearlEntity.remove(RemovalReason.DISCARDED);
				}
				entity.level = 0;
			}
			entity.charge--;
		}
	}

	public int usePearl(World world, BlockPos pos, PlayerEntity player) {
		if (this.hasStack() && world instanceof ServerWorld serverWorld) {
			world.emitGameEvent(null, GameEvent.BLOCK_ACTIVATE, pos);
			Resonance.PEARL_HUB_CRITERION.trigger((ServerPlayerEntity) player, this.getStack(), 0);
			return affectEntities(serverWorld, pos, List.of(player));
		}
		return 3;
	}

	public int pulsePearl(World world, BlockPos pos) {
		if (this.hasStack() && world instanceof ServerWorld serverWorld) {
			world.emitGameEvent(null, GameEvent.BLOCK_ACTIVATE, pos);
			if (!this.getCachedState().get(PearlHubBlock.WATERLOGGED)) {
				world.playSound(null, pos, SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK, SoundCategory.BLOCKS, 0.5f, 0.6f + world.random.nextFloat() * 0.4f);
			}
			return affectEntities(serverWorld, pos, world.getNonSpectatingEntities(LivingEntity.class, new Box(pos).expand(3)));
		}
		return 3;
	}

	public int affectEntities(ServerWorld world, BlockPos pos, List<LivingEntity> entities) {
		List<Integer> pearlStats = this.getStack().getOrDefault(Resonance.PEARL_STATS, PearlStatsComponent.DEFAULT).stats();
		int recuperation = pearlStats.get(1);
		if (this.getStack().contains(DataComponentTypes.POTION_CONTENTS)) {
			float duration = pearlStats.get(0);
			float strength = pearlStats.get(2);
			this.getStack().get(DataComponentTypes.POTION_CONTENTS).forEachEffect(effect -> {
				StatusEffectInstance newEffect = new StatusEffectInstance(effect.getEffectType(), (int) (duration * effect.getDuration() / 3F), (int) Math.max(0, strength * effect.getAmplifier() / 3F));
				for (LivingEntity livingEntity : entities) {
					livingEntity.addStatusEffect(newEffect);
				}
			});
			world.playSound(null, pos, Resonance.PEARL_HUB_USE_SOUND.value(), SoundCategory.BLOCKS);
		} else {
			Optional<AlternatePearl> alternateOrb = world.getServer().getRegistryManager().get(Resonance.ALTERNATE_PEARLS_REGISTRY_KEY).stream().filter(alternatePearl -> alternatePearl.matches(this.getStack())).findFirst();
			if (alternateOrb.isPresent()) {
				AlternatePearl orb = alternateOrb.get();
				orb.effects().forEach(effect -> {
					StatusEffectInstance newEffect = new StatusEffectInstance(effect.getEffectType(), effect.getDuration(), effect.getAmplifier());
					for (LivingEntity livingEntity : entities) {
						livingEntity.addStatusEffect(newEffect);
					}
				});
				world.playSound(null, pos, orb.sound(), SoundCategory.BLOCKS);
				recuperation = orb.recuperation();
			}
		}

		if (this.getStack().isOf(Items.ENDER_PEARL)) {
			for (LivingEntity livingEntity : entities) {
				if (livingEntity.hasVehicle()) { livingEntity.stopRiding(); }
				for (int i = 0; i < 16; ++i) {
					double d = livingEntity.getX() + (world.getRandom().nextDouble() - 0.5) * 16.0;
					double e = MathHelper.clamp(livingEntity.getY() + world.getRandom().nextInt(16) - 8, world.getBottomY(), world.getBottomY() + world.getLogicalHeight() - 1);
					double f = livingEntity.getZ() + (world.getRandom().nextDouble() - 0.5) * 16.0;
					if (!livingEntity.teleport(d, e, f, true)) { continue; }
					world.emitGameEvent(GameEvent.TELEPORT, livingEntity.getPos(), GameEvent.Emitter.of(livingEntity));
					world.playSound(null, livingEntity.getBlockPos(), SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.BLOCKS);
					livingEntity.onLanding();
					break;
				}
			}
		}

		if (this.getStack().getItem() instanceof Pearl) {
			ItemStack stack = this.getStack();
			int i = stack.getDamage() + 1;
			stack.set(DataComponentTypes.DAMAGE, i);
			if (i == stack.getMaxDamage()) {
				this.dropStack(world, pos, 0.5, 0.6, 0.5);
				this.clear();
				world.setBlockState(pos, this.getCachedState().with(PearlHubBlock.HAS_PEARL, false));
			} else {
				this.setStack(stack);
			}
		}

		return recuperation;
	}

	@Override
	public ItemStack decreaseStack(int amount) {
		ItemStack stack = this.getStack();
		this.clear();
		this.getWorld().setBlockState(this.getPos(), this.getCachedState().with(PearlHubBlock.HAS_PEARL, false));
		return stack;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		this.setStack(stack);
		this.getWorld().setBlockState(this.getPos(), this.getCachedState().with(PearlHubBlock.HAS_PEARL, !stack.isEmpty()));
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return super.isValid(slot, stack) && (stack.isIn(Resonance.PEARL_ITEMS_TAG) || stack.isOf(Resonance.PEARL) || stack.isOf(Resonance.BIFURCATED_PEARL));
	}
}