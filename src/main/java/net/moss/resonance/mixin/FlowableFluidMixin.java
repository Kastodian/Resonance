package net.moss.resonance.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.moss.resonance.Resonance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowableFluid.class)
public class FlowableFluidMixin {
	@Shadow protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {}

	@Inject(method = "flow", at = @At("HEAD"), cancellable = true)
	public void flow(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState, CallbackInfo ci) {
		if (state.isIn(Resonance.WATERLOGGABLE_WATERBREAKABLE_TAG)) {
			if (!state.isAir()) { this.beforeBreakingBlock(world, pos, state); }
			world.setBlockState(pos, fluidState.getBlockState(), 3);
			ci.cancel();
		}
	}

	@Inject(method = "canFill", at = @At("HEAD"), cancellable = true)
	public void canFill(BlockView world, BlockPos pos, BlockState state, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
		if (state.isIn(Resonance.WATERLOGGABLE_WATERBREAKABLE_TAG)) { cir.setReturnValue(true); }
	}
}