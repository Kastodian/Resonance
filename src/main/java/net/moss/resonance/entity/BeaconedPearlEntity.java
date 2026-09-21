package net.moss.resonance.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.joml.Vector3f;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.moss.resonance.AlternatePearl;
import net.moss.resonance.Resonance;
import net.moss.resonance.ResonanceEvents;
import net.moss.resonance.block.PearlHubBlock;
import net.moss.resonance.block.entity.PearlHubBlockEntity;

public class BeaconedPearlEntity extends MobEntity {
	protected List<StatusEffectInstance> effects = new ArrayList<StatusEffectInstance>();

	public BeaconedPearlEntity(EntityType<? extends MobEntity> entityType, World world) {
		super(entityType, world);
	}

	public BeaconedPearlEntity(World world, double x, double y, double z) {
		this(ModEntities.BEACONED_PEARL, world);
		this.setNoGravity(true);
		this.setPersistent();
		this.setPosition(x, y, z);
	}

	public BeaconedPearlEntity(World world, double x, double y, double z, ItemStack stack) {
		this(world, x, y, z);
		this.setStackInHand(Hand.MAIN_HAND, stack);
		if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
			this.setCustomName(stack.get(DataComponentTypes.CUSTOM_NAME));
		}
	}

	@Override public PistonBehavior getPistonBehavior() { return PistonBehavior.IGNORE; }
	@Override public boolean isPushable() { return false; }
	@Override public boolean isCollidable() { return this.isAlive(); }
	@Override protected void pushAway(Entity entity) { return; }
	@Override public void takeKnockback(double strength, double x, double z) { return; }
	@Override public boolean canHaveStatusEffect(StatusEffectInstance effect) { return false; }
	@Override public boolean isInvulnerableTo(DamageSource damageSource) { return damageSource.isOf(Resonance.REVERBERATION_DAMAGE) || damageSource.isOf(DamageTypes.IN_WALL) || super.isInvulnerableTo(damageSource); }
	@Override public ItemStack getPickBlockStack() { return this.getMainHandStack(); }
	@Override public boolean canTeleportBetween(World from, World to) { return false; }
	@Override public void move(MovementType movementType, Vec3d movement) { return; }

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (this.hurtTime <= 0 && !isInvulnerableTo(source)) {
			ResonanceEvents.REVERBERATE.invoker().onResonate(this.getWorld(), this.getEyePos(), new Vector3f(MathHelper.HALF_PI, 0, 0), this, source.getAttacker(), 2F, false);
		}
		return super.damage(source, 1);
	}

	public static DefaultAttributeContainer.Builder createLostSoulAttributes() {
		return LivingEntity.createLivingAttributes()
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 0F)
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 4F)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0F);
	}

	@Override
	public void onDeath(DamageSource damageSource) {
		ResonanceEvents.REVERBERATE.invoker().onResonate(this.getWorld(), this.getEyePos(), new Vector3f(MathHelper.HALF_PI, 0, 0), this, damageSource.getAttacker(), 2.5F, false);
		this.dropStack(this.getMainHandStack());
		if (this.getWorld().getBlockEntity(getBlockPos().down(2)) instanceof PearlHubBlockEntity blockEntity) {
			blockEntity.clear();
			this.getWorld().setBlockState(getBlockPos().down(2), blockEntity.getCachedState().with(PearlHubBlock.HAS_PEARL, false));
		}
		super.onDeath(damageSource);
	}

	@Override
	public void tick() {
		super.tick();
		World world = this.getWorld();
		ItemStack stack = this.getMainHandStack();
		BlockPos pos = this.getBlockPos();
		if (world.getBlockEntity(pos.down(2)) instanceof PearlHubBlockEntity blockEntity && !stack.isEmpty()) {
			if (world.getTime() % 80L == 0L) {
				Iterator<PlayerEntity> entities = world.getNonSpectatingEntities(PlayerEntity.class, new Box(pos).expand(blockEntity.getLevel() * 8 + 8)).iterator();
				PlayerEntity playerEntity;
				while(entities.hasNext()) {
					playerEntity = entities.next();
					for (StatusEffectInstance effectInstance : this.effects) {
						StatusEffectInstance newInstance = new StatusEffectInstance(effectInstance);
						playerEntity.addStatusEffect(newInstance);
					}
				}
			}
			if (world instanceof ServerWorld serverWorld && world.getTime() % 160L == 0L) {
				if (!this.effects.isEmpty()) {
					this.effects.clear();
				}
				int j = (11 + blockEntity.getLevel() * 2) * 20;
				if (stack.contains(DataComponentTypes.POTION_CONTENTS)) {
					float strength = stack.get(Resonance.PEARL_STATS).stats().get(2);
					stack.get(DataComponentTypes.POTION_CONTENTS).forEachEffect(effect -> effects.add(new StatusEffectInstance(effect.getEffectType(), j, (int) Math.max(0, strength * effect.getAmplifier() / 3F), true, true)));
				} else {
					Optional<AlternatePearl> alternateOrb = serverWorld.getServer().getRegistryManager().get(Resonance.ALTERNATE_PEARLS_REGISTRY_KEY).stream().filter(alternatePearl -> alternatePearl.matches(stack)).findFirst();
					if (alternateOrb.isPresent()) {
						alternateOrb.get().effects().forEach(effect -> effects.add(new StatusEffectInstance(effect.getEffectType(), j, effect.getAmplifier(), true, true)));
					}
				}
			}
		} else {
			this.dropStack(this.getMainHandStack());
			this.remove(RemovalReason.DISCARDED);
		}
	}
}