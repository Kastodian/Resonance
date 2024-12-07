package net.moss.resonance.block;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import com.google.common.primitives.Ints;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.entity.BuddingPearlIceBlockEntity;
import net.minecraft.state.property.Properties;

public class BuddingPearlIce extends Block implements BlockEntityProvider {
    public static final IntProperty AGE = IntProperty.of("age", 0, 6);
    public static final BooleanProperty HAS_SEED = BooleanProperty.of("has_seed");
    public static final BooleanProperty HAS_POTION = BooleanProperty.of("has_potion");
    public static final BooleanProperty HAS_CATALYST = BooleanProperty.of("has_catalyst");
    public static final BooleanProperty HAS_PEARL = BooleanProperty.of("has_pearl");
    private static final IntProperty DUSTED = Properties.DUSTED;
    private final SoundEvent brushingCompleteSound = Resonance.REVERBERATION_SOUND.value();

    public BuddingPearlIce(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(DUSTED, 0).with(AGE, 0).with(HAS_PEARL, false).with(HAS_POTION, false).with(HAS_CATALYST, false).with(HAS_SEED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(HAS_SEED, HAS_POTION, HAS_PEARL, HAS_CATALYST, AGE, DUSTED);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(world, player, pos, state, blockEntity, tool);
        if (blockEntity instanceof BuddingPearlIceBlockEntity buddingPearlIce && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
            ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, buddingPearlIce.getCatalyst().copy().getRecipeRemainder());
            itemEntity.setToDefaultPickupDelay();
            world.spawnEntity(itemEntity);
            buddingPearlIce.clear();
        }
        if (world.getDimension().ultrawarm()) {
            world.removeBlock(pos, false);
            return;
        }
        BlockState blockState = world.getBlockState(pos.down());
        if (blockState.blocksMovement() || blockState.isLiquid()) { world.setBlockState(pos, Blocks.WATER.getDefaultState()); }
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (random.nextInt(40) == 0 && world.getBlockEntity(pos) instanceof BuddingPearlIceBlockEntity blockEntity) {
            blockEntity.spreadSculk(state, world, random);
            if (getAge(state) == 5 && blockEntity.canCraftPearl()) { craftPearl(blockEntity, pos, state, world, random); }
            else { world.setBlockState(pos, state.with(AGE, getAge(state) + 1), Block.NOTIFY_LISTENERS); }
        }
    }
    
    public void craftPearl(BuddingPearlIceBlockEntity blockEntity, BlockPos pos, BlockState state, ServerWorld world, Random random) {
        ItemStack stack = new ItemStack(Resonance.PEARL);
        ItemStack seed = blockEntity.getSeed();
        ItemStack potion = blockEntity.getPotion();
        if (potion.isOf(Resonance.BIFURCATED_PEARL) || seed.isOf(Resonance.BIFURCATED_PEARL)) { stack = new ItemStack(Resonance.BIFURCATED_PEARL); }

        List<Integer> pearlStats = seed.get(Resonance.PEARL_STATS);
        List<Integer> stats = pearlStats == null ? Ints.asList(1, 1, 1, 0) : Ints.asList(pearlStats.get(0), pearlStats.get(1), pearlStats.get(2), pearlStats.get(3));
        int i = random.nextBetween(0, 2);
        stats.set(i, Math.min(3, stats.get(i) + 1));
        int j = random.nextBetween(0, 2);
        if (i != j) { stats.set(j, Math.min(3, stats.get(j) + 1)); }
        if (potion.contains(Resonance.PEARL_STATS)) {
            List<Integer> potionStats = potion.get(Resonance.PEARL_STATS);
            stats.set(0, Math.min((stats.get(0) + potionStats.get(0) / 2), 3));
            stats.set(1, Math.min((stats.get(1) + potionStats.get(1) / 2), 3));
            stats.set(2, Math.min((stats.get(2) + potionStats.get(2) / 2), 3));
        } if (blockEntity.hasCatalyst() && blockEntity.getCatalyst().contains(Resonance.CATALYST_STATS)) {
            List<Integer> catalystStats = blockEntity.getCatalyst().get(Resonance.CATALYST_STATS);
            stats.set(0, Math.max(1, Math.min(stats.get(0) + catalystStats.get(0), 3)));
            stats.set(1, Math.max(1, Math.min(stats.get(1) + catalystStats.get(1), 3)));
            stats.set(2, Math.max(1, Math.min(stats.get(2) + catalystStats.get(2), 3)));
        } if (stats.get(3) != 1 && random.nextInt(128) == 1) { stats.set(3, 1); }
        
        PotionContentsComponent potionComponent = new PotionContentsComponent(Potions.AWKWARD);
        if (potion.contains(DataComponentTypes.POTION_CONTENTS)){ potionComponent = potion.get(DataComponentTypes.POTION_CONTENTS); }
        if (potion.isOf(Resonance.PEARL) && seed.isOf(Resonance.PEARL)) {
            for (StatusEffectInstance effectInstance : seed.get(DataComponentTypes.POTION_CONTENTS).getEffects()) { potionComponent = potionComponent.with(effectInstance); }
            stack = new ItemStack(Resonance.BIFURCATED_PEARL);
        }
        if (potion.contains(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER)) {
            Integer integer = (Integer)stack.getOrDefault(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER, 0);
            potionComponent = potionComponent.with(new StatusEffectInstance(StatusEffects.BAD_OMEN, 120000, integer, false, false, true));
        }

        stack.set(Resonance.PEARL_STATS, stats);
        stack.set(DataComponentTypes.POTION_CONTENTS, potionComponent);

        blockEntity.clear();
        blockEntity.setPearl(stack);
        BlockState blockState = state.with(BuddingPearlIce.HAS_PEARL, true).with(BuddingPearlIce.HAS_POTION, false).with(BuddingPearlIce.HAS_SEED, false).with(AGE, getAge(state) + 1);
        world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, blockState));
        world.updateNeighborsAlways(pos, blockState.getBlock());
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        if (state.get(AGE) > 2) { return new ItemStack(Resonance.PEARL_ICE); }
        return new ItemStack(Blocks.ICE);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient && world.getBlockEntity(pos) instanceof BuddingPearlIceBlockEntity blockEntity && !state.get(HAS_PEARL)) {
            if ((stack.contains(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER) || stack.contains(DataComponentTypes.POTION_CONTENTS)) && !state.get(HAS_POTION).booleanValue()) {
                blockEntity.setPotion(stack.split(1));
                BlockState blockState = state.with(HAS_POTION, true);
                world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, blockState));
                world.updateNeighborsAlways(pos.down(), blockState.getBlock());
                return ItemActionResult.success(world.isClient);
            } else if (stack.contains(Resonance.CATALYST_STATS) && !state.get(HAS_CATALYST).booleanValue()) {
                blockEntity.setCatalyst(stack.split(1));
                BlockState blockState = state.with(HAS_CATALYST, true);
                world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, blockState));
                world.updateNeighborsAlways(pos.down(), blockState.getBlock());
                return ItemActionResult.success(world.isClient);
            }
        }
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public void putSeed(World world, BlockPos pos, BlockState state, ItemStack stack) {
        if (!state.get(HAS_SEED).booleanValue()) {
            if (!world.isClient) {
                world.getBlockEntity(pos, Resonance.BUDDING_PEARL_ICE_BLOCK_ENTITY).get().setSeed(stack.split(1));
                BlockState blockState = state.with(HAS_SEED, true);
                world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, blockState));
                world.updateNeighborsAlways(pos, blockState.getBlock());
            }
        }
    }
    
    public void putPotion(World world, BlockPos pos, BlockState state, ItemStack stack) {
        if (!state.get(HAS_POTION).booleanValue()) {
            if (!world.isClient) {
                world.getBlockEntity(pos, Resonance.BUDDING_PEARL_ICE_BLOCK_ENTITY).get().setPotion(stack.split(1));            
                BlockState blockState = state.with(HAS_POTION, true);
                world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, blockState));
                world.updateNeighborsAlways(pos, blockState.getBlock());
            }
        }
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) { 
        if (stateFrom.isOf(this)) { return getAge(stateFrom) == getAge(state); }
        boolean bl = stateFrom.isOf(this) ? getAge(stateFrom) == getAge(state) : stateFrom.isOf(Blocks.ICE) && getAge(state) == 0;
        return bl ? true : super.isSideInvisible(state, stateFrom, direction);
    }

    @Override public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) { world.getBlockEntity(pos, Resonance.BUDDING_PEARL_ICE_BLOCK_ENTITY).get().scheduledTick(); }
    public SoundEvent getBrushingCompleteSound() { return this.brushingCompleteSound; }
    @Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new BuddingPearlIceBlockEntity(pos, state); }
    public int getAge(BlockState state) { return state.get(AGE); }
    public final boolean isMature(BlockState state) { return this.getAge(state) >= 6; }
    @Override protected boolean hasRandomTicks(BlockState state) { return !this.isMature(state) && state.get(HAS_POTION).booleanValue(); }
}
