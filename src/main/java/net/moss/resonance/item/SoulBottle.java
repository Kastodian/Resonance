package net.moss.resonance.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.LostSoulBlock;
import net.moss.resonance.util.Utils;

public class SoulBottle extends TintedBottle {
    public SoulBottle(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockPos pos = user.getBlockPos();
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        user.getItemCooldownManager().set(this, 20);
        world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
        world.playSound(null, pos, SoundEvents.PARTICLE_SOUL_ESCAPE.value(), SoundCategory.PLAYERS, 1.5f, world.getRandom().nextFloat() * 0.4f + 0.8f);
        boolean clearArea = true;
        for (BlockPos blockPos : BlockPos.iterateOutwards(pos, 24, 24, 24)) {
            BlockState state = world.getBlockState(blockPos);
            if (state.isOf(Resonance.LOST_SOUL_ECHO) && !Utils.canMobSpawn(world, blockPos)) { world.removeBlock(blockPos, false); }
            if (state.isAir() && Utils.canMobSpawn(world, blockPos) && blockPos.isWithinDistance(pos, 12)) {
                world.setBlockState(blockPos, Resonance.LOST_SOUL_ECHO.getDefaultState().with(LostSoulBlock.DURATION, 1), Block.NOTIFY_LISTENERS);
                clearArea = false;
            }
        }
        if (clearArea) { for (int i = 0; i < 6; i++) { world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, user.getX(), user.getHeight() + user.getHeight() / 2, user.getZ(), (world.random.nextFloat() - 0.5) / 4, (world.random.nextFloat() - 0.5) / 4, (world.random.nextFloat() - 0.5) / 4); } }
        if (!user.getAbilities().creativeMode) {
            ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, user, Resonance.TINTED_BOTTLE.getDefaultStack());
            return TypedActionResult.success(itemStack2, world.isClient());
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override public Block getBlock() { return Resonance.SOUL_BOTTLE_BLOCK; }
}
