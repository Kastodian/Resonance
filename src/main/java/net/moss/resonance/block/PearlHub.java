package net.moss.resonance.block;

import java.util.function.BiConsumer;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.entity.PearlHubBlockEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class PearlHub extends HorizontalFacingBlock implements BlockEntityProvider {
    public static final BooleanProperty HAS_PEARL = BooleanProperty.of("has_pearl");
    public static final BooleanProperty EXPENDED = BooleanProperty.of("expended");
    public static final BooleanProperty POWERED = BooleanProperty.of("powered");
    public static final MapCodec<PearlHub> CODEC = PearlHub.createCodec(PearlHub::new);
    private int xIn;
    private int yIn;
    
    public PearlHub(AbstractBlock.Settings settings, int xIn, int yIn) {
        super(settings);
        this.xIn = xIn;
        this.yIn = yIn;
        this.setDefaultState(getDefaultState().with(HAS_PEARL, false).with(EXPENDED, false).with(FACING, Direction.NORTH));
    }

    public PearlHub(AbstractBlock.Settings settings) {
        this(settings, 2, 7);
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(HAS_PEARL, EXPENDED, POWERED, FACING);
    }


    @Override public BlockState getPlacementState(ItemPlacementContext ctx) { return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()); }
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return Block.createCuboidShape(xIn, 0, xIn, 16 - xIn, yIn, 16 - xIn); }
    @Override public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }
    @Nullable @Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new PearlHubBlockEntity(pos, state); }
    @Override protected boolean hasComparatorOutput(BlockState state) { return true; }
    @Override public MapCodec<? extends PearlHub> getCodec() { return CODEC; }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof PearlHubBlockEntity pearlHubBlockEntity && pearlHubBlockEntity.hasPearl()) {
            return state.get(EXPENDED) ? 1 : 15;
        }
        return 0;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (state.get(HAS_PEARL).booleanValue()) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.isIn(Resonance.PEARL_ITEMS_TAG) || stack.isOf(Resonance.PEARL) || stack.isOf(Resonance.BIFURCATED_PEARL)) {
            return PearlHub.putPearlIfAbsent(player, world, pos, state, stack) ? ItemActionResult.success(world.isClient) : ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) { return ActionResult.CONSUME; }
        if (world.getBlockEntity(pos) instanceof PearlHubBlockEntity blockEntity && state.get(HAS_PEARL)) {
            if (player.isSneaking()) {
                this.dropPearl(blockEntity, world, pos);
                PearlHub.setHasPearl(player, world, pos, state, false);
                return ActionResult.success(true);
            } else if (!state.get(EXPENDED)) {
                world.setBlockState(pos, state.with(EXPENDED, true), Block.NOTIFY_LISTENERS);
                world.playSound(null, pos, Resonance.PEARL_HUB_USE_SOUND.value(), SoundCategory.BLOCKS);
                int i = blockEntity.usePearl(world, pos, player);
                world.scheduleBlockTick(pos, this, world.getGameRules().getInt(Resonance.PEARL_REFRESH_DELAY_GAMERULE) / i);
                return ActionResult.success(true);
            }
        }
        return ActionResult.CONSUME;
    }

    @Override
    protected void onExploded(BlockState state, World world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!state.get(EXPENDED) && state.get(HAS_PEARL) && blockEntity instanceof PearlHubBlockEntity pearlHubBlockEntity) {
            world.setBlockState(pos, state.with(EXPENDED, true), Block.NOTIFY_LISTENERS);
            int i = pearlHubBlockEntity.pulsePearl(world, pos);
            world.scheduleBlockTick(pos, this, world.getGameRules().getInt(Resonance.PEARL_REFRESH_DELAY_GAMERULE) / i);
        }
        super.onExploded(state, world, pos, explosion, stackMerger);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS);
        world.setBlockState(pos, state.with(EXPENDED, false), Block.NOTIFY_LISTENERS);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) { return; }
        if (world.getBlockEntity(pos) instanceof PearlHubBlockEntity blockEntity && state.get(HAS_PEARL).booleanValue()) {
            this.dropPearl(blockEntity, world, pos);
            blockEntity.clear();
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    private void dropPearl(PearlHubBlockEntity pearlHubBlockEntity, World world, BlockPos pos) {
        ItemStack itemStack = pearlHubBlockEntity.getPearl().copy();
        ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, itemStack);
        world.playSound(null, pos, Resonance.PEARL_HUB_REMOVE_SOUND.value(), SoundCategory.BLOCKS);
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
    }

    public static boolean putPearlIfAbsent(@Nullable Entity user, World world, BlockPos pos, BlockState state, ItemStack stack) {
        if (!state.get(HAS_PEARL).booleanValue()) {
            world.playSound(null, pos, Resonance.PEARL_HUB_ADD_SOUND.value(), SoundCategory.BLOCKS);
            if (!world.isClient && world.getBlockEntity(pos) instanceof PearlHubBlockEntity blockEntity) {
                blockEntity.setPearl(stack.split(1));
                PearlHub.setHasPearl(user, world, pos, state, true);
            }
            return true;
        }
        return false;
    }

    public static void setHasPearl(@Nullable Entity user, World world, BlockPos pos, BlockState state, boolean hasPearl) {
        BlockState blockState = state.with(HAS_PEARL, hasPearl);
        world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(user, blockState));
        world.updateNeighborsAlways(pos.down(), blockState.getBlock());
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isClient) { return; }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (world.isReceivingRedstonePower(pos) && !state.get(POWERED) && !state.get(EXPENDED) && state.get(HAS_PEARL) && blockEntity instanceof PearlHubBlockEntity pearlHubBlockEntity) {
            world.setBlockState(pos, state.with(POWERED, true).with(EXPENDED, true), Block.NOTIFY_LISTENERS);
            int i = pearlHubBlockEntity.pulsePearl(world, pos);
            world.scheduleBlockTick(pos, this, 5 * world.getGameRules().getInt(Resonance.PEARL_REFRESH_DELAY_GAMERULE) / i);
        } else if (!world.isReceivingRedstonePower(pos) && state.get(POWERED) ) {
            world.setBlockState(pos, state.cycle(POWERED), Block.NOTIFY_LISTENERS);
        }
    }
}
