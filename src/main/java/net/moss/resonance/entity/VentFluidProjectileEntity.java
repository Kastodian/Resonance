package net.moss.resonance.entity;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.particle.VentSmoke;
import net.moss.resonance.util.Utils;

public class VentFluidProjectileEntity extends ProjectileEntity {
	protected static final TrackedData<Vector3f> COLOR = DataTracker.registerData(VentFluidProjectileEntity.class, TrackedDataHandlerRegistry.VECTOR3F);

	public VentFluidProjectileEntity(EntityType<? extends VentFluidProjectileEntity> entityType, World world) {
		super((EntityType<? extends ProjectileEntity>) entityType, world);
	}

	public VentFluidProjectileEntity(World world, LivingEntity owner, double directionX, double directionY, double directionZ, Vector3f vec) {
		super(ModEntities.VENT_FLUID_PROJECTILE, world);
		this.setOwner(owner);
		this.dataTracker.set(COLOR, vec, true);
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);
		if (this.getWorld() instanceof ServerWorld serverWorld) {
			Utils.entitiesInRadius(serverWorld, this.getPos(), this, 2, 2, Entity::isAlive).forEach(entity -> {
				if (entity instanceof LivingEntity livingEntity) { livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 5 * 20)); }
			});
			serverWorld.spawnParticles(new VentSmoke(getColor(), 1.0F + random.nextFloat() * 2, true), this.getX(), this.getY(), this.getZ(), 200, 0.7, 0.7, 0.7, 0.05);
			if (serverWorld.getGameRules().getBoolean(Resonance.VENT_FLUID_BOTTLES_PLACES_WATER)) {
				if (getBlockStateAtPos().isReplaceable()) { serverWorld.setBlockState(getBlockPos(), Blocks.WATER.getDefaultState().with(FluidBlock.LEVEL, 5), Block.NOTIFY_ALL); }
				for (Direction direction : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
					if (serverWorld.getBlockState(getBlockPos().offset(direction)).isReplaceable()) { serverWorld.setBlockState(getBlockPos().offset(direction), Blocks.WATER.getDefaultState().with(FluidBlock.LEVEL, 2), Block.NOTIFY_ALL); }
				}
			}
			this.discard();
		}
	}

	@Override
	protected void onBlockHit(BlockHitResult blockHitResult) {
		BlockPos blockPos = blockHitResult.getBlockPos();
		World world = this.getWorld();
		BlockState upState = world.getBlockState(blockPos);
		if (upState.getBlock() instanceof Oxidizable oxidizable && world instanceof ServerWorld serverWorld) {
			oxidizable.tryDegrade(upState, serverWorld, blockPos, random).ifPresent(degraded -> world.setBlockState(blockPos, degraded));
		}
		super.onBlockHit(blockHitResult);
	}

	@Override
	public void tick() {
		World world = this.getWorld();
		HitResult hitResult;
		Entity entity = this.getOwner();
		if (!world.isClient() && (this.age > 12000 && entity != null && entity.isRemoved() || !world.isChunkLoaded(this.getChunkPos().x, this.getChunkPos().z))) {
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
		float g = this.isTouchingWater() ? 0.9f : 0.98f;
		this.setVelocity(this.getVelocity().multiply(g, this.isTouchingWater() ? g : 1, g));
		if (this.getVelocity().lengthSquared() < 0.001 && this.isTouchingWater()) { this.discard(); }
		if (this.getVelocity().y > -.2);
		this.setVelocity(this.getVelocity().add(0, this.isTouchingWater() ? -0.001 : -.025, 0));
		if (this.age > 2) {
			addParticle(world, random);
			addParticle(world, random);
			addParticle(world, random);
		}
		this.setPosition(d, e, f);
	}

	public void addParticle(World world, Random random) {
		world.addParticle(
				new VentSmoke(getColor(), 1.0F + random.nextFloat() * 2, true), true,
				this.getX() + (random.nextDouble() - 0.5) / 5.0, this.getY() + (random.nextDouble() - 0.5) / 5.0, this.getZ() + (random.nextDouble() - 0.5) / 5.0,
				0.04 * (random.nextDouble() - 0.5), 0.04 * (random.nextDouble() - 0.5), 0.04 * (random.nextDouble() - 0.5));
	}

	@Nullable public Vector3f getColor() { return this.dataTracker.get(COLOR); }
	@Override public boolean canHit() { return true; }
	@Override public boolean damage(DamageSource source, float amount) { return false; }
	@Override public boolean canUsePortals(boolean allowVehicles) { return true; }
	@Override protected void initDataTracker(Builder builder) { builder.add(COLOR, Vec3d.unpackRgb(16777215).toVector3f()); }

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		Vector3f colour = this.dataTracker.get(COLOR);
		nbt.putByte("Color", (byte) ColorHelper.Argb.getArgb((int)colour.x, (int)colour.y, (int)colour.z));
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (nbt.contains("Color", NbtElement.NUMBER_TYPE)) {
			this.dataTracker.set(COLOR, Vec3d.unpackRgb(nbt.getByte("Color")).toVector3f());
		}
	}
}