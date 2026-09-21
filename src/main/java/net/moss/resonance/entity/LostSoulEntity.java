package net.moss.resonance.entity;

import java.util.EnumSet;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.Utils;

public class LostSoulEntity extends VexEntity {
	public LostSoulEntity(EntityType<? extends VexEntity> entityType, World world) {
		super(entityType, world);
		this.experiencePoints = 20;
	}

	@Override
	protected void initGoals() {
		super.initGoals();
		this.goalSelector.add(3, new RunToHostGoal(this));
		this.goalSelector.add(3, new HostAttackGoal(this, 1.4, false));
	}

	@Override
	public boolean hurtByWater() {
		return true;
	}

	@Override
	public boolean canTarget(LivingEntity target) {
		return super.canTarget(target) && !Utils.isItemEquipped(target, Resonance.SPIRIT_SCARF, true);
	}

	@Override
	protected void onKilledBy(LivingEntity adversary) {
		if (this.getVehicle() != null) {
			this.getVehicle().damage(Utils.damageSource(this.getWorld(), DamageTypes.INDIRECT_MAGIC, adversary), 10);
		}
		super.onKilledBy(adversary);
	}

	@Override
	public void tickMovement() {
		World world = this.getWorld();
		if (world.isClient()) {
			world.addParticle(ParticleTypes.SOUL, this.getX(), this.getEyeY(), this.getZ(), random.nextFloat()/10, random.nextFloat()/10, random.nextFloat()/10);
			world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getEyeY(), this.getZ(), random.nextFloat()/10, random.nextFloat()/10, random.nextFloat()/10);
		}
		super.tickMovement();
	}

	public static DefaultAttributeContainer.Builder createLostSoulAttributes() {
		return VexEntity.createVexAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 4f)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1f)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0f);
	}

	private class RunToHostGoal extends Goal {
		protected final LostSoulEntity mob;
		protected LivingEntity target;
		protected int updateCountdownTicks;

		public RunToHostGoal(final LostSoulEntity entity) {
			this.mob = entity;
			this.setControls(EnumSet.of(Control.MOVE));
		}

		@Override
		public boolean canStart() {
			if (this.mob.getHealth() < this.mob.getMaxHealth() && !this.mob.hasVehicle()) {
				float range = 12f;
				Box box = new Box(this.mob.getX() - range, this.mob.getY() - range, this.mob.getZ() - range, this.mob.getX() + range, this.mob.getY() + range, this.mob.getZ() + range);
				this.target = this.mob.getWorld().getClosestEntity(LivingEntity.class, TargetPredicate.createAttackable().setPredicate(i -> (i.getType().isIn(EntityTypeTags.UNDEAD) && !i.hasPassengers())), this.mob.getOwner(), this.mob.getX(), this.mob.getY(), this.mob.getZ(), box);
				if (this.target != null) { return true; }
			}
			return false;
		}

		@Override
		public boolean shouldContinue() {
			return !this.target.hasPassengers();
		}

		@Override
		public void tick() {
			if (this.target != null || (!this.target.hasPassengers() && this.mob.getBoundingBox().intersects(this.target.getBoundingBox()))) {
				if (this.target instanceof WitherSkeletonEntity) {
					this.target.heal(this.mob.getHealth());
					this.mob.discard();
				} else if (this.target.getType().isIn(EntityTypeTags.SKELETONS) && !(this.target instanceof WitherSkeletonEntity) && this.target instanceof MobEntity mobEntity) {
					this.target = mobEntity.convertTo(EntityType.WITHER_SKELETON, true);
					((ServerWorld)this.mob.getWorld()).spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, this.target.getX(), this.target.getEyeY(), this.target.getZ(), 20, 0, 0, 0, 0.1);
					((ServerWorld)this.mob.getWorld()).spawnParticles(ParticleTypes.SMOKE, this.target.getX(), this.target.getEyeY(), this.target.getZ(), 40, 0, 0, 0, 0.3);
					if (!this.target.isSilent()) {
						this.mob.getWorld().playSound(null, getBlockPos(), Resonance.SKELETON_CONVERTS_TO_WITHER_SKELETON_SOUND.value(), SoundCategory.HOSTILE, 3.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
					}
				} else {
					this.mob.startRiding(this.target, true);
				}
			} else if (--this.updateCountdownTicks <= 0) {
				this.updateCountdownTicks = this.getTickCount(10);
				this.mob.addVelocity(this.target.getEyePos().subtract(this.mob.getPos()).multiply(0.2));
			}
		}
	}

	private class HostAttackGoal extends MeleeAttackGoal {
		public HostAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
			super(mob, speed, pauseWhenMobIdle);
		}

		@Override
		public boolean canStart() {
			return super.canStart() && this.mob.hasVehicle();
		}
	}
}