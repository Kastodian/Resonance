package net.moss.resonance.entity;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.ResonanceEvents;

public class WhistlingArrowEntity extends PersistentProjectileEntity {
	public WhistlingArrowEntity(EntityType<? extends WhistlingArrowEntity> entityType, World world) {
		super(entityType, world);
	}

	public WhistlingArrowEntity(World world, LivingEntity owner, ItemStack stack, @Nullable ItemStack shotFrom) {
		super(ModEntities.WHISTLING_ARROW, owner, world, stack, shotFrom);
	}

	public WhistlingArrowEntity(World world, double x, double y, double z, ItemStack stack, @Nullable ItemStack shotFrom) {
		super(ModEntities.WHISTLING_ARROW, x, y, z, world, stack, shotFrom);
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.inGround) {
			this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.NEUTRAL, 0.2f, 1f);
		}
	}

	private void hit() {
		if (this.getWorld() instanceof ServerWorld serverWorld) {
			serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.NEUTRAL, 1f, 1f);
			ResonanceEvents.REVERBERATE.invoker().onResonate(serverWorld, this.getPos().add(this.getVelocity()), new Vector3f(this.prevPitch * MathHelper.RADIANS_PER_DEGREE, this.prevYaw * MathHelper.RADIANS_PER_DEGREE + MathHelper.PI, 0f), this, this.getOwner(), (float) (this.getVelocity().length() / 1.5F), false);
		}
	}

	@Override
	protected void onDeflected(Entity deflector, boolean fromAttack) {
		super.onDeflected(deflector, fromAttack);
		hit();
	}

	@Override
	protected void onBlockHit(BlockHitResult blockHitResult) {
		super.onBlockHit(blockHitResult);
		hit();
	}

	@Override
	protected void onEntityHit(EntityHitResult entityHitResult) {
		super.onEntityHit(entityHitResult);
		hit();
	}

	@Override
	protected ItemStack getDefaultItemStack() {
		return Resonance.WHISTLING_ARROW.getDefaultStack();
	}
}