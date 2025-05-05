package net.moss.resonance.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.moss.resonance.util.EnchantUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentItem.class)
public class TridentItemMixin {
	@Shadow private static boolean isAboutToBreak(ItemStack stack) { return true; };

    @Inject(method = "onStoppedUsing", at = @At("TAIL"))
    public void usageTick(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (user instanceof PlayerEntity playerEntity) {
            if (((TridentItem) (Object) this).getMaxUseTime(stack, user) - remainingUseTicks < 10 && EnchantmentHelper.getTridentSpinAttackStrength(stack, playerEntity) > 0.0f && TridentItemMixin.isAboutToBreak(stack)) {
                return;
            } else if (stack.getItem() instanceof ProjectileItem interface1) {
                ProjectileEntity projectileEntity = interface1.createEntity(world, user.getPos(), stack, Direction.NORTH);
                EnchantUtils.recoilCheck(user, projectileEntity.getDamageSources().trident(projectileEntity, playerEntity), stack, 0.6f, 1.4f, true);
            } else {
                EnchantUtils.recoilCheck(user, null, stack, 0.6f, 1.4f, true);
            }
        }
    }
}