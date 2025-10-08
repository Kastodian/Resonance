package net.moss.resonance.mixin;

import net.minecraft.block.AmethystBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.moss.resonance.ResonanceEvents;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AmethystBlock.class)
public class AmethystBlockMixin {
	@Inject(at = @At("HEAD"), method = "onProjectileHit", cancellable = true)
	private void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile, CallbackInfo info) {
		world.addBlockBreakParticles(hit.getBlockPos(), state);
		ResonanceEvents.REVERBERATE.invoker().onResonate(world, projectile.getPos(), new Vector3f(projectile.prevPitch * MathHelper.DEGREES_PER_RADIAN, projectile.prevYaw * MathHelper.DEGREES_PER_RADIAN, 0f), null, projectile.getOwner(), 1, false);
	}

	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
		if (world.isClient) { return; }
		if (world.isReceivingRedstonePower(pos)) {
			world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 2f, 0.5f + world.random.nextFloat() * 1.2f);
		}
	}
}