package net.moss.resonance.block;

import java.util.Optional;
import java.util.Arrays;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LichenGrower;
import net.minecraft.block.LichenGrower.GrowPos;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class IceFur extends MultifaceGrowthBlock {
    public static final MapCodec<IceFur> CODEC = createCodec(IceFur::new);
    private final LichenGrower grower = new LichenGrower(new IceFurGrowerChecker(this));
    public static final IntProperty AGE = Properties.AGE_7;

    public IceFur(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(AGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(new Property[]{AGE});
    }

    @Override protected FluidState getFluidState(BlockState state) { return Fluids.WATER.getStill(false); }
    @Override protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) { return state.getFluidState().isEmpty(); }
    @Override public LichenGrower getGrower() { return this.grower; }
    @Override protected MapCodec<? extends MultifaceGrowthBlock> getCodec() { return CODEC; }
    @Override protected boolean hasRandomTicks(BlockState state) { return true; }
    @Override protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) { entity.setInPowderSnow(true); }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    protected static boolean hasAnyDirection(BlockState state) {
        return Arrays.stream(DIRECTIONS).anyMatch((direction) -> { return hasDirection(state, direction); });
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return fluidState.isIn(FluidTags.WATER) && fluidState.getLevel() == 8 ? super.getPlacementState(ctx) : null;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getLightLevel(LightType.BLOCK, pos) > 11 - state.getOpacity(world, pos) || world.getDimension().ultrawarm()) { world.removeBlock(pos, false); return; }
        int currentAge = state.get(AGE);
        if (currentAge > 6) { return; }
        int age = Math.min(currentAge + random.nextBetween(1, world.getBiome(pos).value().isCold(pos) ? 2 : 3), 7);
        Optional<GrowPos> growPos = this.grower.grow(state.with(AGE, age), world, pos, random);
        if (growPos.isPresent() && growPos.get().pos() != pos) {
            BlockPos pos2 = growPos.get().pos();
            world.setBlockState(pos2, world.getBlockState(pos2).with(AGE, age), Block.NOTIFY_ALL);
        }
        for (Direction direction : Direction.values()) {
            BlockPos pos2 = pos.offset(direction);
            BlockState state2 = world.getBlockState(pos2);
            if (state.get(AGE) < 4 && state2.isOf(this) && state2.get(AGE) > 6) { world.setBlockState(pos2, state2.with(AGE, age), Block.NOTIFY_ALL); }
            if (!hasDirection(state, direction)) { continue; }
            BrittleBlock.makeBrittle(state2, world, pos2, random);
        }
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand.equals(Hand.MAIN_HAND) && player.getOffHandStack().isOf(Items.SHIELD) && !player.shouldCancelInteraction()) { return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; }
        else if (!stack.isIn(ItemTags.PICKAXES)) { return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; }
        world.addBlockBreakParticles(pos, state);
        world.breakBlock(pos, true);
        if (player instanceof ServerPlayerEntity serverPlayerEntity) { Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayerEntity, pos, stack); }
        return ItemActionResult.SUCCESS;
    }

    public class IceFurGrowerChecker extends LichenGrower.LichenGrowChecker {
        public IceFurGrowerChecker(final IceFur sculkVeinBlock) {
            super(sculkVeinBlock);
        }
        
        @Override
        public boolean canGrow(BlockView world, BlockPos pos, BlockPos growPos, Direction direction, BlockState state) {
            FluidState fluidState = state.getFluidState();
            if (fluidState.isEmpty() || !fluidState.isOf(Fluids.WATER)) { return false; }
            return super.canGrow(world, pos, growPos, direction, state);
        }
    }
}