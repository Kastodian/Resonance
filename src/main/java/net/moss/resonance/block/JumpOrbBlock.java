package net.moss.resonance.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.Resonance;
import net.moss.resonance.util.Utils;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class JumpOrbBlock extends Block implements Waterloggable {
	public static final BooleanProperty EXPENDED = BooleanProperty.of("expended");
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public final RegistryEntry<StatusEffect> statusEffect;

	public JumpOrbBlock(RegistryEntry<StatusEffect> statusEffect) {
		super(AbstractBlock.Settings.copy(Blocks.AMETHYST_CLUSTER).ticksRandomly().noBlockBreakParticles().noCollision().luminance(state -> state.get(EXPENDED) ? 1 : 5));
		this.setDefaultState(this.getDefaultState().with(EXPENDED, false).with(WATERLOGGED, false));
		this.statusEffect = statusEffect;
	}

	@Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { super.appendProperties(builder.add(EXPENDED, WATERLOGGED)); }
	@Override public FluidState getFluidState(BlockState state) { return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : state.getFluidState(); }
	@Override public BlockState getPlacementState(ItemPlacementContext ctx) { return this.getDefaultState().with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER); }
	@Override public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return Block.createCuboidShape(5, 5, 5, 11, 11, 11); }

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}
		return state;
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (entity.isOnGround() || state.get(EXPENDED)) { return; }
		LivingEntity livingEntity = entity instanceof LivingEntity living ? living : (entity instanceof ProjectileEntity projectileEntity && projectileEntity.getOwner() instanceof LivingEntity living ? living : null);
		if (livingEntity == null) { return; }
		world.emitGameEvent(entity, GameEvent.BLOCK_ACTIVATE, pos);
		world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.BLOCKS, 2f, 0.5f + world.random.nextFloat() * 1.2f);
		world.setBlockState(pos, state.with(EXPENDED, true), Block.NOTIFY_LISTENERS);
		world.scheduleBlockTick(pos, this, world.getGameRules().getInt(Resonance.JUMP_ORB_REFRESH_DELAY_GAMERULE));
		Utils.sumStatusEffectAmplifiers(livingEntity, new StatusEffectInstance(statusEffect, -1, 0), 255);
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.BLOCKS, 2f, 0.5f + random.nextFloat() * 1.2f);
		world.setBlockState(pos, state.with(EXPENDED, false), Block.NOTIFY_LISTENERS);
	}
}