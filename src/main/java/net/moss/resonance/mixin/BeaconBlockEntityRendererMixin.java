package net.moss.resonance.mixin;

import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.moss.resonance.util.interfaces.BeamSegmentInterface;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
@Mixin(BeaconBlockEntityRenderer.class)
public class BeaconBlockEntityRendererMixin {
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BeaconBlockEntityRenderer;renderBeam(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;FJIII)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
	private void render(BeaconBlockEntity beaconBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, CallbackInfo ci, long l, List<BeaconBlockEntity.BeamSegment> list, int k, int m, BeaconBlockEntity.BeamSegment beamSegment) {
		if (beamSegment instanceof BeamSegmentInterface accessor && accessor.isHidden()) { ci.cancel(); }
	}
}