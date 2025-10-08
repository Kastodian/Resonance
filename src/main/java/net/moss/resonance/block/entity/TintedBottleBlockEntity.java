package net.moss.resonance.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.text.Text.Serialization;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.moss.resonance.Resonance;

public class TintedBottleBlockEntity extends BlockEntity implements Nameable {
	private Text customName;
	private int color;

	public TintedBottleBlockEntity(BlockPos pos, BlockState state) {
		super(Resonance.TINTED_BOTTLE_BLOCK_ENTITY, pos, state);
	}

	public ItemStack getPickStack(ItemStack itemstack) {
		itemstack.applyComponentsFrom(this.createComponentMap());
		return itemstack;
	}

	public int getColor() {
		return this.color;
	}

	@Override
	protected void readComponents(ComponentsAccess components) {
		super.readComponents(components);
		this.color = (components.getOrDefault(DataComponentTypes.DYED_COLOR, new DyedColorComponent(DyedColorComponent.DEFAULT_COLOR, true))).rgb();
		this.customName = components.get(DataComponentTypes.CUSTOM_NAME);
	}

	@Override
	protected void addComponents(ComponentMap.Builder componentMapBuilder) {
		super.addComponents(componentMapBuilder);
		componentMapBuilder.add(DataComponentTypes.DYED_COLOR, new DyedColorComponent(this.color, true));
		componentMapBuilder.add(DataComponentTypes.CUSTOM_NAME, this.customName);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		if (this.color != DyedColorComponent.DEFAULT_COLOR) { nbt.putInt("color", this.color); }
		if (this.customName != null) { nbt.putString("CustomName", Serialization.toJsonString(this.customName, registryLookup)); }
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		if (nbt.contains("CustomName", 8)) { this.customName = tryParseCustomName(nbt.getString("CustomName"), registryLookup); }
		if (nbt.contains("color")) { this.color = nbt.getInt("color"); }
	}

	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createNbt(registryLookup);
	}

	@Override
	public Text getName() {
		return this.customName != null ? this.customName : Text.translatable("block.resonance.tinted_bottle");
	}
}