package net.moss.resonance.mixin;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractFurnaceBlock.class)
public class AbstractFurnaceBlockMixin {
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
		if (projectile.getType() == EntityType.SMALL_FIREBALL && world.getBlockEntity(hit.getBlockPos()) instanceof AbstractFurnaceBlockEntity blockEntity) {
			AbstractFurnaceBlockEntityAccessor accessor = (AbstractFurnaceBlockEntityAccessor) blockEntity;
			accessor.setBurnTime(Math.max(accessor.getBurnTime(), 2400));
         	world.setBlockState(hit.getBlockPos(), state.with(AbstractFurnaceBlock.LIT, true), Block.NOTIFY_ALL);
		}
    }
}