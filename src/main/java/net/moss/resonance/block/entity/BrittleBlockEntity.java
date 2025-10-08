package net.moss.resonance.block.entity;

import org.jetbrains.annotations.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.moss.resonance.Resonance;

public class BrittleBlockEntity extends BlockEntity {
	protected BlockState storedState = Blocks.AIR.getDefaultState();

	public BrittleBlockEntity(BlockPos pos, BlockState state) {
		super(Resonance.BRITTLE_BLOCK_ENTITY, pos, state);
	}

	public void setStoredState(BlockState state) { this.storedState = state; }
	public boolean hasStoredState() { return !this.storedState.isAir(); }
	public BlockState getStoredState() { return this.storedState; }
	public void clear() { this.setStoredState(Blocks.AIR.getDefaultState()); }

	@Nullable @Override public Packet<ClientPlayPacketListener> toUpdatePacket() { return BlockEntityUpdateS2CPacket.create(this); }
	@Override public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) { return createNbt(registryLookup); }

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		this.storedState = nbt.contains("State", NbtElement.COMPOUND_TYPE) ? NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), nbt.getCompound("State")) : Blocks.AIR.getDefaultState();
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		if (hasStoredState()) { nbt.put("State", NbtHelper.fromBlockState(this.getStoredState())); }
	}
}