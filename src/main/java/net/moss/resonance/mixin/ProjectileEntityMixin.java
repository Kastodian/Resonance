package net.moss.resonance.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.block.BlockState;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;

@Mixin(PersistentProjectileEntity.class)
public class ProjectileEntityMixin {
	@Shadow protected void onBlockHitEnchantmentEffects(ServerWorld world, BlockHitResult blockHitResult, ItemStack weaponStack) { return; };

	@Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
	public void onBlockHit(BlockHitResult blockHitResult, CallbackInfo ci) {
		PersistentProjectileEntity projectileEntity = (PersistentProjectileEntity) (Object) this;
		World world = projectileEntity.getWorld();
		BlockState blockState = world.getBlockState(blockHitResult.getBlockPos());
		if (!blockState.isIn(Resonance.REFLECTS_PROJECTILES_TAG) || projectileEntity.getVelocity().length() < 0.7) { return; }
		projectileEntity.setCritical(true);
		blockState.onProjectileHit(world, blockState, blockHitResult, projectileEntity);
		Direction direction = blockHitResult.getSide();
		Vec3i vec = (direction.getDirection() == AxisDirection.POSITIVE ? direction.getOpposite().getVector() : direction.getVector());
		projectileEntity.setVelocity(projectileEntity.getVelocity().multiply(vec.getX() == 0 ? 1 : vec.getX() * 0.25, vec.getY() == 0 ? 1 : vec.getY() * 0.25, vec.getZ() == 0 ? 1 : vec.getZ() * 0.25));
		ItemStack itemStack = projectileEntity.getWeaponStack();
		if (world instanceof ServerWorld serverWorld && itemStack != null) { this.onBlockHitEnchantmentEffects(serverWorld, blockHitResult, itemStack); }
		ci.cancel();
	}
}
