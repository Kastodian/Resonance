package net.moss.resonance.entity;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;

public class LostSoulEntity extends VexEntity {
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    public LostSoulEntity(EntityType<? extends VexEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 10;
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
    public void tickMovement() {
        World world = this.getWorld();
        if (world.isClient) {
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
                .add(EntityAttributes.GENERIC_ARMOR, 0.5f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0f);
    }
}