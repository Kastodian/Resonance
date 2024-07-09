package net.moss.resonance.item;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import net.moss.resonance.entity.WhistlingArrowEntity;

public class WhistlingArrow
extends ArrowItem {
    public static final DispenserBehavior WHISTLING_ARROW_BEHAVIOR = new ItemDispenserBehavior(){
        @Override
        public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            if (stack.getItem() instanceof WhistlingArrow item) {
                ServerWorld world = pointer.world();
                Direction direction = pointer.state().get(DispenserBlock.FACING);
                ProjectileItem.Settings settings = item.getProjectileSettings();
                Position position = settings.positionFunction().getDispensePosition(pointer, direction);
                ProjectileEntity projectileEntity = item.createEntity(world, position, stack, direction);
                item.initializeProjectile(projectileEntity, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ(), settings.power(), settings.uncertainty());
                world.spawnEntity(projectileEntity);
                stack.decrement(1);
            }
            return stack;
        }
    };
    
    public WhistlingArrow(Item.Settings settings) {
        super(settings);
        DispenserBlock.registerBehavior(this, WHISTLING_ARROW_BEHAVIOR);
    }

    @Override
    public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter, ItemStack shotFrom) {
        return new WhistlingArrowEntity(world, shooter, stack.copyWithCount(1), shotFrom);
    }
    
    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        WhistlingArrowEntity arrowEntity = new WhistlingArrowEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack.copyWithCount(1), (ItemStack)null);
        arrowEntity.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
        return arrowEntity;
    }
}