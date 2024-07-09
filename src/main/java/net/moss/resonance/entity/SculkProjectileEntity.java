package net.moss.resonance.entity;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import net.moss.resonance.util.Utils;
import net.moss.resonance.entity.SculkProjectileEntity;


public class SculkProjectileEntity extends ProjectileEntity {
    private final SculkSpreadManager spreadManager;

    public SculkProjectileEntity(EntityType<? extends SculkProjectileEntity> entityType, World world) {
        super((EntityType<? extends ProjectileEntity>) entityType, world);
        this.spreadManager = SculkSpreadManager.create();
    }

    public SculkProjectileEntity(World world, LivingEntity owner, double directionX, double directionY, double directionZ) {
        super(ModEntities.SCULK_PROJECTILE, world);
        this.spreadManager = SculkSpreadManager.create();
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (hitResult.getType() == HitResult.Type.ENTITY && this.isOwner(((EntityHitResult) hitResult).getEntity())) { return; }
        World world = this.getWorld();
        if (!world.isClient) { this.discard(); }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (entity instanceof LivingEntity livingEntity) { livingEntity.damage(this.getDamageSources().mobProjectile(this, null), 5); }
        blindSurroundingEntities();
    }

    public void blindSurroundingEntities() {
        if (this.getWorld() instanceof ServerWorld world) {
            List<Entity> list = Utils.entitiesInRadius(world, this.getPos(), this, 2, 2, Entity::isAlive);
            for (Entity entity : list) {
                if (entity instanceof LivingEntity livingEntity) { livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 10 * 20)); }
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        blindSurroundingEntities();
        BlockPos blockPos = blockHitResult.getBlockPos();
        World world = this.getWorld();
        if (world.getBlockState(blockPos).getBlock() == Blocks.BONE_BLOCK) {
            world.playSound(null, blockPos, SoundEvents.BLOCK_SCULK_CATALYST_BLOOM, SoundCategory.PLAYERS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
            world.playSound(null, blockPos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
            world.setBlockState(blockPos, Blocks.SCULK_CATALYST.getDefaultState(), Block.NOTIFY_LISTENERS);
            for (Direction direction : Direction.values()) {
                BlockPos blockPosOff = blockPos.offset(direction);
                if (world.getBlockState(blockPosOff).isOpaqueFullCube(world, blockPosOff)) continue;
                Direction.Axis axis = direction.getAxis();
                double e = axis == Direction.Axis.X ? 0.5 + 0.5625 * (double)direction.getOffsetX() : (double)world.random.nextFloat();
                double f = axis == Direction.Axis.Y ? 0.5 + 0.5625 * (double)direction.getOffsetY() : (double)world.random.nextFloat();
                double g = axis == Direction.Axis.Z ? 0.5 + 0.5625 * (double)direction.getOffsetZ() : (double)world.random.nextFloat();
                world.addParticle(ParticleTypes.SCULK_CHARGE_POP, blockPos.getX() + e, blockPos.getY() + f, blockPos.getZ() + g, 0, 0, 0);
            }
        }
        this.spreadManager.spread(blockPos, 25);
        this.spreadManager.tick(world, blockPos, world.getRandom(), true);
        this.spreadManager.tick(world, blockPos, world.getRandom(), true);
        this.spreadManager.tick(world, blockPos, world.getRandom(), true);
        super.onBlockHit(blockHitResult);
    }
    
    @Override
    public void tick() {
        this.spreadManager.spread(this.getBlockPos(), 25);
        World world = this.getWorld();
        this.spreadManager.tick(world, this.getBlockPos(), world.getRandom(), true);
        HitResult hitResult;
        Entity entity = this.getOwner();
        if (!world.isClient && (entity != null && entity.isRemoved() || !world.isChunkLoaded(this.getChunkPos().x, this.getChunkPos().z))) {
            this.discard();
            return;
        }
        super.tick();
        if ((hitResult = ProjectileUtil.getCollision(this, this::canHit)).getType() != HitResult.Type.MISS) { this.onCollision(hitResult); }
        this.checkBlockCollision();
        Vec3d vec3d = this.getVelocity();
        double d = this.getX() + vec3d.x;
        double e = this.getY() + vec3d.y;
        double f = this.getZ() + vec3d.z;
        ProjectileUtil.setRotationFromVelocity(this, 0.2f);
        float g = 0.95f;
        if (this.isTouchingWater()) {
            for (int i = 0; i < 4; ++i) { world.addParticle(ParticleTypes.BUBBLE, d - vec3d.x * 0.25, e - vec3d.y * 0.25, f - vec3d.z * 0.25, vec3d.x, vec3d.y, vec3d.z); }
            g = 0.8f;
        }
        this.setVelocity(this.getVelocity().x * g, this.getVelocity().y, this.getVelocity().z * g);
        if (this.getVelocity().y > -.25);
        this.setVelocity(this.getVelocity().add(0, -.025, 0));
        world.addParticle(ParticleTypes.SCULK_CHARGE_POP, d, e, f, 0.0, 0.0, 0.0);
        this.setPosition(d, e, f);
    }

    @Override public boolean canHit() { return true; }
    @Override public boolean damage(DamageSource source, float amount) { return false; }
    @Override protected void initDataTracker(Builder var1) { }
}