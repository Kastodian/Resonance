package net.moss.resonance.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.Resonance;

public class LightningBottle extends TintedBottle {
	public static final DispenserBehavior LIGHTNING_BOTTLE_BEHAVIOR = new ItemDispenserBehavior(){
		private ItemStack replace(BlockPointer pointer, ItemStack oldStack, ItemStack newStack) {
			pointer.world().emitGameEvent(null, GameEvent.LIGHTNING_STRIKE, pointer.pos());
			return this.decrementStackWithRemainder(pointer, oldStack, newStack);
		}

		@Override
		public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
			BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
			ServerWorld world = pointer.world();
			world.playSound(null, blockPos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
			LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(world);
			lightningEntity.setPosition(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
			world.spawnEntity(lightningEntity);
			return replace(pointer, stack, new ItemStack(Resonance.TINTED_BOTTLE));
		}
	};

	public LightningBottle(Item.Settings settings) {
		super(settings);
		DispenserBlock.registerBehavior(this, LIGHTNING_BOTTLE_BEHAVIOR);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		super.finishUsing(stack, world, user);
		if (user instanceof ServerPlayerEntity serverPlayerEntity) {
			Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
			user.emitGameEvent(GameEvent.CONTAINER_OPEN);
			if (world instanceof ServerWorld world2) {
				world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
				LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(world2);
				lightningEntity.setPosition(user.getPos());
				world2.spawnEntity(lightningEntity);
				serverPlayerEntity.damage(user.getDamageSources().lightningBolt(), 10);
			}
			serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
			if (!serverPlayerEntity.getAbilities().creativeMode) {
				ItemStack itemStack2 = ItemUsage.exchangeStack(stack, serverPlayerEntity, Resonance.TINTED_BOTTLE.getDefaultStack());
				return itemStack2;
			}
		}
		return stack;
	}

	@Override
	public int getMaxUseTime(ItemStack stack, LivingEntity user) {
		return 20;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.DRINK;
	}

	@Override
	public SoundEvent getDrinkSound() {
		return SoundEvents.ENTITY_GENERIC_DRINK;
	}

	@Override
	public SoundEvent getEatSound() {
		return SoundEvents.ENTITY_GENERIC_DRINK;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		return ItemUsage.consumeHeldItem(world, user, hand);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		PlayerEntity playerEntity = context.getPlayer();
		if (!playerEntity.isSneaking()) {
			World world = context.getWorld();
			ItemStack itemStack = context.getStack();
			if (world instanceof ServerWorld world2) {
				world.playSound(null, playerEntity.getBlockPos(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
				LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(world2);
				lightningEntity.setPosition(context.getHitPos());
				world2.spawnEntity(lightningEntity);
			}
			playerEntity.getItemCooldownManager().set(this, 20);
			playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
			if (!playerEntity.getAbilities().creativeMode) {
				ItemUsage.exchangeStack(itemStack, playerEntity, Resonance.TINTED_BOTTLE.getDefaultStack());
			}
			return ActionResult.success(world.isClient);
		}
		return super.useOnBlock(context);
	}

	@Override public Block getBlock() { return Resonance.LIGHTNING_BOTTLE_BLOCK; }
}