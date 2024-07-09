package net.moss.resonance.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DatapackBlock extends Block {
    public static final IntProperty TYPE = IntProperty.of("type",1,32);
    public static final IntProperty LIGHT = IntProperty.of("light",0,15);

    public DatapackBlock(AbstractBlock.Settings settings) {
        super(settings.luminance(state -> state.get(LIGHT)));
        this.setDefaultState(this.getDefaultState().with(TYPE, 1).with(LIGHT, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TYPE, LIGHT);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        player.increaseStat(Stats.BROKEN.getOrCreateStat(this.asItem()), state.get(TYPE));
        return ActionResult.CONSUME;
    }
}