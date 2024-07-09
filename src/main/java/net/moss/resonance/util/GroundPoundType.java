package net.moss.resonance.util;

import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.interfaces.PlayerEntityInterface;

public class GroundPoundType {
    private final EntityAttributeModifier gravity;
    private final float baseDamage;
    private final float baseRange;
    private final float baseKnockback;
    private final float damageMultiplier;
    private final float rangeMultiplier;
    private final float knockbackMultiplier;
    private final float damageCap;
    private final float rangeCap;
    private final float knockbackCap;

    public GroundPoundType(double gravity, float baseDamage, float baseRange, float baseKnockback, float damageMultiplier, float rangeMultiplier, float knockbackMultiplier, float damageCap, float rangeCap, float knockbackCap) {
        this.gravity = new EntityAttributeModifier(Identifier.of(Resonance.MOD_ID, "generic_ground_pound"), gravity, Operation.ADD_VALUE);
        this.baseDamage = baseDamage;
        this.baseRange = baseRange;
        this.baseKnockback = baseKnockback;
        this.damageMultiplier = damageMultiplier;
        this.rangeMultiplier = rangeMultiplier;
        this.knockbackMultiplier = knockbackMultiplier;
        this.damageCap = damageCap;
        this.rangeCap = rangeCap;
        this.knockbackCap = knockbackCap;
    }

    public GroundPoundType(double gravity) {
        this(gravity, 0f, 1f, 0.1f, 1f, 0.3f, 0.1f, 20f, 4f, 0.7f);
    }

    public void start(ServerWorld world, PlayerEntity user) {
        user.getAttributeInstance(EntityAttributes.GENERIC_GRAVITY).removeModifier(gravity.id());
        user.getAttributeInstance(EntityAttributes.GENERIC_GRAVITY).addTemporaryModifier(gravity);
        PlayerEntityInterface player = (PlayerEntityInterface) user;
        player.cancelGroundPound(user);
        player.startGroundPound(world, user, this);
    }

    public void tick(ServerWorld world, PlayerEntity user) {}
    
    public void end(ServerWorld world, PlayerEntity user, @Nullable Entity target) {
        user.getAttributeInstance(EntityAttributes.GENERIC_GRAVITY).removeModifier(gravity.id());
        float fallDistance = user.fallDistance + 2;
        float damageCalc = calculateDamage(fallDistance);
        float rangeCalc = calculateRange(fallDistance);
        float knockbackCalc = calculateKnockback(fallDistance);
        world.getEntitiesByClass(LivingEntity.class, user.getBoundingBox().expand(rangeCalc), Entity::isAttackable).forEach(entity -> {
            if (entity == user) { return; }
            indirectHitEntity(world, user, entity, damageCalc, rangeCalc, knockbackCalc, fallDistance);
        });
        if (target != null) { directHitEntity(world, user, user, damageCalc, rangeCalc, knockbackCalc, fallDistance); }
        endEffects(world, user, damageCalc, rangeCalc, knockbackCalc, fallDistance);
    }

    public void cancel(PlayerEntity user) {
        EntityAttributeInstance entityAttributeInstance = user.getAttributeInstance(EntityAttributes.GENERIC_GRAVITY);
        entityAttributeInstance.removeModifier(gravity.id());
    }

    public float calculateDamage(float fallDistance) { return Math.min(baseDamage + damageMultiplier * fallDistance, damageCap); }
    public float calculateRange(float fallDistance) { return Math.min(baseRange + rangeMultiplier * fallDistance, rangeCap); }
    public float calculateKnockback(float fallDistance) { return Math.min(baseKnockback + knockbackMultiplier * fallDistance, knockbackCap); }
    public void directHitEntity(ServerWorld world, PlayerEntity user, LivingEntity target, float damage, float range, float knockback, float fallDistance) { user.fallDistance = 0; }

    public void indirectHitEntity(ServerWorld world, PlayerEntity user, LivingEntity target, float damage, float range, float knockback, float fallDistance) {
        Vec3d vec3d = target.getPos().subtract(user.getPos());
        double k = (range - vec3d.length()) * knockback * (1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
        float d = (float) (damage - vec3d.length());
        if (d >= 0.0f) { target.damage(Utils.damageSource(world, Resonance.GROUND_POUND_DAMAGE, user), d); }
        if (k > 0.0) {
            Vec3d vec3d2 = vec3d.normalize().multiply(k);
            target.addVelocity(vec3d2.x, 0.7f, vec3d2.z);
            if (target instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)target;
                serverPlayerEntity.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayerEntity));
            }
        }
    }

    public void endEffects(ServerWorld world, PlayerEntity user, float damage, float range, float knockback, float fallDistance) {
        user.fallDistance = fallDistance / 4;
        int count = Math.min((int) Math.ceil(fallDistance) / 6, 15);
        Vec3d vec3d = user.getPos();
        world.spawnParticles(ParticleTypes.GUST_EMITTER_SMALL, vec3d.x, vec3d.y, vec3d.z, count, 0.5, 0, 0.5, fallDistance);
        user.getWorld().playSound(null, user.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS);
    }
}