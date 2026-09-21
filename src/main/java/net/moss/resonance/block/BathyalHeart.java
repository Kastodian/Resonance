package net.moss.resonance.block;

import org.jetbrains.annotations.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.Resonance;

public class BathyalHeart extends Block implements Waterloggable {
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public static final BooleanProperty POWERED = Properties.POWERED;
	public static final IntProperty POWER = Properties.POWER;

	public BathyalHeart(Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false).with(POWERED, false).with(POWER, 0));
	}

	@Override protected boolean hasRandomTicks(BlockState state) { return state.get(WATERLOGGED); }
	@Override public FluidState getFluidState(BlockState state) { return state.get(WATERLOGGED).booleanValue() ? Fluids.WATER.getStill(false) : state.getFluidState(); }
	@Override protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { super.appendProperties(builder.add(WATERLOGGED, POWERED, POWER)); }
	@Override protected int getComparatorOutput(BlockState state, World world, BlockPos pos) { return state.get(POWER); }
	@Override protected boolean hasComparatorOutput(BlockState state) { return true; }

	@Override
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		World world = ctx.getWorld();
		BlockPos pos = ctx.getBlockPos();
		return this.getDefaultState().with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER).with(POWER, world.getReceivedRedstonePower(pos)).with(POWERED, world.isReceivingRedstonePower(pos));
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		world.getEntitiesByClass(LivingEntity.class, new Box(pos).expand(32), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and((entity) -> entity.isSubmergedInWater())).forEach(entity -> {
			entity.addStatusEffect(new StatusEffectInstance(Resonance.CRUSHING_WEIGHT, 600, 0, true, false));
		});
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		int i = pos.getX();
		int j = pos.getY();
		int k = pos.getZ();
		world.addParticle(ParticleTypes.ASH, i + random.nextDouble(), j + 0.7, k + random.nextDouble(), 0, 0, 0);
		if (!state.get(WATERLOGGED)) { return; }
		if (random.nextInt(100) == 1) { world.playSound(pos.getX(), pos.getY(), pos.getZ(), Resonance.BATHYAL_HEART_AMBIENT_SOUND.value(), SoundCategory.AMBIENT, 0.2f, 0.5f + world.random.nextFloat() * 1.2f, false); }
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		for (int l = 0; l < 64; l++) {
			mutable.set(i + MathHelper.nextInt(random, -10, 10), j - MathHelper.nextInt(random, -10, 10), k + MathHelper.nextInt(random, -10, 10));
			if (!world.getBlockState(mutable).isFullCube(world, mutable)) {
				world.addParticle(ParticleTypes.WHITE_ASH, mutable.getX() + random.nextDouble(), mutable.getY() + random.nextDouble(), mutable.getZ() + random.nextDouble(), 0, 0, 0);
				world.addParticle(ParticleTypes.WHITE_ASH, mutable.getX() + random.nextDouble(), mutable.getY() + random.nextDouble(), mutable.getZ() + random.nextDouble(), 0, 0, 0);
				world.addParticle(ParticleTypes.WHITE_ASH, mutable.getX() + random.nextDouble(), mutable.getY() + random.nextDouble(), mutable.getZ() + random.nextDouble(), 0, 0, 0);
			}
		}
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
		if (world.isClient()) { return; }
		if (world.isReceivingRedstonePower(pos)) {
			if (!state.get(POWERED) && state.get(WATERLOGGED)) { world.playSound(null, pos, Resonance.BATHYAL_HEART_PULSE_SOUND.value(), SoundCategory.AMBIENT, 0.2f, 0.5f + world.random.nextFloat() * 1.2f); }
			world.setBlockState(pos, state.with(POWERED, true).with(POWER, world.getReceivedRedstonePower(pos)), Block.NOTIFY_LISTENERS);
		} else if (state.get(POWERED)) {
			world.setBlockState(pos, state.with(POWERED, false).with(POWER, 0), Block.NOTIFY_LISTENERS);
		}
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		super.onStateReplaced(state, world, pos, newState, moved);
		if (world instanceof ServerWorld serverWorld && ((state.isOf(this) && state.get(WATERLOGGED)) || (newState.isOf(this) && newState.get(WATERLOGGED)))) {
			updateSignal(state, serverWorld, pos);
		}
	}

	protected void updateSignal(BlockState state, ServerWorld world, BlockPos pos) {
		world.emitGameEvent(Resonance.BATHYAL_HEART_PULSE_EVENT, pos, GameEvent.Emitter.of(state));
		for (Direction direction : Direction.values()) {
			for (int i = 1; i <= 8; i++) {
				BlockPos newPos = pos.offset(direction, i);
				BlockState outerState = world.getBlockState(newPos);
				if (outerState.isIn(BlockTags.OCCLUDES_VIBRATION_SIGNALS) || outerState.isOf(this)) { break; }
				if (i % 2 == 1) { continue; }
				world.updateNeighbor(newPos, this.asBlock(), pos);
				world.spawnParticles(ParticleTypes.SCULK_CHARGE_POP, newPos.getX() + 0.5, newPos.getY() + 0.5, newPos.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.025);
			}
		}
	}
}