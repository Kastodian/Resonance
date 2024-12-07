package net.moss.resonance.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.moss.resonance.util.EnchantUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponItemMixin {
	@Inject(at = @At("HEAD"), method = "shootAll", cancellable = true)
    private void shootAll(ServerWorld world, LivingEntity shooter, Hand hand, ItemStack stack, List<ItemStack> projectiles, float speed, float divergence, boolean critical, @Nullable LivingEntity target, CallbackInfo info) {
        ItemStack projectile = projectiles.get(0);
        if (projectile.getItem() instanceof ProjectileItem interface1) {
            ProjectileEntity projectileEntity = interface1.createEntity(world, shooter.getPos(), stack, Direction.NORTH);
            if (projectileEntity instanceof PersistentProjectileEntity persistentProjectileEntity) {
                EnchantUtils.recoilCheck(shooter, projectileEntity.getDamageSources().arrow(persistentProjectileEntity, target), stack, 0.4f, 1f, true);
            }
        } else { EnchantUtils.recoilCheck(shooter, null, stack, 0.5f, 1.2f, true); }
    }
}
