// Source code is decompiled from a .class file using FernFlower decompiler.
package net.moss.resonance.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LandingBlock;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.Thickness;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;
import net.moss.resonance.Resonance;
import net.moss.resonance.mixin.AbstractCauldronBlockAccessor;
import net.moss.resonance.util.Utils;
import org.jetbrains.annotations.Nullable;

public class Icicle extends Block implements LandingBlock, Waterloggable {
    public static final DirectionProperty VERTICAL_DIRECTION = Properties.VERTICAL_DIRECTION;
    public static final EnumProperty<Thickness> THICKNESS = Properties.THICKNESS;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final IntProperty AGE = Properties.AGE_5;
    private static final VoxelShape TIP_MERGE_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape UP_TIP_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape DOWN_TIP_SHAPE = Block.createCuboidShape(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape BASE_SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape FRUSTUM_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape MIDDLE_SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
    private static final VoxelShape DRIP_COLLISION_SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);

    public Icicle(AbstractBlock.Settings settings) {
        super(settings.offset(OffsetType.XZ));
        this.setDefaultState(this.getDefaultState().with(VERTICAL_DIRECTION, Direction.UP).with(THICKNESS, Thickness.TIP).with(WATERLOGGED, false).with(AGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{VERTICAL_DIRECTION, THICKNESS, WATERLOGGED, AGE});
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if ((Boolean)state.get(WATERLOGGED)) { world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world)); }
        if (direction != Direction.UP && direction != Direction.DOWN) { return state; }
        else {
            Direction direction2 = (Direction)state.get(VERTICAL_DIRECTION);
            if (direction2 == Direction.DOWN && world.getBlockTickScheduler().isQueued(pos, this)) {
                return state;
            } else if (direction == direction2.getOpposite() && !this.canPlaceAt(state, world, pos)) {
                if (direction2 == Direction.DOWN) { world.scheduleBlockTick(pos, this, 2); }
                else { world.scheduleBlockTick(pos, this, 1); }
                return state;
            } else {
                boolean bl = state.get(THICKNESS) == Thickness.TIP_MERGE;
                Thickness thickness = getThickness(world, pos, direction2, bl);
                return state.with(THICKNESS, thickness);
            }
        }
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        if (world.isClient) { return; }
        BlockPos blockPos = hit.getBlockPos();
        if (projectile.canModifyAt(world, blockPos) && projectile.canBreakBlocks(world) && projectile.getVelocity().length() > 0.6) { world.breakBlock(blockPos, true); }
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (state.get(VERTICAL_DIRECTION) == Direction.UP && state.get(THICKNESS) == Thickness.TIP) {
            entity.handleFallDamage(fallDistance + 2.0F, 2.0F, Utils.damageSource(world, Resonance.ICICLE_DAMAGE));
        } else {
            super.onLandedUpon(world, state, pos, entity, fallDistance);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (isFacingDirection(state, Direction.DOWN) && state.get(THICKNESS) == Thickness.TIP && !isColdAndWet(state, world, pos)) {
            if (random.nextFloat() < 0.2F) {
                Vec3d vec3d = state.getModelOffset(world, pos);
                double a = pos.getX() + 0.5 + vec3d.x;
                double b = pos.getY() + 1 - 0.065F;
                double c = pos.getZ() + 0.5 + vec3d.z;
                world.addParticle(ParticleTypes.DRIPPING_DRIPSTONE_WATER, a, b, c, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (isFacingDirection(state, Direction.UP) && !this.canPlaceAt(state, world, pos)) { world.breakBlock(pos, true); }
        else { spawnFallingBlock(state, world, pos); }
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getLightLevel(LightType.BLOCK, pos) > 11 - state.getOpacity(world, pos) || world.getDimension().ultrawarm()) { world.removeBlock(pos, false); }
        dripTick(state, world, pos, random.nextFloat());
        if (state.get(WATERLOGGED) && isFacingDirection(state, Direction.DOWN)) {
            if (isTip(state, true) && state.get(THICKNESS) == Thickness.TIP_MERGE) {
                freezeFloor(state, world, pos, random);
            } else if (random.nextFloat() < 0.01F && state.get(AGE) < 5) {
                BlockPos posDown = pos.down();
                BlockPos posUp = pos.up();
                BlockState stateDown = world.getBlockState(posDown);
                boolean isCold = isColdAndWet(state, world, pos);
                int age = Math.min(state.get(AGE) + random.nextBetween(1, isCold ? 2 : 3), 5);
                if (isTip(stateDown, false) && stateDown.get(VERTICAL_DIRECTION) == Direction.UP) {
                    place(world, pos, Direction.DOWN, Thickness.TIP_MERGE, age);
                    place(world, posDown, Direction.UP, Thickness.TIP_MERGE, age);
                    freezeFloor(state, world, pos, random);
                } else if (stateDown.isFullCube(world, posDown)) {
                    place(world, pos, Direction.DOWN, Thickness.TIP_MERGE, age);
                    freezeFloor(state, world, pos, random);
                } else if (stateDown.isAir() || stateDown.isOf(Blocks.WATER)) {
                    place(world, posDown, Direction.DOWN, Thickness.TIP, age);
                } else if (state.get(THICKNESS) == Thickness.BASE && stateDown.isOf(Resonance.ICICLE) && stateDown.get(THICKNESS) == Thickness.MIDDLE) {
                    world.setBlockState(pos, Blocks.ICE.getDefaultState(), 3);
                    place(world, posDown, Direction.DOWN, Thickness.BASE, age);
                } else if (state.get(THICKNESS) == Thickness.BASE && state.get(AGE) == 2 && world.getBlockState(posUp).isOf(Blocks.ICE)) {
                    world.setBlockState(posUp, Blocks.PACKED_ICE.getDefaultState(), 3);
                }
            }
        }
    }

    public void freezeFloor(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        Optional<BlockPos> optional = getFloorPos(world, pos, state, 11);
        if (optional.isPresent()) {
            BrittleBlock.makeBrittle(state, world, pos, random);
            for (Direction direction : Direction.values()) {
                BlockPos pos2 = optional.get().offset(direction);
                if (world.getBlockState(pos2).isOf(Blocks.WATER)) {
                    BlockState state2 = Resonance.ICE_FUR.getDefaultState();
                    for (Direction direction2 : Direction.values()) { state2 = state2.with(IceFur.getProperty(direction2), world.getBlockState(pos2.offset(direction2)).isSideSolidFullSquare(world, pos2, direction2.getOpposite())); }
                    if (IceFur.hasAnyDirection(state2)) { world.setBlockState(pos2, state2, Block.NOTIFY_ALL); }
                }
                BlockPos pos3 = optional.get().offset(direction).up();
                if (world.getBlockState(pos3).isOf(Blocks.WATER)) {
                    BlockState state2 = Resonance.ICE_FUR.getDefaultState();
                    for (Direction direction2 : Direction.values()) { state2 = state2.with(IceFur.getProperty(direction2), world.getBlockState(pos3.offset(direction2)).isSideSolidFullSquare(world, pos3, direction2.getOpposite())); }
                    if (IceFur.hasAnyDirection(state2)) { world.setBlockState(pos3, state2, Block.NOTIFY_ALL); }
                }
            }
        }
    }

    protected boolean isColdAndWet(BlockState state, World world, BlockPos pos) {
        return state.get(WATERLOGGED) && world.getBiome(pos).value().isCold(pos);
    }

    @VisibleForTesting
    public static void dripTick(BlockState state, ServerWorld world, BlockPos pos, float dripChance) {
        if (!(dripChance > 0.2F)) {
        Optional<BlockPos> optional = getSupportingPos(world, pos, state, 11);
        if (optional.isPresent()) {
            BlockPos pos2 = optional.get().up();
                BlockState state2 = world.getBlockState(pos2);
                if (state2.isOf(Blocks.MUD)) {
                    BlockState blockState = Blocks.COARSE_DIRT.getDefaultState();
                    world.setBlockState(pos2, blockState);
                    Block.pushEntitiesUpBeforeBlockChange(state2, blockState, world, pos2);
                    world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos2, Emitter.of(blockState));
                    world.syncWorldEvent(1504, pos, 0);
                }
            }
            BlockPos blockPos2 = getCauldronPos(world, pos);
            if (blockPos2 != null) {
                BlockState blockState2 = world.getBlockState(blockPos2);
                if (blockState2.isOf(Blocks.CAULDRON)) { world.setBlockState(blockPos2, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 1), Block.NOTIFY_LISTENERS); }
                if (blockState2.isOf(Blocks.WATER_CAULDRON)) {
                    int level = blockState2.get(LeveledCauldronBlock.LEVEL);
                    if (level < 3) { world.setBlockState(blockPos2, blockState2.with(LeveledCauldronBlock.LEVEL, level + 1), Block.NOTIFY_LISTENERS); }
                }
            }
        }
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        WorldAccess worldAccess = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        Direction direction = ctx.getVerticalPlayerLookDirection().getOpposite();
        Direction direction2 = getDirectionToPlaceAt(worldAccess, blockPos, direction);
        if (direction2 == null) { return null; }
        else {
            boolean bl = !ctx.shouldCancelInteraction();
            Thickness thickness = getThickness(worldAccess, blockPos, direction2, bl);
            return thickness == null ? null : this.getDefaultState().with(VERTICAL_DIRECTION, direction2).with(THICKNESS, thickness).with(WATERLOGGED, worldAccess.getFluidState(blockPos).getFluid() == Fluids.WATER);
        }
    }

    @Override protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) { return canPlaceAtWithDirection(world, pos, state.get(VERTICAL_DIRECTION)); }
    @Override public DamageSource getDamageSource(Entity attacker) { return Utils.damageSource(attacker.getWorld(), Resonance.ICICLE_DAMAGE); }
    @Override protected boolean canPathfindThrough(BlockState state, NavigationType type) { return false; }
    @Override protected boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) { return false; }
    @Override protected float getMaxHorizontalModelOffset() { return 0.125F; }
    @Override protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) { if (state.get(WATERLOGGED)) { entity.setInPowderSnow(true); } }
    @Override protected boolean hasRandomTicks(BlockState state) { return state.get(AGE) < 5; }
    @Override protected FluidState getFluidState(BlockState state) { return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state); }
    @Override protected VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) { return VoxelShapes.empty(); }
    @Override public void onDestroyedOnLanding(World world, BlockPos pos, FallingBlockEntity fallingBlockEntity) { if (!fallingBlockEntity.isSilent()) { world.syncWorldEvent(1045, pos, 0); } }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Thickness thickness = state.get(THICKNESS);
        VoxelShape voxelShape;
        if (thickness == Thickness.TIP_MERGE) { voxelShape = TIP_MERGE_SHAPE; }
        else if (thickness == Thickness.TIP) {
            if (state.get(VERTICAL_DIRECTION) == Direction.DOWN) { voxelShape = DOWN_TIP_SHAPE; }
            else { voxelShape = UP_TIP_SHAPE; }
        }
        else if (thickness == Thickness.FRUSTUM) { voxelShape = BASE_SHAPE; }
        else if (thickness == Thickness.MIDDLE) { voxelShape = FRUSTUM_SHAPE; }
        else { voxelShape = MIDDLE_SHAPE; }
        Vec3d vec3d = state.getModelOffset(world, pos);
        return voxelShape.offset(vec3d.x, 0.0, vec3d.z);
    }

    private static void spawnFallingBlock(BlockState state, ServerWorld world, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for(BlockState blockState = state; isFacingDirection(blockState, Direction.DOWN); blockState = world.getBlockState(mutable)) {
            FallingBlockEntity fallingBlockEntity = FallingBlockEntity.spawnFromBlock(world, mutable, blockState);
            if (isTip(blockState, true)) {
                fallingBlockEntity.setHurtEntities(1.0F * Math.max(1 + pos.getY() - mutable.getY(), 6), 40);
                break;
            }
            mutable.move(Direction.DOWN);
        }
    }

    private static void place(WorldAccess world, BlockPos pos, Direction direction, Thickness thickness, int age) {
        world.setBlockState(pos, Resonance.ICICLE.getDefaultState().with(VERTICAL_DIRECTION, direction).with(THICKNESS, thickness).with(AGE, age).with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER), 3);
    }

    @Nullable
    private static Direction getDirectionToPlaceAt(WorldView world, BlockPos pos, Direction direction) {
        Direction direction2;
        if (canPlaceAtWithDirection(world, pos, direction)) { direction2 = direction; }
        else {
            if (!canPlaceAtWithDirection(world, pos, direction.getOpposite())) { return null; }
            direction2 = direction.getOpposite();
        }
        return direction2;
    }

    private static Thickness getThickness(WorldView world, BlockPos pos, Direction direction, boolean tryMerge) {
        Direction direction2 = direction.getOpposite();
        BlockState blockState = world.getBlockState(pos.offset(direction));
        if (isFacingDirection(blockState, direction2)) {
            return !tryMerge && blockState.get(THICKNESS) != Thickness.TIP_MERGE ? Thickness.TIP : Thickness.TIP_MERGE;
        } else if (blockState.isSideSolidFullSquare(world, pos.offset(direction), direction2)) {
            return !tryMerge ? Thickness.TIP : Thickness.TIP_MERGE;
        } else if (!isFacingDirection(blockState, direction)) {
            return Thickness.TIP;
        } else {
            Thickness thickness = blockState.get(THICKNESS);
            if (thickness != Thickness.TIP && thickness != Thickness.TIP_MERGE) {
                BlockState blockState2 = world.getBlockState(pos.offset(direction2));
                return !isFacingDirection(blockState2, direction) ? Thickness.BASE : Thickness.MIDDLE;
            } else {
                return Thickness.FRUSTUM;
            }
        }
    }

    private static Optional<BlockPos> getSupportingPos(World world, BlockPos pos, BlockState state, int range) {
        Direction direction = (Direction)state.get(VERTICAL_DIRECTION);
        BiPredicate<BlockPos, BlockState> biPredicate = (posx, statex) -> { return statex.isOf(Resonance.ICICLE) && statex.get(VERTICAL_DIRECTION) == direction; };
        return searchInDirection(world, pos, direction.getOpposite().getDirection(), biPredicate, (statex) -> { return !statex.isOf(Resonance.ICICLE); }, range);
    }

    private static Optional<BlockPos> getFloorPos(World world, BlockPos pos, BlockState state, int range) {
        Direction direction = (Direction)state.get(VERTICAL_DIRECTION);
        BiPredicate<BlockPos, BlockState> biPredicate = (posx, statex) -> { return statex.isOf(Resonance.ICICLE); };
        return searchInDirection(world, pos, direction.getDirection(), biPredicate, (statex) -> { return !statex.isOf(Resonance.ICICLE); }, range);
    }

    private static boolean canPlaceAtWithDirection(WorldView world, BlockPos pos, Direction direction) {
        BlockPos blockPos = pos.offset(direction.getOpposite());
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isSideSolidFullSquare(world, blockPos, direction) || isFacingDirection(blockState, direction);
    }

    private static boolean isTip(BlockState state, boolean allowMerged) {
        if (!state.isOf(Resonance.ICICLE)) { return false; }
        else {
            Thickness thickness = state.get(THICKNESS);
            return thickness == Thickness.TIP || allowMerged && thickness == Thickness.TIP_MERGE;
        }
    }

    private static boolean isFacingDirection(BlockState state, Direction direction) {
        return state.isOf(Resonance.ICICLE) && state.get(VERTICAL_DIRECTION) == direction;
    }

    @Nullable
    private static BlockPos getCauldronPos(World world, BlockPos pos) {
        Predicate<BlockState> predicate = (state) -> { return state.getBlock() instanceof AbstractCauldronBlock block && ((AbstractCauldronBlockAccessor)block).canBeFilledByDripstone(Fluids.WATER); };
        BiPredicate<BlockPos, BlockState> biPredicate = (posx, state) -> { return canDripThrough(world, posx, state); };
        return searchInDirection(world, pos, Direction.DOWN.getDirection(), biPredicate, predicate, 11).orElse(null);
    }

    private static Optional<BlockPos> searchInDirection(WorldAccess world, BlockPos pos, Direction.AxisDirection direction, BiPredicate<BlockPos, BlockState> continuePredicate, Predicate<BlockState> stopPredicate, int range) {
        Direction direction2 = Direction.get(direction, Axis.Y);
        BlockPos.Mutable mutable = pos.mutableCopy();
        for(int i = 1; i < range; ++i) {
            mutable.move(direction2);
            BlockState blockState = world.getBlockState(mutable);
            if (stopPredicate.test(blockState)) { return Optional.of(mutable.toImmutable()); }
            if (world.isOutOfHeightLimit(mutable.getY()) || !continuePredicate.test(mutable, blockState)) { return Optional.empty(); }
        }
        return Optional.empty();
    }

    private static boolean canDripThrough(BlockView world, BlockPos pos, BlockState state) {
        if (state.isAir()) { return true; }
        else if (state.isOpaqueFullCube(world, pos)) { return false; }
        else if (!state.getFluidState().isEmpty()) { return false; }
        else { return !VoxelShapes.matchesAnywhere(DRIP_COLLISION_SHAPE, state.getCollisionShape(world, pos), BooleanBiFunction.AND); }
    }
}