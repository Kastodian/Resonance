package net.moss.resonance.item;

import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.Resonance;

public class FireballBottle extends TintedBottle {
    public static final DispenserBehavior FIREBALL_BOTTLE_BEHAVIOR = new FallibleItemDispenserBehavior(){
        private ItemStack replace(BlockPointer pointer, ItemStack oldStack, ItemStack newStack) {
            pointer.world().emitGameEvent(null, GameEvent.PROJECTILE_SHOOT, pointer.pos());
            return this.decrementStackWithRemainder(pointer, oldStack, newStack);
        }

        @Override
        public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
            ServerWorld world = pointer.world();
            world.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
            world.playSound(null, blockPos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
            BlockPos temp = new BlockPos(0, 0, 0).offset(pointer.state().get(DispenserBlock.FACING)).multiply(4);
            Vec3d vec3d = new Vec3d(temp.getX(), temp.getY(), temp.getZ());
            ArmorStandEntity armorStandEntity = new ArmorStandEntity(world, vec3d.getX(), vec3d.getY(), vec3d.getZ());
            armorStandEntity.setCustomName(Text.translatable("block.minecraft.dispenser"));
            FireballEntity fireballEntity = new FireballEntity(world, armorStandEntity, vec3d, 2);
            fireballEntity.setPosition(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
            blockPos = new BlockPos(0, 0, 0).offset(pointer.state().get(DispenserBlock.FACING));
            fireballEntity.setVelocity(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            world.spawnEntity(fireballEntity);
            return replace(pointer, stack, new ItemStack(Resonance.TINTED_BOTTLE));
        }
    };

    public FireballBottle(Item.Settings settings) {
        super(settings);
        DispenserBlock.registerBehavior(this, FIREBALL_BOTTLE_BEHAVIOR);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getEyeY(), user.getZ(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
        world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
        if (!world.isClient) {
            Vec3d vec3d = user.getRotationVec(1.0f).multiply(4);
            FireballEntity fireballEntity = new FireballEntity(world, user, vec3d, 1);
            fireballEntity.setPosition(user.getX(), user.getEyeY(), user.getZ());
            fireballEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 3f, 1.0f);
            world.spawnEntity(fireballEntity);
        }
        user.getItemCooldownManager().set(this, 4);
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.getAbilities().creativeMode) {
            ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, user, Resonance.TINTED_BOTTLE.getDefaultStack());
            return TypedActionResult.success(itemStack2, world.isClient());
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override public Block getBlock() { return Resonance.FIREBALL_BOTTLE_BLOCK; }
}
