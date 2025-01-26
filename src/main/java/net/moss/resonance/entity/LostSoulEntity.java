package net.moss.resonance.entity;

import java.util.EnumSet;
import net.minecraft.entity.AnimationState;
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
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.Utils;

public class LostSoulEntity extends VexEntity {
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

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

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.age);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        return super.canTarget(target) && !Utils.isItemEquipped(target, Resonance.SPIRIT_SCARF, true);
    }

    @Override
    protected void onKilledBy(LivingEntity adversary) {
        this.getVehicle().damage(Utils.damageSource(this.getWorld(), DamageTypes.INDIRECT_MAGIC, adversary), 10);
        super.onKilledBy(adversary);
    }
    
    @Override
    public void tickMovement() {
        World world = this.getWorld();
        if (world.isClient()) {
            setupAnimationStates();
            world.addParticle(ParticleTypes.SOUL, this.getX(), this.getY() + 0.25, this.getZ(), random.nextFloat()/10, random.nextFloat()/10, random.nextFloat()/10);
            world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.25, this.getZ(), random.nextFloat()/10, random.nextFloat()/10, random.nextFloat()/10);
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
		            if (!this.target.isSilent()) {
			            // TODO : Sound Effects... this.mob.getWorld().syncWorldEvent(null, WorldEvents.SKELETON_CONVERTS_TO_STRAY, this.target.getBlockPos(), 0);
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