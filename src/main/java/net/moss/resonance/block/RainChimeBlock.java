package net.moss.resonance.block;

import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector3f;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;
import net.moss.resonance.Resonance;
import net.moss.resonance.ResonanceEvents;
import net.moss.resonance.block.entity.RainChimeBlockEntity;
import net.moss.resonance.mixin.ServerWorldAccessor;
import net.moss.resonance.particle.ModParticles;

public class RainChimeBlock extends BlockWithEntity implements Waterloggable {
	public static final MapCodec<RainChimeBlock> CODEC = createCodec(RainChimeBlock::new);
	public static final BooleanProperty AGITATED = BooleanProperty.of("agitated");
	public static final BooleanProperty WEATHER_WARPING = BooleanProperty.of("weather_warping");
	public static final BooleanProperty IS_RAINING = BooleanProperty.of("is_raining");
	public static final BooleanProperty IS_THUNDERING = BooleanProperty.of("is_thundering");
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	public RainChimeBlock(AbstractBlock.Settings settings) {
		super(AbstractBlock.Settings.copy(Blocks.AMETHYST_CLUSTER).nonOpaque().strength(0.075f, 0f).noBlockBreakParticles());
		this.setDefaultState(this.getDefaultState().with(AGITATED, false).with(WATERLOGGED, false).with(WEATHER_WARPING, false).with(IS_RAINING, false).with(IS_THUNDERING, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(AGITATED, WATERLOGGED, WEATHER_WARPING, IS_RAINING, IS_THUNDERING);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return BlockWithEntity.validateTicker(type,	Resonance.RAIN_CHIME_BLOCK_ENTITY, RainChimeBlock::tick);
	}

	public static void tick(World world, BlockPos pos, BlockState state, RainChimeBlockEntity blockEntity) {
		if (world.getTime() % 20L != 0L) { return; }
		if (world.isThundering()) { world.setBlockState(pos, state.with(IS_THUNDERING, true).with(IS_RAINING, true)); }
		else if (world.isRaining()) { world.setBlockState(pos, state.with(IS_THUNDERING, false).with(IS_RAINING, true)); }
		else { world.setBlockState(pos, state.with(IS_THUNDERING, false).with(IS_RAINING, false)); }
	}

	@Override public boolean hasComparatorOutput(BlockState state) { return true; }
	@Override public int getComparatorOutput(BlockState state, World world, BlockPos pos) { return state.get(IS_THUNDERING) ? 15 : (state.get(IS_RAINING) ? 7 : 0); }
	@Override protected MapCodec<? extends BlockWithEntity> getCodec() { return CODEC; }
	@Nullable @Override public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState) { return new RainChimeBlockEntity(blockPos, blockState); }
	@Override public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.INVISIBLE; }
	@Override public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return Block.createCuboidShape(5, 8, 5, 11, 16, 11); }
	@Override public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return Block.createCuboidShape(6, 14, 6, 10, 16, 10); }

	@Override
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockState blockState = this.getDefaultState();
		if (this.canPlaceAt(blockState, ctx.getWorld(), ctx.getBlockPos())) {
			FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
			return blockState.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
		}
		return null;
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return Block.sideCoversSmallSquare(world, pos.offset(Direction.UP), Direction.DOWN);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		if (state.get(WATERLOGGED)) { return Fluids.WATER.getStill(false); }
		return state.getFluidState();
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED)) { world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world)); }
		if (direction == Direction.UP && !state.canPlaceAt(world, pos)) { return Blocks.AIR.getDefaultState(); }
		return state;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (agitate(pos, state, world, true)) { return ActionResult.SUCCESS; }
		else { return ActionResult.PASS; }
	}

	@Override
	public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
		super.onProjectileHit(world, state, hit, projectile);
		ResonanceEvents.REVERBERATE.invoker().onResonate(world, projectile.getPos(), new Vector3f(projectile.prevPitch * MathHelper.DEGREES_PER_RADIAN, projectile.prevYaw * MathHelper.DEGREES_PER_RADIAN, 0f), null, projectile.getOwner(), 1, false);
		if (world.isClient) { return; }
		BlockPos blockPos = hit.getBlockPos();
		world.playSound((PlayerEntity)null, blockPos, SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.BLOCKS, 1.0F, 0.5F + world.random.nextFloat() * 1.2F);
		world.playSound((PlayerEntity)null, blockPos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.0F, 0.5F + world.random.nextFloat() * 1.2F);
		agitate(hit.getBlockPos(), state, world, true);
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (world.isClient) { return; }
		agitate(pos, state, world, false);
	}

	public boolean agitate(BlockPos pos, BlockState state, World world, boolean weatherManipulation) {
		if (!state.get(AGITATED) && !world.isClient) {
			world.setBlockState(pos, state.with(AGITATED, true).with(WEATHER_WARPING, weatherManipulation), Block.NOTIFY_LISTENERS);
			world.emitGameEvent(GameEvent.BLOCK_ACTIVATE, pos, Emitter.of(state));
			world.scheduleBlockTick(pos, this, 2*20);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1f, 0.5f + world.random.nextFloat() * 1.2f);
		if (!state.isOf(this)) { return; }
		if (!world.isThundering() && !world.isRaining() && state.get(WEATHER_WARPING)) {
			int clearWeatherTime = ((ServerWorldAccessor) world).getWorldProperties().getClearWeatherTime();
			if (clearWeatherTime == 0) { clearWeatherTime = random.nextBetween(3000, 80000); }
			clearWeatherTime -= clearWeatherTime > 5000 ? 4000 : clearWeatherTime / 2;
			world.setWeather(Math.clamp(1, 180000, clearWeatherTime), 0, false, false);
		}
		world.setBlockState(pos, state.with(AGITATED, false).with(WEATHER_WARPING, false), Block.NOTIFY_LISTENERS);
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (world.isThundering()) {
			ParticleUtil.spawnParticle(Direction.Axis.Y, world, pos, 0.125, ModParticles.SPARK_PARTICLE, UniformIntProvider.create(1, 1));
		}
		if (random.nextInt(10) == 0) {
			Vec3d vec = pos.toCenterPos();
			world.playSound(vec.getX(), vec.getY(), vec.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.AMBIENT, 1.0f, 0.5f + world.random.nextFloat() * 1.2f, false);
		}
	}
}