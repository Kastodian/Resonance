package net.moss.resonance.util.abstracts;

import org.jetbrains.annotations.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.Resonance;

public abstract class BlockEntityWithItem extends BlockEntity implements Clearable {
	protected ItemStack stack;

	protected BlockEntityWithItem(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
		super(blockEntityType, pos, state);
		this.stack = ItemStack.EMPTY;
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		this.stack = nbt.contains("Stack") ? ItemStack.fromNbtOrEmpty(registryLookup, nbt.getCompound("Stack")) : ItemStack.EMPTY;
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		nbt.put("Stack", this.getStack().encodeAllowEmpty(registryLookup));
	}

	public void dropStack(World world, BlockPos pos, double x, double y, double z) {
		ItemEntity itemEntity = new ItemEntity(world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, this.getStack().copy());
		world.playSound(null, pos, Resonance.REMOVE_ITEM_SOUND.value(), SoundCategory.BLOCKS);
		itemEntity.setToDefaultPickupDelay();
		world.spawnEntity(itemEntity);
	}

	public void setStack(ItemStack stack) {
		this.stack = stack;
		BlockState state = this.getCachedState();
		if (this.getWorld() instanceof ServerWorld serverWorld) {
			serverWorld.updateListeners(this.getPos(), state, state, Block.NOTIFY_ALL);
			serverWorld.emitGameEvent(GameEvent.BLOCK_CHANGE, this.getPos(), GameEvent.Emitter.of(state));
			BlockEntity.markDirty(serverWorld, this.getPos(), state);
		}
	}

	@Nullable @Override public Packet<ClientPlayPacketListener> toUpdatePacket() { return BlockEntityUpdateS2CPacket.create(this); }
	@Override public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) { return this.createNbt(registryLookup); }
	@Override public void clear() { this.setStack(ItemStack.EMPTY); }
	public ItemStack getStack() { return this.stack; }
	public boolean hasStack() { return !this.stack.isEmpty(); }
}