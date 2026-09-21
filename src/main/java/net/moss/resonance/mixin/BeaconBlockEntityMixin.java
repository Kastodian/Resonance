package net.moss.resonance.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BeaconBlockEntity.BeamSegment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.moss.resonance.Resonance;
import net.moss.resonance.block.entity.PearlHubBlockEntity;
import net.moss.resonance.util.interfaces.BeamSegmentInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import com.llamalad7.mixinextras.sugar.Local;
import com.google.common.collect.Lists;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {
	@Inject(method = "tick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
	private static void tick(World world, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity, CallbackInfo ci, @Local(ordinal = 1) BlockPos blockPos) {
		BlockState blockState = world.getBlockState(blockPos);
		if (blockState.isOf(Resonance.PEARL_HUB) && blockEntity instanceof BeaconBlockEntityAccessor accessor) {
			boolean clampBeam = !blockEntity.getBeamSegments().isEmpty();
			// if (world.getTime() % 80L == 0L) {
				BlockPos pos2 = blockEntity.getPos();
				// TODO: may be laggy vvvvvv
				int level = BeaconBlockEntityAccessor.updateLevel(world, pos2.getX(), pos2.getY(), pos2.getZ());
				if (level != 0 && world.getBlockEntity(blockPos) instanceof PearlHubBlockEntity hub) {
					hub.beaconActivate(world, blockPos, blockState, hub, blockEntity, level);
					clampBeam = true;
				} else {
					clampBeam = false;
				}
			// }
			if (clampBeam) {
				BeamSegment endSegment = new BeamSegment(1);
				if (endSegment instanceof BeamSegmentInterface segment) { segment.setHidden(true); }
				accessor.getField().add(endSegment);
				accessor.setMinY(world.getBottomY() - 1);
				accessor.setBeamSegments(accessor.getField());
				if (accessor.getLevel() >= 0) { accessor.setLevel(-1); }
			} else {
				accessor.setBeamSegments(Lists.newLinkedList());
			}
			ci.cancel();
		}
	}
}