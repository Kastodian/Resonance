package net.moss.resonance.block;

import java.util.Optional;
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
import net.minecraft.state.property.Properties;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.moss.resonance.Resonance;
import net.moss.resonance.event.ReverberateEvent;
import net.moss.resonance.particle.ModParticles;
import net.moss.resonance.util.Dischargable;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;

public class ChargedAmethystBlock extends AmethystClusterBlock implements Dischargable {
    public static final BooleanProperty POWERED = Properties.POWERED;
    private static final Integer[] STAGE_TO_HEIGHT = new Integer[]{7,5,4,3};
    private static final Integer[] STAGE_TO_XZOFFSET = new Integer[]{3,3,3,4};
	private final Dischargable.ChargeLevel chargeLevel;

    public ChargedAmethystBlock(Dischargable.ChargeLevel chargeLevel) {
        super(STAGE_TO_HEIGHT[chargeLevel.getNumber()], STAGE_TO_XZOFFSET[chargeLevel.getNumber()], AbstractBlock.Settings.copy(Blocks.AMETHYST_CLUSTER).luminance(state -> 7 - 2 * chargeLevel.getNumber()));
        this.setDefaultState(this.getDefaultState().with(POWERED, false));
        this.chargeLevel = chargeLevel;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWERED);
    }

    @Override public boolean hasComparatorOutput(BlockState state) { return true; }
    @Override public int getComparatorOutput(BlockState state, World world, BlockPos pos) { return Math.clamp(1, 15, (3 - chargeLevel.getNumber()) * 5); }
    @Override public boolean hasRandomTicks(BlockState state) { return true; }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (random.nextInt(50) != 0) { return; }
        BlockState holdingBlock = world.getBlockState(pos.offset(state.get(FACING).getOpposite()));
        if (holdingBlock.isOf(Blocks.BUDDING_AMETHYST) || holdingBlock.isOf(Blocks.AMETHYST_BLOCK)) { return; }
        tickDegradation(state, world, pos, random);
    }

    @Override
    public void tickDegradation(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.tryDegrade(state, world, pos, random).ifPresent(degraded -> world.setBlockState(pos, degraded));
    }

    @Override
    public Optional<BlockState> tryDegrade(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.BLOCKS, 1f, 1f);
        world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1f, 1f);
        resonateBlock(world, state, pos, 1, false);
		return this.getDegradationResult(state);
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
        resonateBlock(world, state, pos, 1f - (chargeLevel.getNumber() * 0.3f), false);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld) { resonateBlock(serverWorld, state, pos, 1f - (chargeLevel.getNumber() * 0.2f), false); }
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

    public static BlockState changeState(BlockState state, int stage) {
        switch (stage) {
            case 0: { return Resonance.CHARGED_AMETHYST_CLUSTER.getStateWithProperties(state); }
            case 1: { return Resonance.LARGE_CHARGED_AMETHYST_BUD.getStateWithProperties(state); }
            case 2: { return Resonance.MEDIUM_CHARGED_AMETHYST_BUD.getStateWithProperties(state); }
            case 3: { return Resonance.SMALL_CHARGED_AMETHYST_BUD.getStateWithProperties(state); }
            default: { return state; }
        }
    }

    @Override
    protected void onExploded(BlockState state, World world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        if (world instanceof ServerWorld serverWorld && explosion.getPower() > 0.5f) { resonateBlock(serverWorld, world.getBlockState(pos), pos, 1.5f, true); }
        super.onExploded(state, world, pos, explosion, stackMerger);
    }

    @Override
    public ChargeLevel getDegradationLevel() {
        return chargeLevel;
    }
}