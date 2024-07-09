package net.moss.resonance.block;

import java.util.function.BiConsumer;

import org.joml.Math;
import org.joml.Vector3f;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.moss.resonance.event.ReverberateEvent;
import net.moss.resonance.particle.ModParticles;
import net.minecraft.block.ShapeContext;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;

public class ChargedAmethystBlock extends AmethystClusterBlock {
    public static final IntProperty STAGE = IntProperty.of("stage",0,3);
    public static final BooleanProperty POWERED = BooleanProperty.of("powered");
    private static final Integer[] STAGE_TO_HEIGHT = new Integer[]{7,5,4,3};
    private static final Integer[] STAGE_TO_XZOFFSET = new Integer[]{3,3,3,4};

    public ChargedAmethystBlock() {
        super(7, 3, AbstractBlock.Settings.copy(Blocks.AMETHYST_CLUSTER).luminance(state -> 7 - 2 * state.get(STAGE)));
        this.setDefaultState(this.getDefaultState().with(STAGE, 0).with(POWERED, false));
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return Math.clamp(1, 15, (3 - state.get(STAGE)) * 5);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(STAGE, POWERED);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (random.nextInt(50) != 0 || world.getBlockState(pos.offset(state.get(FACING).getOpposite())).isOf(Blocks.BUDDING_AMETHYST) || world.getBlockState(pos.offset(state.get(FACING).getOpposite())).isOf(Blocks.AMETHYST_BLOCK)) {
            return;
        }
        world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.BLOCKS, 1f, 1f);
        world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1f, 1f);
        resonateBlock(world, state, pos, 1, false);
        if (state.get(STAGE) < 3) {
            BlockState blockState = this.getDefaultState().with(FACING, state.get(FACING)).with(WATERLOGGED, state.get(WATERLOGGED)).with(STAGE, state.get(STAGE) + 1);
            world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
        }
        else {
            BlockState blockState = Blocks.SMALL_AMETHYST_BUD.getDefaultState().with(FACING, state.get(FACING)).with(WATERLOGGED, state.get(WATERLOGGED));
            world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
        }
    }
    
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isClient) { return; }
        if (world.isReceivingRedstonePower(pos) && !state.get(POWERED)) {
            world.setBlockState(pos, state.cycle(POWERED), Block.NOTIFY_LISTENERS);
            world.scheduleBlockTick(pos, this, 1);
        } else if (!world.isReceivingRedstonePower(pos) && state.get(POWERED)) {
            world.setBlockState(pos, state.cycle(POWERED), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.BLOCKS, 1f, 1f);
        world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1f, 1f);
        resonateBlock(world, state, pos, 0.6f, false);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld) { resonateBlock(serverWorld, state, pos, 1f, false); }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        ParticleUtil.spawnParticle(state.get(FACING).getAxis(), world, pos, 0.125, ModParticles.SPARK_PARTICLE, UniformIntProvider.create(1, 2));
    }

    public void resonateBlock(ServerWorld world, BlockState state, BlockPos pos, float strength, boolean windCharged) {
		Vector3f rotOffest = new Vector3f(pos.getX(), pos.getY(), pos.getZ());
        ReverberateEvent.REVERBERATE.invoker().onResonate(world, pos.toCenterPos(), rotOffest, null, null, strength, windCharged);
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Integer stage = state.get(STAGE);
        Integer xzOffset = STAGE_TO_XZOFFSET[stage];
        Integer height = STAGE_TO_HEIGHT[stage];
        Direction direction = state.get(FACING);
        switch (direction) {
            case NORTH: { return Block.createCuboidShape(xzOffset, xzOffset, 16 - height, 16 - xzOffset, 16 - xzOffset, 16.0); }
            case SOUTH: { return Block.createCuboidShape(xzOffset, xzOffset, 0.0, 16 - xzOffset, 16 - xzOffset, height); }
            case EAST: { return Block.createCuboidShape(0.0, xzOffset, xzOffset, height, 16 - xzOffset, 16 - xzOffset); }
            case WEST: { return Block.createCuboidShape(16 - height, xzOffset, xzOffset, 16.0, 16 - xzOffset, 16 - xzOffset); }
            case DOWN: { return Block.createCuboidShape(xzOffset, 16 - height, xzOffset, 16 - xzOffset, 16.0, 16 - xzOffset); }
            default: break;
        }
        return Block.createCuboidShape(xzOffset, 0.0, xzOffset, 16 - xzOffset, height, 16 - xzOffset);
    }

    @Override
    protected void onExploded(BlockState state, World world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        if (world instanceof ServerWorld serverWorld && explosion.getPower() > 0.5f) { resonateBlock(serverWorld, world.getBlockState(pos), pos, 1.5f, true); }
        super.onExploded(state, world, pos, explosion, stackMerger);
    }
}
