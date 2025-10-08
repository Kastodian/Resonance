package net.moss.resonance.entity;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.entity.PearlProjectileEntity;
import net.moss.resonance.util.PearlStatsComponent;

public class PearlProjectileEntity extends ThrownItemEntity {
	private static final TrackedData<Byte> HITS = DataTracker.registerData(PearlProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);

	public PearlProjectileEntity(EntityType<? extends PearlProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	public PearlProjectileEntity(World world, LivingEntity owner) {
		super(ModEntities.PEARL_PROJECTILE, owner, world);
	}

	public PearlProjectileEntity(World world, double x, double y, double z) {
		super(ModEntities.PEARL_PROJECTILE, x, y, z, world);
	}

	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder.add(HITS, (byte) 0));
	}

	public void setHits(int amount) {
		this.getDataTracker().set(HITS, (byte) amount);
	}

	public int getHits() {
		return this.getDataTracker().get(HITS);
	}

	@Override
	public void tick() {
		this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.NEUTRAL, 0.1f, 1f);
		if (!this.getWorld().isClient() && this.age > 12000) {
			this.dropStack(this.getStack().copyAndEmpty());
			this.discard();
			return;
		}
		super.tick();
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);
		if (!this.getWorld().isClient()) {
			this.dropStack(this.getStack().copyAndEmpty());
			this.discard();
		}
	}

	@Override
	protected void onDeflected(Entity deflector, boolean fromAttack) {
		super.onDeflected(deflector, fromAttack);
		this.setHits(this.getHits() + 1);
		if (this.getWorld() instanceof ServerWorld serverWorld) {
			serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.NEUTRAL, 2f, 1f + (this.getHits() / 5F));
		}
	}

	@Override
	protected void onEntityHit(EntityHitResult entityHitResult) {
		super.onEntityHit(entityHitResult);
		Entity entity = entityHitResult.getEntity();
		ItemStack stack = this.getStack();
		PearlStatsComponent pearlStats = stack.get(Resonance.PEARL_STATS);
		PotionContentsComponent potions = stack.get(DataComponentTypes.POTION_CONTENTS);
		if (pearlStats != null && potions != null && entity instanceof LivingEntity livingEntity && stack.getDamage() != stack.getMaxDamage()) {
			stack.set(DataComponentTypes.DAMAGE, stack.getDamage() + 8);
			this.setItem(stack);
			Entity source = this.getOwner();
			potions.forEachEffect(effect -> {
				StatusEffectInstance newEffect = new StatusEffectInstance(effect.getEffectType(), (int) (pearlStats.getDuration() * effect.getDuration() / 6F), (int) Math.max(0, pearlStats.getStrength() * effect.getAmplifier() / 3F));
				livingEntity.addStatusEffect(newEffect, source);
			});
		}
		entity.damage(this.getDamageSources().thrown(this, this.getOwner()), 1);
	}

	@Override protected Item getDefaultItem() { return Resonance.PEARL; }
}