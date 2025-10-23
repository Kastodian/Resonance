package net.moss.resonance.item;

import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.Resonance;

public class ShulkerBottle extends TintedBottle {
	public static final DispenserBehavior SHULKER_BOTTLE_BEHAVIOR = new ItemDispenserBehavior(){
		private ItemStack replace(BlockPointer pointer, ItemStack oldStack, ItemStack newStack) {
			pointer.world().emitGameEvent(null, GameEvent.PROJECTILE_SHOOT, pointer.pos());
			return this.decrementStackWithRemainder(pointer, oldStack, newStack);
		}

		@Override
		public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
			BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
			ServerWorld world = pointer.world();
			world.playSound(null, blockPos, SoundEvents.ENTITY_SHULKER_SHOOT, SoundCategory.BLOCKS, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
			world.playSound(null, blockPos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
			BlockPos vec3d = new BlockPos(0, 0, 0).offset(pointer.state().get(DispenserBlock.FACING)).multiply(4);
			ArmorStandEntity armorStandEntity = new ArmorStandEntity(world, vec3d.getX(), vec3d.getY(), vec3d.getZ());
			armorStandEntity.setCustomName(Text.translatable("block.minecraft.dispenser"));
			ShulkerBulletEntity shulkerBulletEntity = new ShulkerBulletEntity(world, armorStandEntity, world.getClosestEntity(LivingEntity.class, TargetPredicate.createAttackable(), armorStandEntity, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, new Box(blockPos).expand(32)), pointer.state().get(DispenserBlock.FACING).getAxis());
			shulkerBulletEntity.setPosition(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
			world.spawnEntity(shulkerBulletEntity);
			return replace(pointer, stack, new ItemStack(Resonance.TINTED_BOTTLE));
		}
	};

	public ShulkerBottle(Item.Settings settings) {
		super(settings);
		DispenserBlock.registerBehavior(this, SHULKER_BOTTLE_BEHAVIOR);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		world.playSound(null, user.getX(), user.getEyeY(), user.getZ(), SoundEvents.ENTITY_SHULKER_SHOOT, SoundCategory.PLAYERS, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
		world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
		if (!world.isClient) {
			ShulkerBulletEntity shulkerBulletEntity = new ShulkerBulletEntity(world, user, world.getClosestEntity(LivingEntity.class, TargetPredicate.createAttackable(), user, user.getX() + 0.5, user.getY() + 0.5, user.getZ() + 0.5, new Box(user.getBlockPos()).expand(32)), Axis.Y);
			shulkerBulletEntity.setPosition(user.getEyePos());
			world.spawnEntity(shulkerBulletEntity);
		}
		user.getItemCooldownManager().set(this, 4);
		user.incrementStat(Stats.USED.getOrCreateStat(this));
		if (!user.getAbilities().creativeMode) {
			ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, user, Resonance.TINTED_BOTTLE.getDefaultStack());
			return TypedActionResult.success(itemStack2, world.isClient());
		}
		return TypedActionResult.success(itemStack, world.isClient());
	}

	@Override public Block getBlock() { return Resonance.SHULKER_BOTTLE_BLOCK; }
}