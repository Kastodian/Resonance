package net.moss.resonance.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.moss.resonance.block.entity.TintedBottleBlockEntity;

public class TintedBottleBlock extends LanternBlock implements BlockEntityProvider {
	public final Item ITEM;

	public TintedBottleBlock(AbstractBlock.Settings settings, Item bottle) {
		super(settings);
		this.ITEM = bottle;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TintedBottleBlockEntity(pos, state);
	}

	@Override
	public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
		if (world.getBlockEntity(pos) instanceof TintedBottleBlockEntity blockEntity) { return blockEntity.getPickStack(ITEM.getDefaultStack()); }
		else { return new ItemStack(ITEM); }
	}
}