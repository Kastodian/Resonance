package net.moss.resonance.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.moss.resonance.block.PearlHubBlock;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private static void tick(World world, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity, CallbackInfo ci) {
		if (!(world.getBlockState(pos.up()).getBlock() instanceof PearlHubBlock)) { return; }
		((BeaconBlockEntityAccessor) blockEntity).setField_19178(List.of());
		((BeaconBlockEntityAccessor) blockEntity).setBeamSegments(List.of());
		((BeaconBlockEntityAccessor) blockEntity).setMinY(world.getBottomY() - 1);
		ci.cancel();
	}
}