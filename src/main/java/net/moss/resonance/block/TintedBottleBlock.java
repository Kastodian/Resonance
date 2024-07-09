package net.moss.resonance.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class TintedBottleBlock extends LanternBlock {
    public final Item ITEM;

    public TintedBottleBlock(AbstractBlock.Settings settings, Item bottle) {
        super(settings);
        this.ITEM = bottle;
    }

    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return new ItemStack(ITEM);
    }
}