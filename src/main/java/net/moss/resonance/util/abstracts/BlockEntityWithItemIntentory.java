package net.moss.resonance.util.abstracts;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public abstract class BlockEntityWithItemIntentory extends BlockEntityWithItem implements SingleStackInventory {
	protected BlockEntityWithItemIntentory(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) { super(blockEntityType, pos, state); }
	@Override public boolean canPlayerUse(PlayerEntity player) { return Inventory.canPlayerUse(null, player); }
	@Override public boolean isValid(int slot, ItemStack stack) { return this.isEmpty(); }
}