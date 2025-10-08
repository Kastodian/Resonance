package net.moss.resonance.item;

import java.util.List;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.entity.PearlProjectileEntity;
import net.moss.resonance.util.PearlStatsComponent;

// TODO: replace damage component with something unrepairable
public class Pearl extends Item implements ProjectileItem {
	public Pearl(Item.Settings settings) {
		super(settings.maxCount(1).rarity(Rarity.UNCOMMON).component(DataComponentTypes.MAX_DAMAGE, 16));
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ModelPredicateProviderRegistry.register(Identifier.of("shine"), (itemStack, clientWorld, livingEntity, i) -> {
				return itemStack.contains(Resonance.PEARL_STATS) && itemStack.get(Resonance.PEARL_STATS).shiny() ? 1.0F : 0.0F;
			});
		}
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
		PearlStatsComponent pearlStats = stack.get(Resonance.PEARL_STATS);
		PotionContentsComponent potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
		float duration = 3;
		if (pearlStats != null) {
			duration = pearlStats.getDuration();
			pearlStats.appendTooltip(context, tooltip::add, type);
		}
		if (potionContents != null) {
			potionContents.buildTooltip(tooltip::add, duration / 3F, context.getUpdateTickRate());
		}
		super.appendTooltip(stack, context, tooltip, type);
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return stack.contains(DataComponentTypes.POTION_CONTENTS);
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		return stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getColor();
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F) );
		if (!world.isClient()) {
			PearlProjectileEntity snowballEntity = new PearlProjectileEntity(world, user);
			snowballEntity.setItem(itemStack);
			snowballEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 0.75F, 1.0F);
			world.spawnEntity(snowballEntity);
		}

		user.incrementStat(Stats.USED.getOrCreateStat(this));
		itemStack.decrementUnlessCreative(1, user);
		return TypedActionResult.success(itemStack, world.isClient());
	}

	@Override
	public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
		PearlProjectileEntity snowballEntity = new PearlProjectileEntity(world, pos.getX(), pos.getY(), pos.getZ());
		snowballEntity.setItem(stack);
		return snowballEntity;
	}
}