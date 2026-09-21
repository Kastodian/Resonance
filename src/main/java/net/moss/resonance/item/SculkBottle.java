package net.moss.resonance.item;

import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.Resonance;
import net.moss.resonance.entity.SculkProjectileEntity;

public class SculkBottle extends TintedBottle {
	public static final DispenserBehavior SCULK_BOTTLE_BEHAVIOR = new ItemDispenserBehavior(){
		private ItemStack replace(BlockPointer pointer, ItemStack oldStack, ItemStack newStack) {
			pointer.world().emitGameEvent(null, GameEvent.PROJECTILE_SHOOT, pointer.pos());
			return this.decrementStackWithRemainder(pointer, oldStack, newStack);
		}

		@Override
		public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
			BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
			ServerWorld world = pointer.world();
			world.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.BLOCK_SCULK_SPREAD, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
			world.playSound(null, blockPos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
			SculkProjectileEntity sculkProjectileEntity = new SculkProjectileEntity(world, null, 0, 0, 0);
			sculkProjectileEntity.setPosition(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
			blockPos = new BlockPos(0, 0, 0).offset(pointer.state().get(DispenserBlock.FACING));
			sculkProjectileEntity.setVelocity(blockPos.getX(), blockPos.getY(), blockPos.getZ());
			world.spawnEntity(sculkProjectileEntity);
			return replace(pointer, stack, new ItemStack(Resonance.TINTED_BOTTLE));
		}
	};

	public SculkBottle(Item.Settings settings) {
		super(settings);
		DispenserBlock.registerBehavior(this, SCULK_BOTTLE_BEHAVIOR);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		world.playSound(null, user.getX(), user.getEyeY(), user.getZ(), SoundEvents.BLOCK_SCULK_SPREAD, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
		if (!world.isClient()) {
			SculkProjectileEntity sculkProjectileEntity = new SculkProjectileEntity(world, user, 0, 0, 0);
			sculkProjectileEntity.setPosition(user.getEyePos());
			sculkProjectileEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1f, 1.0f);
			world.spawnEntity(sculkProjectileEntity);
		}
		return emptyBottle(world, user, hand, 20);
	}

	@Override public Block getBlock() { return Resonance.SCULK_BOTTLE_BLOCK; }
}