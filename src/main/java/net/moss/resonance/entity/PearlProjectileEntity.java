package net.moss.resonance.entity;

import org.joml.Vector3f;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.entity.PearlProjectileEntity;
import net.moss.resonance.particle.SmallRing;
import net.moss.resonance.util.PearlStatsComponent;

public class PearlProjectileEntity extends ThrownItemEntity {
	private static final TrackedData<Byte> HITS = DataTracker.registerData(PearlProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);
	private static final TrackedData<Byte> SLOT = DataTracker.registerData(PearlProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);
	private static final TrackedData<Byte> LOYALTY = DataTracker.registerData(PearlProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);
	private static final TrackedData<Boolean> NO_DROP = DataTracker.registerData(PearlProjectileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private boolean dealtDamage;

	public PearlProjectileEntity(EntityType<? extends PearlProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	public PearlProjectileEntity(World world, LivingEntity owner) {
		super(ModEntities.PEARL_PROJECTILE, owner, world);
	}

	public PearlProjectileEntity(World world, double x, double y, double z) {
		super(ModEntities.PEARL_PROJECTILE, x, y, z, world);
	}

	@Override
	public void setItem(ItemStack stack) {
		super.setItem(stack);
		this.dataTracker.set(LOYALTY, this.getLoyalty(this.getStack()));
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder.add(HITS, (byte) 0).add(SLOT, (byte) -1).add(LOYALTY, (byte) 0).add(NO_DROP, false));
	}

	private byte getLoyalty(ItemStack stack) {
		if (!(this.getWorld() instanceof ServerWorld serverWorld)) return 0;
		return (byte)MathHelper.clamp(EnchantmentHelper.getTridentReturnAcceleration(serverWorld, stack, this), 0, 127);
	}

	public int getLoyalty() {
		return this.getDataTracker().get(LOYALTY);
	}

	public void noDrop() {
		this.getDataTracker().set(NO_DROP, true);
	}

	public void drop() {
		if (!this.getDataTracker().get(NO_DROP)) {
			this.dropStack(this.getStack().copyAndEmpty());
		}
		this.discard();
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		this.dealtDamage = nbt.getBoolean("DealtDamage");
		this.dataTracker.set(HITS, nbt.getByte("Hits"));
		this.dataTracker.set(SLOT, nbt.getByte("Slot"));
		this.dataTracker.set(LOYALTY, this.getLoyalty(this.getStack()));
		this.dataTracker.set(NO_DROP, nbt.getBoolean("NoDrop"));
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("DealtDamage", this.dealtDamage);
		nbt.putByte("Hits", (byte) this.getHits());
		nbt.putByte("Slot", (byte) this.getSlot());
		nbt.putByte("Loyalty", (byte) this.getLoyalty());
		nbt.putBoolean("NoDrop", this.getDataTracker().get(NO_DROP));
	}

	public void setSlot(int slot) {
		this.getDataTracker().set(SLOT, (byte) slot);
	}

	public int getSlot() {
		return this.getDataTracker().get(SLOT);
	}

	public void setHits(int amount) {
		this.getDataTracker().set(HITS, (byte) amount);
	}

	public int getHits() {
		return this.getDataTracker().get(HITS);
	}

	@Override
	public void tick() {
		if (this.age % 20 == 0) {
			this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.NEUTRAL, 0.1f, 1f);
		}
		Entity entity = this.getOwner();
		int i = this.getLoyalty();
		if (i > 0 && this.dealtDamage && entity != null) {
			if (!this.isOwnerAlive()) {
				if (!this.getWorld().isClient()) {
					drop();
				}
			} else {
				Vec3d vec3d = entity.getEyePos().subtract(this.getPos());
				this.setPos(this.getX(), this.getY() + vec3d.y * 0.015 * (double)i, this.getZ());
				if (this.getWorld().isClient()) {
					this.lastRenderY = this.getY();
				}

				double d = 0.05 * (double)i;
				this.setVelocity(this.getVelocity().multiply(0.95).add(vec3d.normalize().multiply(d)));
			}
		} else if (!this.getWorld().isClient() && (this.dealtDamage || this.age > 12000)) {
			drop();
			return;
		}
		super.tick();
	}

	private boolean isOwnerAlive() {
		Entity entity = this.getOwner();
		return entity != null && entity.isAlive() && entity instanceof ServerPlayerEntity && !entity.isSpectator();
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		if (this.dealtDamage) { return; }
		super.onCollision(hitResult);
		this.setVelocity(this.getLoyalty() > 0 ? this.getVelocity().negate() : Vec3d.ZERO);
		this.dealtDamage = true;
		this.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, 2f, 1f + (this.getHits() / 5F));
		PotionContentsComponent potions = this.getStack().getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
		this.getWorld().addParticle(EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, potions.getColor()), this.getX(), this.getY() + 0.25F, this.getZ(), 0, 0, 0);
	}

	@Override
	public void onPlayerCollision(PlayerEntity player) {
		if (this.dealtDamage && this.isOwner(player) && this.tryInsert(getStack(), player)) {
			this.discard();
			return;
		}
		super.onPlayerCollision(player);
	}

	public boolean tryInsert(ItemStack stack, PlayerEntity player) {
		if (this.getDataTracker().get(NO_DROP)) { return true; }
		int preferredSlot = this.getSlot();
		if (preferredSlot == -1) {
			if (player.getOffHandStack().isEmpty()) {
				player.equipStack(EquipmentSlot.OFFHAND, stack.copy());
				stack.setCount(0);
				return true;
			}
		} else if (player.getInventory().getStack(preferredSlot).isEmpty() && player.getInventory().insertStack(preferredSlot, stack)) {
			return true;
		}
		return player.getInventory().insertStack(this.getStack());
	}

	@Override
	protected void onDeflected(Entity deflector, boolean fromAttack) {
		super.onDeflected(deflector, fromAttack);
		this.setHits(this.getHits() + 1);
		this.dealtDamage = false;
		((ServerWorld)this.getWorld()).spawnParticles(new SmallRing(new Vector3f(deflector.getPitch() * MathHelper.RADIANS_PER_DEGREE, deflector.getYaw() * MathHelper.RADIANS_PER_DEGREE, 0), 0.5F), this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 3);
		this.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, 2f, 1f + (this.getHits() / 5F));
		this.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 2f, 1f + (this.getHits() / 5F));
	}

	@Override
	protected void onEntityHit(EntityHitResult entityHitResult) {
		Entity entity2 = this.getOwner();
		Entity entity = entityHitResult.getEntity();
		DamageSource damageSource = this.getDamageSources().thrown(this, (Entity)(entity2 != null ? entity2 : this));

		super.onEntityHit(entityHitResult);
		ItemStack stack = this.getStack();

		entity.damage(damageSource, Math.min(1 + this.getHits(), 6) * (stack.isOf(Resonance.BIFURCATED_PEARL) ? 2 : 1));
		
		if (this.isOnFire() && entity.getType() != EntityType.ENDERMAN) {
			entity.setOnFireFor(5.0F);
		}

		if (entity instanceof LivingEntity livingEntity) {
			livingEntity.onAttacking(entity);
			if (this.getWorld() instanceof ServerWorld serverWorld) {


				double d = EnchantmentHelper.modifyKnockback(serverWorld, this.getStack(), livingEntity, damageSource, 0);
				double e = Math.max(0, 1 - livingEntity.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
				Vec3d vec3d = this.getVelocity().multiply(1, 0, 1).normalize().multiply(d * 0.5 * e);
				if (vec3d.lengthSquared() > 0) {
					livingEntity.addVelocity(vec3d.x, 0.1, vec3d.z);
				}


				EnchantmentHelper.onTargetDamaged(serverWorld, livingEntity, damageSource, stack);
			}

			if (stack.getDamage() != stack.getMaxDamage()) {
				PearlStatsComponent pearlStats = stack.getOrDefault(Resonance.PEARL_STATS, PearlStatsComponent.DEFAULT);
				stack.set(DataComponentTypes.DAMAGE, stack.getDamage() + Math.max(4 - this.getHits(), 2));
				this.setItem(stack);
				stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).forEachEffect(effect -> {
					StatusEffectInstance newEffect = new StatusEffectInstance(effect.getEffectType(), (int) (pearlStats.getDuration() * effect.getDuration() / 6F), (int) Math.max(0, pearlStats.getStrength() * effect.getAmplifier() / 3F));
					livingEntity.addStatusEffect(newEffect, entity2);
				});
			}
		}
	}

	@Override protected Item getDefaultItem() { return Resonance.PEARL; }
}