package net.moss.resonance.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.RedstoneView;
import net.moss.resonance.Resonance;
import net.minecraft.state.property.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneView.class)
public interface RedstoneViewMixin extends BlockView {
    @Inject(method = "Lnet/minecraft/world/RedstoneView;getEmittedRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", at = @At(value = "HEAD"), cancellable = true)
    private void getEmittedRedstonePower(BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir) {
        for (int i = 0; i < 8; i++) {
            BlockState blockState = this.getBlockState(pos.offset(direction, i));
            if (blockState.isIn(BlockTags.OCCLUDES_VIBRATION_SIGNALS)) { return; }
            if (i % 2 == 0) { continue; }
            if (blockState.isOf(Resonance.BATHYAL_HEART) && blockState.get(Properties.POWERED)) {
                int power = blockState.get(Properties.POWER);
                int bitValue = (power >> ((i + 1) / 2 - 1)) & 1;
                if (bitValue == 1) { cir.setReturnValue(power); }
            }
        }
    }
}