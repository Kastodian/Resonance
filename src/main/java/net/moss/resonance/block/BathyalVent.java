// Source code is decompiled from a .class file using FernFlower decompiler.
package net.moss.resonance.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.event.GameEvent;
import net.moss.resonance.Resonance;
import net.moss.resonance.particle.VentSmoke;
import net.moss.resonance.util.VentThickness;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class BathyalVent extends Block implements Waterloggable {
    public static final EnumProperty<VentThickness> THICKNESS = EnumProperty.of("thickness", VentThickness.class);
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.of("color", DyeColor.class);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final IntProperty AGE = Properties.AGE_5;
    public static final BooleanProperty LIT = Properties.LIT;
    private static final VoxelShape UP_TIP_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape BASE_SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape FRUSTUM_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape MIDDLE_SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public BathyalVent(AbstractBlock.Settings settings) {
        super(settings.offset(OffsetType.XZ).luminance(state -> state.get(LIT) ? 7 - state.get(AGE) : 0));
        this.setDefaultState(this.getDefaultState().with(THICKNESS, VentThickness.TIP).with(WATERLOGGED, false).with(AGE, 0).with(LIT, false).with(COLOR, DyeColor.GRAY));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{THICKNESS, WATERLOGGED, AGE, LIT, COLOR});
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if ((Boolean)state.get(WATERLOGGED)) { world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world)); }
        world.scheduleBlockTick(pos, this, 1);
        if (direction == Direction.UP) {
            VentThickness thickness = getThickness(world, pos, false);
            return state.with(THICKNESS, thickness).with(LIT, state.get(LIT) && thickness == VentThickness.TIP);
        }
        return state;
    }
    
    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!this.canPlaceAt(state, world, pos)) { world.breakBlock(pos, true); }
        else if (state.get(THICKNESS) == VentThickness.TIP && state.get(LIT)) {
            for (Direction direction : Direction.values()) {
                if (world.getBlockState(pos.offset(direction)).isOf(Blocks.WATER)) { VentFluid.update(world, pos.offset(direction), state); }
            }
        }
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        WorldAccess worldAccess = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        VentThickness thickness = getThickness(worldAccess, blockPos, !ctx.shouldCancelInteraction());
        return thickness == null ? null : this.getDefaultState().with(THICKNESS, thickness).with(WATERLOGGED, worldAccess.getFluidState(blockPos).getFluid() == Fluids.WATER);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isSideSolidFullSquare(world, blockPos, Direction.UP) || blockState.isOf(Resonance.BATHYAL_VENT);
    }

    @Override protected boolean canPathfindThrough(BlockState state, NavigationType type) { return false; }
    @Override protected boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) { return false; }
    @Override protected float getMaxHorizontalModelOffset() { return 0.125F; }
    @Override protected boolean hasRandomTicks(BlockState state) { return state.get(THICKNESS) == VentThickness.TIP; }
    @Override protected FluidState getFluidState(BlockState state) { return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state); }
    @Override protected VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) { return VoxelShapes.empty(); }
    
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VentThickness thickness = state.get(THICKNESS);
        VoxelShape voxelShape;
        if (thickness == VentThickness.TIP) { voxelShape = UP_TIP_SHAPE; }
        else if (thickness == VentThickness.FRUSTUM) { voxelShape = BASE_SHAPE; }
        else if (thickness == VentThickness.MIDDLE) { voxelShape = FRUSTUM_SHAPE; }
        else { voxelShape = MIDDLE_SHAPE; }
        Vec3d vec3d = state.getModelOffset(world, pos);
        return voxelShape.offset(vec3d.x, 0.0, vec3d.z);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof DyeItem dyeItem && dyeItem.getColor() != state.get(COLOR)) {
            stack.decrement(1);
            player.incrementStat(Stats.USED.getOrCreateStat(dyeItem));
            world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);
            world.setBlockState(pos, state.with(COLOR, dyeItem.getColor()), Block.NOTIFY_ALL);
			return ItemActionResult.success(world.isClient);
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        Boolean light = random.nextInt(20) == 1;
        Boolean grow = state.get(WATERLOGGED) && state.get(AGE) < 5 && random.nextInt(50) == 2;
        if (random.nextInt(5) != 1 && state.get(LIT)) {
            BlockState upState = world.getBlockState(pos.up());
            if (upState.getBlock() instanceof Oxidizable oxidizable) { oxidizable.tryDegrade(upState, world, pos.up(), random).ifPresent(degraded -> world.setBlockState(pos.up(), degraded)); }
        }
        if (!(light || grow)) { return; }
        Optional<BlockPos> supportingPos = getSupportingPos(world, pos, state, 11);
        if (supportingPos.isPresent()) {
            BlockPos blockPos = supportingPos.get();
            boolean heatSource = false;
            for (int i = 1; i < 5; i++) {
                if (world.getBlockState(blockPos.down(i)).isIn(Resonance.VENT_HEAT_SOURCE_BLOCKS_TAG)) {
                    heatSource = true;
                    break; 
                }
            }
            if (light) {
                world.setBlockState(pos, state.with(LIT, heatSource), Block.NOTIFY_ALL);
                if (heatSource) { world.scheduleBlockTick(pos, this, 1); }
            } else if (grow && heatSource && VentFluid.isStillWater(world.getBlockState(pos.up()))) {
                DimensionType dim = world.getDimension();
                int age = Math.min(state.get(AGE) + random.nextBetween(1, 3 + ((pos.getY() + dim.minY()) / (dim.height() + dim.minY())) * 3), 5);
                world.setBlockState(pos.up(), Resonance.BATHYAL_VENT.getDefaultState().with(THICKNESS, VentThickness.TIP).with(AGE, age).with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER), Block.NOTIFY_ALL);
            }
        }
    }

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		// TODO: make it like powdered snow
		// TODO: add new damage type
		if (state.get(LIT) && state.get(WATERLOGGED) && entity instanceof LivingEntity) { entity.damage(world.getDamageSources().hotFloor(), 1.0F); }
		super.onEntityCollision(state, world, pos, entity);
	}

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        if (state.get(LIT) && state.get(WATERLOGGED)) {
            Vec3d vec3d = state.getModelOffset(world, pos);
            Vector3f colour = Vec3d.unpackRgb(state.get(COLOR).getFireworkColor()).toVector3f();
            for (int i = 0; i < random.nextInt(3) + 1; i++) {
                world.addImportantParticle(
                        new VentSmoke(colour, 1.0F + random.nextFloat() * 2, false), true,
                        pos.getX() + 0.5 + vec3d.x + (random.nextDouble() - 0.5) / 5.0,
                        pos.getY() + 0.25 + random.nextDouble(),
                        pos.getZ() + 0.5 + vec3d.z + (random.nextDouble() - 0.5) / 5.0,
                        0.001 * random.nextBetween(-1, 1), 0.01 * (random.nextInt(3) + 1), 0.001 * random.nextBetween(-1, 1));
            }
        }
    }

    private static VentThickness getThickness(WorldView world, BlockPos pos, boolean tryMerge) {
        BlockState blockState = world.getBlockState(pos.up());
        if (blockState.isOf(Resonance.BATHYAL_VENT)) {
            VentThickness thickness = blockState.get(THICKNESS);
            if (thickness != VentThickness.TIP) { return !world.getBlockState(pos.down()).isOf(Resonance.BATHYAL_VENT) ? VentThickness.BASE : VentThickness.MIDDLE; }
            else { return VentThickness.FRUSTUM; }
        }
        return VentThickness.TIP;
    }

    private static Optional<BlockPos> getSupportingPos(World world, BlockPos pos, BlockState state, int range) {
        BiPredicate<BlockPos, BlockState> biPredicate = (posx, statex) -> { return statex.isOf(Resonance.BATHYAL_VENT); };
        return searchInDirection(world, pos, Direction.DOWN.getDirection(), biPredicate, (statex) -> { return !statex.isOf(Resonance.BATHYAL_VENT); }, range);
    }

    private static Optional<BlockPos> searchInDirection(WorldAccess world, BlockPos pos, Direction.AxisDirection direction, BiPredicate<BlockPos, BlockState> continuePredicate, Predicate<BlockState> stopPredicate, int range) {
        Direction direction2 = Direction.get(direction, Direction.Axis.Y);
        BlockPos.Mutable mutable = pos.mutableCopy();
        for(int i = 1; i < range; ++i) {
            mutable.move(direction2);
            BlockState blockState = world.getBlockState(mutable);
            if (stopPredicate.test(blockState)) { return Optional.of(mutable.toImmutable()); }
            if (world.isOutOfHeightLimit(mutable.getY()) || !continuePredicate.test(mutable, blockState)) { return Optional.empty(); }
        }
        return Optional.empty();
    }
}