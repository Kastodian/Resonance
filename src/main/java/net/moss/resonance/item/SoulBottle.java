package net.moss.resonance.item;

import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.particle.ModParticles;

public class SoulBottle extends TintedBottle {
	public SoulBottle(Item.Settings settings) {
		super(settings);
	}

	@SuppressWarnings("deprecation")
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		BlockPos pos = user.getBlockPos();
		user.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 60, 0, true, false), user);
		world.playSound(null, pos, SoundEvents.PARTICLE_SOUL_ESCAPE.value(), SoundCategory.PLAYERS, 1.5f, world.getRandom().nextFloat() * 0.4f + 0.8f);
		for (BlockPos blockPos : BlockPos.iterateOutwards(pos, 32, 32, 32)) {
			if (!world.getBlockState(blockPos).isSolid() && world.getBlockState(blockPos.down()).isSideSolidFullSquare(world, blockPos.down(), Direction.UP) && blockPos.isWithinDistance(pos, 16)) {
				world.addParticle(ModParticles.SOUL_DARKNESS, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0, 0, 0);
			}
		}
		return emptyBottle(world, user, hand, 20);
	}

	@Override public Block getBlock() { return Resonance.SOUL_BOTTLE_BLOCK; }
}