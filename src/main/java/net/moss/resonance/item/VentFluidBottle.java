package net.moss.resonance.item;

import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.Resonance;
import net.moss.resonance.entity.VentFluidProjectileEntity;

public class VentFluidBottle extends TintedBottle {
	public static final DispenserBehavior VENT_FLUID_BOTTLE_BEHAVIOR = new ItemDispenserBehavior(){
		private ItemStack replace(BlockPointer pointer, ItemStack oldStack, ItemStack newStack) {
			pointer.world().emitGameEvent(null, GameEvent.FLUID_PLACE, pointer.pos());
			return this.decrementStackWithRemainder(pointer, oldStack, newStack);
		}

		@Override
		public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
			BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
			ServerWorld world = pointer.world();
			world.playSound(null, blockPos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
			VentFluidProjectileEntity ventFluidProjectileEntity = new VentFluidProjectileEntity(world, null, 0, 0, 0, Vec3d.unpackRgb(stack.contains(DataComponentTypes.DYED_COLOR) ? stack.get(DataComponentTypes.DYED_COLOR).rgb() : 16777215).toVector3f());
			ventFluidProjectileEntity.setPosition(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
			blockPos = new BlockPos(0, 0, 0).offset(pointer.state().get(DispenserBlock.FACING));
			ventFluidProjectileEntity.setVelocity(blockPos.getX(), blockPos.getY(), blockPos.getZ());
			world.spawnEntity(ventFluidProjectileEntity);
			return replace(pointer, stack, new ItemStack(Resonance.TINTED_BOTTLE));
		}
	};

	public VentFluidBottle(Item.Settings settings) {
		super(settings);
		DispenserBlock.registerBehavior(this, VENT_FLUID_BOTTLE_BEHAVIOR);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);

		if (!world.isClient()) {
			VentFluidProjectileEntity ventFluidProjectileEntity = new VentFluidProjectileEntity(world, user, 0, 0, 0, Vec3d.unpackRgb(itemStack.contains(DataComponentTypes.DYED_COLOR) ? itemStack.get(DataComponentTypes.DYED_COLOR).rgb() : 16777215).toVector3f());
			ventFluidProjectileEntity.setPosition(user.getEyePos());
			ventFluidProjectileEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 0.8f, 1.0f);
			world.spawnEntity(ventFluidProjectileEntity);
		}
		user.getItemCooldownManager().set(this, 4);
		user.incrementStat(Stats.USED.getOrCreateStat(this));
		if (!user.getAbilities().creativeMode) {
			ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, user, Resonance.TINTED_BOTTLE.getDefaultStack());
			return TypedActionResult.success(itemStack2, world.isClient());
		}
		return TypedActionResult.success(itemStack, world.isClient());
	}

	@Override public Block getBlock() { return Resonance.VENT_FLUID_BOTTLE_BLOCK; }
}