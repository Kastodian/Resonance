package net.moss.resonance.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import org.joml.Math;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
	@Inject(method = "Lnet/minecraft/entity/ItemEntity;damage", at = @At("HEAD"), cancellable = true)
	private void damageProtection(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		ItemEntity itemEntity = (ItemEntity)(Object) this;
		World world = itemEntity.getWorld();
		if (!world.isClient && !itemEntity.getStack().isEmpty() && itemEntity.getStack().isIn(Resonance.UNIQUE_ITEMS_TAG)) {
			if (!(itemEntity.getWorld().getGameRules().getBoolean(Resonance.CACTI_DESTROY_UNIQUE_ITEMS_GAMERULE) && source.isOf(DamageTypes.CACTUS))) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
	private void tick(CallbackInfo callBackInfo) {
		ItemEntity itemEntity = (ItemEntity)(Object) this;
		World world = itemEntity.getWorld();
		if ((int) itemEntity.getY() <= world.getBottomY() + 2 && itemEntity.getStack().isIn(Resonance.UNIQUE_ITEMS_TAG)) {
			itemEntity.setVelocity(itemEntity.getVelocity().x, 0, itemEntity.getVelocity().z);
			itemEntity.setPos(itemEntity.getX(), Math.clamp(world.getBottomY() + 2, world.getHeight(), itemEntity.getY()), itemEntity.getZ());
		}
		if (!world.isClient && itemEntity.getStack().isIn(Resonance.UNIQUE_ITEMS_TAG) && itemEntity.getItemAge() != Short.MIN_VALUE && !itemEntity.cannotPickup()) {
			itemEntity.setNeverDespawn();
		}
	}

	@Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;discard()V", shift = At.Shift.BEFORE), cancellable = true)
	private void discard(CallbackInfo callBackInfo) {
		ItemEntity itemEntity = (ItemEntity)(Object) this;
		itemEntity.getStack().onItemEntityDestroyed(itemEntity);
	}
}