package net.moss.resonance.block.entity;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldEvents;
import net.moss.resonance.Resonance;

public class BuddingPearlIceBlockEntity extends BlockEntity {
    private final SculkSpreadManager spreadManager;
    ItemStack seed = ItemStack.EMPTY;
    ItemStack potion = ItemStack.EMPTY;
    ItemStack catalyst = ItemStack.EMPTY;
    ItemStack pearl = ItemStack.EMPTY;
    private int brushesCount;
    private long nextDustTime;
    private long nextBrushTime;
    @Nullable private Direction hitDirection;

    public BuddingPearlIceBlockEntity(BlockPos pos, BlockState state) {
        super(Resonance.BUDDING_PEARL_ICE_BLOCK_ENTITY, pos, state);
        this.spreadManager = SculkSpreadManager.create();
    }

    public void spreadSculk(BlockState state, ServerWorld world, Random random) {
        BlockPos pos2 = pos.add(random.nextBetween(0, 6) - 3, random.nextBetween(0, 6) - 3, random.nextBetween(0, 6) - 3);
        this.spreadManager.spread(pos2, 25);
        this.spreadManager.tick(world, pos2, random, true);
        this.spreadManager.tick(world, pos2, random, true);
        this.spreadManager.tick(world, pos2, random, true);
        if ((potion.isOf(Resonance.PEARL) && seed.isOf(Resonance.PEARL)) || (potion.isOf(Resonance.BIFURCATED_PEARL) && seed.isOf(Resonance.BIFURCATED_PEARL))) {
            this.spreadManager.tick(world, pos2, random, true);
            this.spreadManager.tick(world, pos2, random, true);
            this.spreadManager.tick(world, pos2, random, true);
        }
    }

    public boolean brush(long worldTime, Direction hitDirection) {
        if (this.hitDirection == null) { this.hitDirection = hitDirection; }
        this.nextDustTime = worldTime + 40L;
        if (worldTime < this.nextBrushTime || !(this.world instanceof ServerWorld)) { return false; }
        this.nextBrushTime = worldTime + 10L;
        int i = this.getDustedLevel();
        if (++this.brushesCount >= 10) {
            this.finishBrushing();
            return true;
        }
        this.world.scheduleBlockTick(this.getPos(), this.getCachedState().getBlock(), 2);
        int j = this.getDustedLevel();
        if (i != j) { this.world.setBlockState(this.getPos(), this.getCachedState().with(Properties.DUSTED, j), Block.NOTIFY_ALL); }
        return false;
    }

    public void scheduledTick() {
        if (this.world == null) { return; }
        if (this.brushesCount != 0 && this.world.getTime() >= this.nextDustTime) {
            int i = this.getDustedLevel();
            this.brushesCount = Math.max(0, this.brushesCount - 2);
            int j = this.getDustedLevel();
            if (i != j) {
                this.world.setBlockState(this.getPos(), (BlockState)this.getCachedState().with(Properties.DUSTED, j), Block.NOTIFY_ALL);
            }
            this.nextDustTime = this.world.getTime() + 4L;
        }
        if (this.brushesCount == 0) {
            this.hitDirection = null;
            this.nextDustTime = 0L;
            this.nextBrushTime = 0L;
        } else {
            this.world.scheduleBlockTick(this.getPos(), this.getCachedState().getBlock(), 2);
        }
    }

    private void finishBrushing() {
        if (this.world == null || this.world.getServer() == null) { return; }
        this.spawnItem();
        this.world.playSound(null, pos, Resonance.REVERBERATION_SOUND.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
        BlockState blockState = this.getCachedState();
        this.world.syncWorldEvent(WorldEvents.BLOCK_FINISHED_BRUSHING, this.getPos(), Block.getRawIdFromState(blockState));
        this.world.setBlockState(this.pos, Resonance.PEARL_ICE.getDefaultState(), Block.NOTIFY_ALL);
    }

    private void spawnItem() {
        if (!hasPearl() || this.world == null || this.world.getServer() == null) { return; }
        double d = EntityType.ITEM.getWidth();
        double e = 1.0 - d;
        double f = d / 2.0;
        Direction direction = Objects.requireNonNullElse(this.hitDirection, Direction.UP);
        BlockPos blockPos = this.pos.offset(direction, 1);
        double g = blockPos.getX() + 0.5 * e + f;
        double h = blockPos.getY() + 0.5 + EntityType.ITEM.getHeight() / 2.0f;
        double i = blockPos.getZ() + 0.5 * e + f;
        ItemEntity itemEntity = new ItemEntity(this.world, g, h, i, this.pearl.split(this.world.random.nextInt(21) + 10));
        itemEntity.setVelocity(Vec3d.ZERO);
        this.world.spawnEntity(itemEntity);
        setPearl(ItemStack.EMPTY);
    }
    
    public boolean canCraftPearl() { return hasSeed() && hasPotion() && (potion.contains(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER) || potion.contains(DataComponentTypes.POTION_CONTENTS)); }
    public boolean hasSeed() { return !this.seed.isEmpty(); }
    public boolean hasPotion() { return !this.potion.isEmpty(); }
    public boolean hasCatalyst() { return !this.catalyst.isEmpty(); }
    public boolean hasPearl() { return !this.pearl.isEmpty(); }
    public ItemStack getSeed() { return this.seed; }
    public ItemStack getPotion() { return this.potion; }
    public ItemStack getCatalyst() { return this.catalyst; }
    public ItemStack getPearl() { return this.pearl; }
    public void setSeed(ItemStack seed) { this.seed = seed; }
    public void setPotion(ItemStack potion) { this.potion = potion; }
    public void setCatalyst(ItemStack catalyst) { this.catalyst = catalyst; }
    public void setPearl(ItemStack pearl) { this.pearl = pearl; }
    public void clear() { this.setSeed(ItemStack.EMPTY); this.setPotion(ItemStack.EMPTY); this.setCatalyst(ItemStack.EMPTY); this.setPearl(ItemStack.EMPTY); }
    @Nullable public Direction getHitDirection() { return this.hitDirection; }
    @Nullable @Override public Packet<ClientPlayPacketListener> toUpdatePacket() { return BlockEntityUpdateS2CPacket.create(this); }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.seed = nbt.contains("Seed", NbtElement.COMPOUND_TYPE) ? ItemStack.fromNbt(registryLookup, nbt.getCompound("Seed")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        this.potion = nbt.contains("Potion", NbtElement.COMPOUND_TYPE) ? ItemStack.fromNbt(registryLookup, nbt.getCompound("Potion")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        this.catalyst = nbt.contains("Catalyst", NbtElement.COMPOUND_TYPE) ? ItemStack.fromNbt(registryLookup, nbt.getCompound("Catalyst")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        this.pearl = nbt.contains("Pearl", NbtElement.COMPOUND_TYPE) ? ItemStack.fromNbt(registryLookup, nbt.getCompound("Pearl")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        if (nbt.contains("hit_direction")) { this.hitDirection = Direction.values()[nbt.getInt("hit_direction")]; }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (hasSeed()) { nbt.put("Seed", this.getSeed().encode(registryLookup)); }
        if (hasPotion()) { nbt.put("Potion", this.getPotion().encode(registryLookup)); }
        if (hasCatalyst()) { nbt.put("Catalyst", this.getCatalyst().encode(registryLookup)); }
        if (hasPearl()) { nbt.put("Pearl", this.getPearl().encode(registryLookup)); }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbtCompound = super.toInitialChunkDataNbt(registryLookup);
        if (this.hitDirection != null) { nbtCompound.putInt("hit_direction", this.hitDirection.ordinal()); }
        if (hasSeed()) { nbtCompound.put("Seed", this.seed.encode(registryLookup)); }
        if (hasPotion()) { nbtCompound.put("Potion", this.potion.encode(registryLookup)); }
        if (hasCatalyst()) { nbtCompound.put("Catalyst", this.catalyst.encode(registryLookup)); }
        if (hasPearl()) { nbtCompound.put("Pearl", this.pearl.encode(registryLookup)); }
        return nbtCompound;
    }

    private int getDustedLevel() {
        if (this.brushesCount == 0) { return 0; }
        if (this.brushesCount < 3) { return 1; }
        if (this.brushesCount < 6) { return 2; }
        return 3;
    }
}