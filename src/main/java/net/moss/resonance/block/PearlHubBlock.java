package net.moss.resonance.block;

import java.util.function.BiConsumer;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.entity.PearlHubBlockEntity;
import net.moss.resonance.entity.BeaconedPearlEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class PearlHubBlock extends BlockWithEntity implements Waterloggable {
	public static final BooleanProperty HAS_PEARL = BooleanProperty.of("has_pearl");
	public static final BooleanProperty EXPENDED = BooleanProperty.of("expended");
	public static final BooleanProperty BEACON_POWERED = BooleanProperty.of("beacon_powered");
	public static final BooleanProperty POWERED = Properties.POWERED;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final MapCodec<PearlHubBlock> CODEC = PearlHubBlock.createCodec(PearlHubBlock::new);
	private int xIn;
	private int yIn;

	public PearlHubBlock(AbstractBlock.Settings settings, int xIn, int yIn) {
		super(settings);
		this.xIn = xIn;
		this.yIn = yIn;
		this.setDefaultState(getDefaultState().with(HAS_PEARL, false).with(POWERED, false).with(EXPENDED, false).with(FACING, Direction.NORTH).with(WATERLOGGED, false).with(BEACON_POWERED, false));
	}

	public PearlHubBlock(AbstractBlock.Settings settings) {
		this(settings, 2, 7);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder.add(HAS_PEARL, EXPENDED, POWERED, FACING, WATERLOGGED, BEACON_POWERED));
	}

	@Override public BlockState getPlacementState(ItemPlacementContext ctx) { return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER); }
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return Block.createCuboidShape(xIn, 0, xIn, 16 - xIn, yIn, 16 - xIn); }
	@Override public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }
	@Nullable @Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new PearlHubBlockEntity(pos, state); }
	@Override protected boolean hasComparatorOutput(BlockState state) { return true; }
	@Override public MapCodec<? extends PearlHubBlock> getCodec() { return CODEC; }
	@Override protected boolean emitsRedstonePower(BlockState state) { return true; }
	@Override protected int getComparatorOutput(BlockState state, World world, BlockPos pos) { return state.get(HAS_PEARL) ? (state.get(EXPENDED) ? 1 : 15) : 0; }
	@Override protected FluidState getFluidState(BlockState state) { return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state); }
	@Override protected BlockState rotate(BlockState state, BlockRotation rotation) { return state.with(FACING, rotation.rotate(state.get(FACING))); }
	@Override protected BlockState mirror(BlockState state, BlockMirror mirror) { return state.rotate(mirror.getRotation(state.get(FACING))); }

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity user, Hand hand, BlockHitResult hit) {
		if (!state.get(HAS_PEARL) && (stack.isIn(Resonance.PEARL_ITEMS_TAG) || stack.isOf(Resonance.PEARL) || stack.isOf(Resonance.BIFURCATED_PEARL)) && (!stack.contains(DataComponentTypes.DAMAGE) || stack.getDamage() != stack.getMaxDamage())) {
			world.playSound(null, pos, Resonance.PUT_ITEM_SOUND.value(), SoundCategory.BLOCKS);
			if (!world.isClient() && world.getBlockEntity(pos) instanceof PearlHubBlockEntity blockEntity) {
				blockEntity.setStack(stack.split(1));
				world.setBlockState(pos, state.with(HAS_PEARL, true));
				if (state.get(BEACON_POWERED)) {
					world.spawnEntity(new BeaconedPearlEntity(world, pos.getX() + 0.5, pos.getY() + 2.2, pos.getZ() + 0.5, blockEntity.getStack()));
					for (ServerPlayerEntity player : world.getNonSpectatingEntities(ServerPlayerEntity.class, new Box(pos).expand(12))) {
						Resonance.PEARL_HUB_CRITERION.trigger(player, blockEntity.getStack(), blockEntity.getLevel());
					}
				}
			}
			return ItemActionResult.success(world.isClient());
		}
		return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient() && world.getBlockEntity(pos) instanceof PearlHubBlockEntity blockEntity && state.get(HAS_PEARL) && !state.get(BEACON_POWERED)) {
			if (player.isSneaking()) {
				blockEntity.dropStack(world, pos, 0.5, 0.6, 0.5);
				blockEntity.setStack(ItemStack.EMPTY);
				world.setBlockState(pos, state.with(HAS_PEARL, false));
			} else if (!state.get(EXPENDED)) {
				world.setBlockState(pos, state.with(EXPENDED, true));
				int i = blockEntity.usePearl(world, pos, player);
				world.scheduleBlockTick(pos, this, world.getGameRules().getInt(Resonance.PEARL_REFRESH_DELAY_GAMERULE) / i);
			}
			return ActionResult.success(true);
		}
		return ActionResult.CONSUME;
	}

	@Override
	protected void onExploded(BlockState state, World world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
		if (!state.get(EXPENDED) && !state.get(BEACON_POWERED) && state.get(HAS_PEARL) && world.getBlockEntity(pos) instanceof PearlHubBlockEntity pearlHubBlockEntity) {
			world.setBlockState(pos, state.with(EXPENDED, true));
			int i = pearlHubBlockEntity.pulsePearl(world, pos);
			world.scheduleBlockTick(pos, this, world.getGameRules().getInt(Resonance.PEARL_REFRESH_DELAY_GAMERULE) / i);
		}
		super.onExploded(state, world, pos, explosion, stackMerger);
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS);
		world.setBlockState(pos, state.with(EXPENDED, false));
		world.emitGameEvent(null, GameEvent.BLOCK_DEACTIVATE, pos);
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (state.isOf(newState.getBlock())) { return; }
		if (world.getBlockEntity(pos) instanceof PearlHubBlockEntity blockEntity && state.get(HAS_PEARL) && !state.get(BEACON_POWERED)) {
			blockEntity.dropStack(world, pos, 0.5, 0.6, 0.5);
			blockEntity.clear();
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	@Override
	protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
		if (!world.isClient() && world.isReceivingRedstonePower(pos) != state.get(POWERED)) {
			if (!state.get(EXPENDED) && state.get(HAS_PEARL) && !state.get(BEACON_POWERED) && world.getBlockEntity(pos) instanceof PearlHubBlockEntity pearlHubBlockEntity) {
				int i = pearlHubBlockEntity.pulsePearl(world, pos);
				state = state.with(EXPENDED, true);
				world.scheduleBlockTick(pos, this, 5 * world.getGameRules().getInt(Resonance.PEARL_REFRESH_DELAY_GAMERULE) / i);
			}
			world.setBlockState(pos, state.cycle(POWERED));
		}
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return !world.isClient() && state.get(BEACON_POWERED) ? validateTicker(type, Resonance.PEARL_HUB_BLOCK_ENTITY, PearlHubBlockEntity::tick) : null;
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED)) { world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world)); }
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}
}