package net.moss.resonance.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BrushItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.BuddingPearlIce;
import net.moss.resonance.block.entity.BuddingPearlIceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(BrushItem.class)
public class BrushItemMixin {
	@Inject(method = "usageTick", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci, @Local BlockHitResult blockHitResult) {
		int i = ((BrushItem)(Object) this).getMaxUseTime(stack, user) - remainingUseTicks + 1;
		if (i % 10 == 5) {
			BlockPos blockPos = blockHitResult.getBlockPos();
			BlockState blockState = world.getBlockState(blockPos);
			if (!(blockState.isOf(Resonance.BUDDING_PEARL_ICE) && blockState.get(BuddingPearlIce.AGE) == 6)) { return; }
			if (!world.isClient() && world.getBlockEntity(blockPos) instanceof BuddingPearlIceBlockEntity blockEntity && user instanceof ServerPlayerEntity player && blockEntity.brush(world.getTime(), player, blockHitResult.getSide())) {
				EquipmentSlot equipmentSlot = stack.equals(player.getEquippedStack(EquipmentSlot.OFFHAND)) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
				stack.damage(4, user, equipmentSlot);
			}
		}
	}
}