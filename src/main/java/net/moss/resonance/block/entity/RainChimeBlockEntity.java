package net.moss.resonance.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.moss.resonance.Resonance;

public class RainChimeBlockEntity extends BlockEntity {
    public RainChimeBlockEntity(BlockPos pos, BlockState state) {
        super(Resonance.RAIN_CHIME_BLOCK_ENTITY, pos, state);
    }
}